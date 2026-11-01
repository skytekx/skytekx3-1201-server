package twilightforest.block.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFSounds;

public class KeepsakeCasketBlockEntity extends SkullChestBlockEntity {

	public KeepsakeCasketBlockEntity(BlockPos pos, BlockState state) {
		super(TFBlockEntities.KEEPSAKE_CASKET.get(), pos, state);
	}

	@Override
	protected SoundEvent getOpenSound() {
		return TFSounds.CASKET_OPEN.get();
	}

	@Override
	protected SoundEvent getCloseSound() {
		return TFSounds.CASKET_CLOSE.get();
	}

	@Override
	public void displayLockedInfo(Player player) {
		player.playNotifySound(TFSounds.CASKET_LOCKED.get(), SoundSource.BLOCKS, 0.5F, 0.5F);
		player.displayClientMessage(Component.translatable("block.twilightforest.casket.locked", this.getOwnerName()).withStyle(ChatFormatting.RED), true);
	}
}
