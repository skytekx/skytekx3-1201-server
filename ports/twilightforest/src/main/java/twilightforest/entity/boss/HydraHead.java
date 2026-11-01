package twilightforest.entity.boss;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import twilightforest.TwilightForestMod;

public class HydraHead extends HydraPart {

	public static final ResourceLocation RENDERER = TwilightForestMod.prefix("hydra_head");

	private static final EntityDataAccessor<Float> DATA_MOUTH_POSITION = SynchedEntityData.defineId(HydraHead.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_MOUTH_POSITION_LAST = SynchedEntityData.defineId(HydraHead.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(HydraHead.class, EntityDataSerializers.BYTE);

	public HydraHead(Hydra hydra) {
		super(hydra, 4F, 4F);
	}

	@OnlyIn(Dist.CLIENT)
	public ResourceLocation renderer() {
		return RENDERER;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.getEntityData().define(DATA_MOUTH_POSITION, 0F);
		this.getEntityData().define(DATA_MOUTH_POSITION_LAST, 0F);
		this.getEntityData().define(DATA_STATE, (byte) 0);
	}

	public float getMouthOpen() {
		return this.getEntityData().get(DATA_MOUTH_POSITION);
	}

	public float getMouthOpenLast() {
		return this.getEntityData().get(DATA_MOUTH_POSITION_LAST);
	}

	public HydraHeadContainer.State getState() {
		return HydraHeadContainer.State.values()[this.getEntityData().get(DATA_STATE)];
	}

	public void setMouthOpen(float openness) {
		this.getEntityData().set(DATA_MOUTH_POSITION_LAST, getMouthOpen());
		this.getEntityData().set(DATA_MOUTH_POSITION, openness);
	}

	public void setState(HydraHeadContainer.State state) {
		this.getEntityData().set(DATA_STATE, (byte) state.ordinal());
	}

	// a Name Tag only works on LivingEntities, so heads take the name themselves.
	// Not on a dying head: its parts stay pickable for a while after the name was cleared, and the name would carry over to whichever
	// head respawns in that slot. DATA_STATE is synced, so both sides agree.
	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.is(Items.NAME_TAG) && stack.hasCustomHoverName() && this.getState() != HydraHeadContainer.State.DYING && this.getState() != HydraHeadContainer.State.DEAD) {
			if (!this.level().isClientSide() && this.isAlive()) {
				this.setCustomName(stack.getHoverName());

				//save name to main hydra
				Hydra hydra = this.getParent();
				if (hydra != null) {
					for (int i = 0; i < hydra.numHeads; i++) {
						if (hydra.hc[i].headEntity == this) {
							hydra.setHeadNameFor(i, stack.getHoverName().getString());
						}
					}
				}
				stack.shrink(1);
			}

			return InteractionResult.sidedSuccess(this.level().isClientSide());
		}
		return super.interact(player, hand);
	}
}
