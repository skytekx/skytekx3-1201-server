package twilightforest.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import twilightforest.TwilightForestMod;
import twilightforest.init.custom.MagicPaintingVariants;
import twilightforest.util.MagicPaintingVariant;

@OnlyIn(Dist.CLIENT)
public class MagicPaintingTextureManager extends TextureAtlasHolder {
    public final static String MAGIC_PAINTING_PATH = "magic_paintings";
    public static final ResourceLocation ATLAS_LOCATION = TwilightForestMod.prefix("textures/atlas/magic_paintings.png");
    public static final ResourceLocation ATLAS_INFO_LOCATION = new ResourceLocation(MAGIC_PAINTING_PATH);
    public static final ResourceLocation BACK_SPRITE_LOCATION = MagicPaintingVariants.DEFAULT_BACK_TEXTURE;

    public static MagicPaintingTextureManager instance;

    public MagicPaintingTextureManager(TextureManager textureManager) {
        super(textureManager, ATLAS_LOCATION, ATLAS_INFO_LOCATION);
    }

    public TextureAtlasSprite getLayerSprite(ResourceLocation variant, MagicPaintingVariant.Layer layer) {
        return this.getSprite(variant.withPrefix(MAGIC_PAINTING_PATH + "/").withSuffix("/" + layer.path()));
    }

    public TextureAtlasSprite getBackSprite(MagicPaintingVariant variant) {
        return this.getSprite(variant.backTexture());
    }
}