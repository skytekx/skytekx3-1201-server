package com.lvedy.at_mod.client.aurora;

import com.lvedy.at_mod.AlternativeTwilight;
import com.lvedy.at_mod.config.Config;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.joml.Matrix4f;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * 把极光 PostChain 装入 vanilla 的渲染流程。
 * <ul>
 *   <li>在 mod bus 上接 {@link RegisterClientReloadListenersEvent} 注册资源重载监听器；</li>
 *   <li>在 game bus 上接 {@link RenderLevelStageEvent}{@code .Stage.AFTER_TRANSLUCENT_BLOCKS}；</li>
 *   <li>每帧把当前矩阵 / 锚点 / 强度 / 颜色推到 shader uniform；</li>
 *   <li>检测 Iris/Oculus 启用时主动停用，避免与 shaderpack 抢 main framebuffer。</li>
 * </ul>
 *
 * <p>1.20.1 backport: the view (model-view) matrix is the level PoseStack's pose (1.21: getModelViewMatrix),
 * and PostChain#passes is looked up by its SRG name through ObfuscationReflectionHelper, because a plain
 * getDeclaredField("passes") only works in the deobfuscated dev environment.
 */
@Mod.EventBusSubscriber(modid = AlternativeTwilight.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AuroraRenderHooks {
    private static final ResourceLocation CHAIN_ID = AlternativeTwilight.prefix("shaders/post/aurora.json");
    private static final Field PASSES_FIELD = locatePassesField();

    private static PostChain chain;
    private static int chainW = -1;
    private static int chainH = -1;

    private static boolean haveIris;
    private static boolean shaderpackActive;
    private static int irisPollCooldown;

    static {
        haveIris = ModList.get().isLoaded("iris") || ModList.get().isLoaded("oculus");
    }

    private AuroraRenderHooks() {}

    // ─── 资源重载监听（必须挂 mod bus）────────────────────────────────────────
    @Mod.EventBusSubscriber(modid = AlternativeTwilight.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBusBindings {
        private ModBusBindings() {}
        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) AuroraRenderHooks::rebuild);
        }
    }

    private static void rebuild(ResourceManager manager) {
        closeChain();
        if (queryShaderpackInUse()) return; // shaderpack 启用时让位
        Minecraft mc = Minecraft.getInstance();
        if (mc.getMainRenderTarget() == null) return;
        try {
            chain = new PostChain(mc.getTextureManager(), manager, mc.getMainRenderTarget(), CHAIN_ID);
            chainW = chainH = -1;
            resize(mc);
            AlternativeTwilight.LOGGER.info("[aurora] PostChain built");
        } catch (IOException e) {
            AlternativeTwilight.LOGGER.error("[aurora] failed to build PostChain", e);
            closeChain();
        }
    }

    private static void resize(Minecraft mc) {
        if (chain == null) return;
        RenderTarget t = mc.getMainRenderTarget();
        if (t == null) return;
        if (t.width == chainW && t.height == chainH) return;
        chain.resize(t.width, t.height);
        chainW = t.width;
        chainH = t.height;
    }

    private static void closeChain() {
        if (chain != null) {
            try { chain.close(); } catch (Throwable ignored) {}
            chain = null;
        }
        chainW = chainH = -1;
    }

    // ─── 渲染阶段：每帧推 uniform 并执行 PostChain ──────────────────────────────
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        // 每 20 tick poll 一次 Iris 状态
        if (haveIris && --irisPollCooldown <= 0) {
            irisPollCooldown = 20;
            boolean now = queryShaderpackInUse();
            if (now != shaderpackActive) {
                shaderpackActive = now;
                if (now) {
                    closeChain();
                    AlternativeTwilight.LOGGER.info("[aurora] shaderpack active, post chain disabled");
                } else {
                    rebuild(Minecraft.getInstance().getResourceManager());
                }
            }
        }

        if (shaderpackActive) return;
        if (chain == null) return;

        PhantomAuroraState state = PhantomAuroraState.getInstance();
        if (!state.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        resize(mc);

        List<PostPass> passes = readPasses();
        if (passes.isEmpty()) return;

        Matrix4f proj = new Matrix4f(event.getProjectionMatrix());
        Matrix4f invProj = new Matrix4f(proj).invert();
        Matrix4f modelView = new Matrix4f(event.getPoseStack().last().pose());
        Matrix4f invModelView = new Matrix4f(modelView).invert();

        Vec3 cam = event.getCamera().getPosition();
        Vec3 anchor = state.getAnchor();
        float intensity = state.getIntensity();
        float seconds = state.getTimeSeconds(event.getPartialTick());
        float radius = Config.PHANTOM_AURORA_RADIUS.get().floatValue();
        float[] color = parseColor(Config.PHANTOM_AURORA_COLOR_HEX.get(),
                new float[]{0.55f, 0.85f, 1.0f});

        for (PostPass pass : passes) {
            EffectInstance fx = pass.getEffect();
            if (fx == null) continue;
            setMat4(fx, "ProjMat", proj);
            setMat4(fx, "InverseTransformMatrix", invProj);
            setMat4(fx, "InverseModelViewMatrix", invModelView);
            setVec3(fx, "CameraPosition", cam);
            setVec3(fx, "AnchorPosition", anchor);
            setFloat(fx, "AuroraRadius", radius);
            setFloat(fx, "AuroraIntensity", intensity);
            setFloat(fx, "AuroraTime", seconds);
            setVec3(fx, "AuroraColor", color[0], color[1], color[2]);
        }

        chain.process(event.getPartialTick());

        // 关键：恢复主 fbo 绑定，不然紧接着的粒子/手部渲染会画到错误的 fbo 上
        mc.getMainRenderTarget().bindWrite(false);
    }

    // ─── 反射工具：拿 PostChain.passes ─────────────────────────────────────────
    private static List<PostPass> readPasses() {
        if (chain == null || PASSES_FIELD == null) return Collections.emptyList();
        try {
            Object value = PASSES_FIELD.get(chain);
            if (value instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<PostPass> typed = (List<PostPass>) list;
                return typed;
            }
        } catch (IllegalAccessException e) {
            AlternativeTwilight.LOGGER.error("[aurora] failed to read passes", e);
        }
        return Collections.emptyList();
    }

    private static Field locatePassesField() {
        try {
            // f_110009_ = PostChain.passes (SRG name; remapped to "passes" in the dev environment)
            return ObfuscationReflectionHelper.findField(PostChain.class, "f_110009_");
        } catch (ObfuscationReflectionHelper.UnableToFindFieldException e) {
            AlternativeTwilight.LOGGER.error("[aurora] PostChain.passes field not found - shader version mismatch?");
            return null;
        }
    }

    // ─── Iris 探测 ─────────────────────────────────────────────────────────────
    private static boolean queryShaderpackInUse() {
        if (!haveIris) return false;
        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = apiClass.getMethod("getInstance").invoke(null);
            Object active = apiClass.getMethod("isShaderPackInUse").invoke(instance);
            return active instanceof Boolean b && b;
        } catch (Throwable t) {
            return false;
        }
    }

    // ─── Uniform setter ────────────────────────────────────────────────────────
    private static void setMat4(EffectInstance fx, String name, Matrix4f m) {
        Uniform u = fx.getUniform(name);
        if (u != null) u.set(m);
    }

    private static void setVec3(EffectInstance fx, String name, Vec3 v) {
        Uniform u = fx.getUniform(name);
        if (u != null) u.set((float) v.x, (float) v.y, (float) v.z);
    }

    private static void setVec3(EffectInstance fx, String name, float x, float y, float z) {
        Uniform u = fx.getUniform(name);
        if (u != null) u.set(x, y, z);
    }

    private static void setFloat(EffectInstance fx, String name, float v) {
        Uniform u = fx.getUniform(name);
        if (u != null) u.set(v);
    }

    // 解析 "#RRGGBB" → 归一化 RGB float[3]，失败时返回 fallback
    private static float[] parseColor(String hex, float[] fallback) {
        if (hex == null) return fallback;
        String s = hex.trim();
        if (s.startsWith("#")) s = s.substring(1);
        if (s.length() != 6) return fallback;
        try {
            int rgb = Integer.parseInt(s, 16);
            return new float[]{
                    ((rgb >> 16) & 0xFF) / 255f,
                    ((rgb >> 8) & 0xFF) / 255f,
                    (rgb & 0xFF) / 255f
            };
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // 防止"无引用 import"在某些 IDE 里报警
    @SuppressWarnings("unused")
    private static final PackType KEEP_IMPORT = PackType.CLIENT_RESOURCES;
}
