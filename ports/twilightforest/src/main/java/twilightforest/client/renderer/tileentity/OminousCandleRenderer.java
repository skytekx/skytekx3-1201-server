package twilightforest.client.renderer.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;
import twilightforest.block.OminousCandleBlock;
import twilightforest.block.entity.OminousCandleBlockEntity;

import java.util.List;

/**
 * Draws each candle of an ominous candle as a lit vanilla candle, bobbing up and down. The heights are eased towards
 * {@link OminousCandleBlock#getCurrentY} every frame (e85dd1133).
 */
public class OminousCandleRenderer<T extends OminousCandleBlockEntity> implements BlockEntityRenderer<T> {
	private final Minecraft minecraft;

	protected final BlockRenderDispatcher blockRenderer;

	public OminousCandleRenderer(BlockEntityRendererProvider.Context context) {
		this.blockRenderer = context.getBlockRenderDispatcher();
		this.minecraft = Minecraft.getInstance();
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public void render(T blockEntity, float partialTick, PoseStack stack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		stack.pushPose();
		stack.translate(0.5D, 0.0D, 0.5D);
		stack.mulPose(Axis.YP.rotationDegrees(180.0F));

		BlockState state = blockEntity.getBlockState();

		long time = 0L;
		if (blockEntity.getLevel() != null) time = blockEntity.getLevel().getGameTime();
		BlockState candle = (state.getBlock() instanceof OminousCandleBlock candleBlock ? candleBlock.candle : Blocks.CANDLE).defaultBlockState().setValue(CandleBlock.LIT, true);

		// 4.8 widens BlockRenderDispatcher.blockColors for this; the same instance is public on Minecraft
		int color = this.minecraft.getBlockColors().getColor(state, null, null, 0);
		float r = (float) (color >> 16 & 0xFF) / 255.0F;
		float g = (float) (color >> 8 & 0xFF) / 255.0F;
		float b = (float) (color & 0xFF) / 255.0F;

		BakedModel bakedModel = this.blockRenderer.getBlockModel(candle);

		int candles = state.getValue(OminousCandleBlock.CANDLES);

		List<Vec2> vec2s = OminousCandleBlock.CANDLE_OFFSETS.get(candles);

		for (int i = 0; i < candles; i++) {
			stack.pushPose();
			double targetHeight = OminousCandleBlock.getCurrentY(time, partialTick, blockEntity.getBlockPos(), i + 1);

			double yHeight = this.exponentialDecay(blockEntity.getVisualHeight(i), targetHeight, 0.05);

			blockEntity.setVisualHeightScalar(yHeight, i);

			stack.translate(-vec2s.get(i).x, yHeight, -vec2s.get(i).y);
			// Render types are looked up for the vanilla candle: Forge 47 keys them by block state and the ominous
			// candles have none of their own, so their state would fall back to solid and drop the cutout
			for (RenderType rt : bakedModel.getRenderTypes(candle, RandomSource.create(42), ModelData.EMPTY)) {
				this.blockRenderer.getModelRenderer()
					.renderModel(
						stack.last(),
						buffer.getBuffer(RenderTypeHelper.getEntityRenderType(rt, false)),
						candle,
						bakedModel,
						r,
						g,
						b,
						packedLight,
						packedOverlay,
						ModelData.EMPTY,
						rt
					);
			}
			stack.popPose();
		}
		stack.popPose();
	}

	/**
	 * Yoinked from <a href="https://www.youtube.com/watch?v=LSNQuFEDOyQ&t=2982s">"Lerp smoothing is broken" by Freya</a>
	 * @param prevValue Value from previous frame
	 * @param targetValue Value to reach
	 * @param decayFactor Higher value = faster progression towards targetValue
	 * @return A value towards targetValue in respect to decayFactor
	 */
	private double exponentialDecay(double prevValue, double targetValue, double decayFactor) {
		// 1.21's Timer.getGameTimeDeltaTicks: the length of the last frame in ticks
		return targetValue + (prevValue - targetValue) * Math.exp(-decayFactor * this.minecraft.getDeltaFrameTime());
	}
}
