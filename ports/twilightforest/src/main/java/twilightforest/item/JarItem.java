package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.JarBlock;
import twilightforest.block.entity.JarBlockEntity;
import twilightforest.block.entity.MasonJarBlockEntity;
import twilightforest.client.ISTER;

import java.util.List;
import java.util.function.Consumer;

/**
 * Jar items are drawn by the {@link ISTER} with the lid stored in {@code BlockEntityTag.lid}.
 */
public class JarItem extends BlockItem {
	public JarItem(JarBlock block, Properties properties) {
		super(block, properties);
	}

	@Override
	public ItemStack getDefaultInstance() {
		return JarBlockEntity.setLid(super.getDefaultInstance(), this.getBlock().getDefaultLid());
	}

	@Override
	public JarBlock getBlock() {
		return (JarBlock) super.getBlock();
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(ISTER.CLIENT_ITEM_EXTENSION);
	}

	public static class MasonJarItem extends JarItem {
		public MasonJarItem(JarBlock block, Properties properties) {
			super(block, properties);
		}

		@Override
		public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
			super.appendHoverText(stack, level, components, flag);
			ItemStack storedStack = MasonJarBlockEntity.getContents(stack);
			if (!storedStack.isEmpty()) components.add(storedStack.getDisplayName().copy().append(" x" + storedStack.getCount()).withStyle(ChatFormatting.GRAY));
		}

		@Override
		protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
			boolean flag = super.placeBlock(context, state);
			Player player = context.getPlayer();
			if (player != null && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MasonJarBlockEntity jarBlockEntity) {
				jarBlockEntity.setItemRotation(RotationSegment.convertToSegment(player.getYRot() + 180.0F));
			}
			return flag;
		}
	}
}
