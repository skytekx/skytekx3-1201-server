package baguchi.spiced_twilight.client.render;

import baguchi.spiced_twilight.SpicedTwilight;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import twilightforest.client.model.entity.SlimeBeetleModel;
import twilightforest.client.renderer.entity.SlimeBeetleRenderer;
import twilightforest.entity.monster.SlimeBeetle;

public class MazeSlimeBeetleRenderer extends SlimeBeetleRenderer {
    private static final ResourceLocation TEXTURE = SpicedTwilight.prefix("textures/entity/maze_slime_beetle.png");


    public MazeSlimeBeetleRenderer(EntityRendererProvider.Context context, SlimeBeetleModel model, float shadowSize) {
        super(context, model, shadowSize);
    }

    @Override
    public ResourceLocation getTextureLocation(SlimeBeetle entity) {
        return TEXTURE;
    }
}
