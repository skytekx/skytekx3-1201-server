package twilightforest.client.model.block;

import com.google.common.base.MoreObjects;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import twilightforest.block.entity.ReactorDebrisBlockEntity;
import twilightforest.client.renderer.tileentity.ReactorDebrisRenderer;

/**
 * Stands in for the reactor debris block model so break particles use one of the debris' face textures
 * (backported from 4.8, 67bbcd9fb).
 */
public class ReactorDebrisModel extends BakedModelWrapper<BakedModel> {
	public static final ModelProperty<ResourceLocation> TEXTURE_FOR_PARTICLE = new ModelProperty<>();

	public ReactorDebrisModel(BakedModel defaultModel) {
		super(defaultModel);
	}

	@Override
	public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
		if (!(level.getBlockEntity(pos) instanceof ReactorDebrisBlockEntity reactorDebrisBlockEntity)
			|| !(level instanceof ClientLevel clientLevel))
			return modelData.derive().with(TEXTURE_FOR_PARTICLE, ReactorDebrisBlockEntity.DEFAULT_TEXTURE).build();
		final ResourceLocation textureForParticle = reactorDebrisBlockEntity.textures[clientLevel.random.nextInt(reactorDebrisBlockEntity.textures.length)];
		return modelData.derive().with(TEXTURE_FOR_PARTICLE, textureForParticle).build();
	}

	@Override
	public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
		ResourceLocation texturePath = MoreObjects.firstNonNull(data.get(TEXTURE_FOR_PARTICLE), ReactorDebrisBlockEntity.DEFAULT_TEXTURE);
		return ReactorDebrisRenderer.getSprite(texturePath);
	}
}
