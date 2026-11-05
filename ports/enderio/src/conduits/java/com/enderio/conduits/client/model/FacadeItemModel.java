package com.enderio.conduits.client.model;

import com.enderio.base.common.util.PaintUtils;
import com.enderio.conduits.client.ConduitClientSetup;
import com.enderio.core.data.model.EIOModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The item model of a conduit facade (Ender IO 7.1, 78e627263). An unpainted facade shows its own model. A painted facade renders the
 * item model of its paint and the facade frame over it. The frame is a texture with a transparent middle, so it gets a translucent pass of
 * its own instead of the paint's layer.
 */
public class FacadeItemModel implements IDynamicBakedModel {

    private final Map<Block, List<BakedModel>> renderPassCache = new HashMap<>();
    private final BakedModel facadeModel;

    public FacadeItemModel(BakedModel facadeModel) {
        this.facadeModel = facadeModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData,
        @Nullable RenderType renderType) {
        return facadeModel.getQuads(state, side, rand, extraData, renderType);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        @Nullable Block paint = PaintUtils.getPaint(itemStack);
        if (paint == null || paint == Blocks.AIR || paint.asItem().getDefaultInstance().isEmpty()) {
            return List.of(this);
        }

        return renderPassCache.computeIfAbsent(paint, key -> List.of(new PaintPass(key), new FramePass()));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return facadeModel.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        // The frame is only known once the models are baked. A mod that asks earlier gets the facade's own transforms.
        @Nullable BakedModel frame = frameModel();
        return frame != null ? frame.getTransforms() : facadeModel.getTransforms();
    }

    @Nullable
    private static BakedModel frameModel() {
        return ConduitClientSetup.modelOf(ConduitClientSetup.CONDUIT_FACADE_OVERLAY);
    }

    /**
     * The quads of the paint's item model, in the paint's own layers.
     */
    private static final class PaintPass implements IDynamicBakedModel {

        private final ItemStack paintStack;

        private PaintPass(Block paint) {
            this.paintStack = paint.asItem().getDefaultInstance();
        }

        private BakedModel paintModel() {
            return Minecraft.getInstance().getItemRenderer().getModel(paintStack, null, null, 0);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData,
            @Nullable RenderType renderType) {
            BakedModel paintModel = paintModel();
            List<BakedQuad> quads = new ArrayList<>();
            for (BakedModel pass : paintModel.getRenderPasses(paintStack, true)) {
                quads.addAll(pass.getQuads(state, side, rand, extraData, renderType));
            }
            return quads;
        }

        @Override
        public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
            return paintModel().getRenderTypes(paintStack, fabulous);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean usesBlockLight() {
            return true;
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return EIOModel.getMissingTexture();
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }
    }

    /**
     * The facade frame, conduit_facade_overlay.
     */
    private static final class FramePass implements IDynamicBakedModel {

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData,
            @Nullable RenderType renderType) {
            @Nullable BakedModel frame = frameModel();
            return frame != null ? frame.getQuads(state, side, rand, extraData, renderType) : List.of();
        }

        @Override
        public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
            return List.of(fabulous ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet());
        }

        @Override
        public boolean useAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean usesBlockLight() {
            return true;
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return EIOModel.getMissingTexture();
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }
    }
}
