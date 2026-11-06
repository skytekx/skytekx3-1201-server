package de.ellpeck.actuallyadditions.mod.blocks.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class RenderTypes extends RenderType {
    public RenderTypes(String NameIn, VertexFormat FormatIn, VertexFormat.Mode GLMode, int BufferSize, boolean useDelegate, boolean needsSorting, Runnable setupTask, Runnable ClearTask) {
        super(NameIn, FormatIn, GLMode, BufferSize, useDelegate, needsSorting, setupTask, ClearTask);
    }

/*    public static final RenderType LASER = create("actuallyadditions:laser", DefaultVertexFormats.POSITION_COLOR,
        GL11.GL_QUADS, 256, false, true,
        RenderType.State.builder()
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setTextureState(NO_TEXTURE)
            .setOutputState(RenderType.OUTLINE_TARGET)
            .createCompositeState(true));*/
    public static final RenderType LASER = create("actuallyadditions:laser", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
    VertexFormat.Mode.QUADS, 256, false, true,
    RenderType.CompositeState.builder()
        .setTransparencyState(ADDITIVE_TRANSPARENCY)
        .setTextureState(BLOCK_SHEET)
        .setOutputState(MAIN_TARGET)
        .setLightmapState(RenderStateShard.LIGHTMAP)
        .setCullState(RenderStateShard.NO_CULL)
        // SkyTekx: the block sheet and main target of 8374a4d7 with the position_color shader of
        // db1bb6e7, which shader packs (Oculus on 1.20.1) handle. The base set no shader state
        .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
        .createCompositeState(true));
}
