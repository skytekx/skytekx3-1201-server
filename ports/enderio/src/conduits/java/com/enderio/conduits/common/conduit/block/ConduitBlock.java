package com.enderio.conduits.common.conduit.block;

import com.enderio.api.conduit.ConduitType;
import com.enderio.api.conduit.facade.FacadeType;
import com.enderio.base.common.config.BaseConfig;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.conduits.client.ConduitBlockClientExtensions;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.conduit.ConduitBundle;
import com.enderio.conduits.common.conduit.ConduitGraphObject;
import com.enderio.conduits.common.conduit.RightClickAction;
import com.enderio.conduits.common.conduit.connection.ConnectionState;
import com.enderio.conduits.common.conduit.connection.DynamicConnectionState;
import com.enderio.conduits.common.conduit.connection.StaticConnectionStates;
import com.enderio.conduits.common.conduit.facades.ConduitFacade;
import com.enderio.conduits.common.conduit.facades.FacadeUtil;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitData;
import com.enderio.conduits.common.init.ConduitBlockEntities;
import com.enderio.conduits.common.init.EIOConduitTypes;
import com.enderio.conduits.common.items.ConduitProbeItem;
import com.enderio.conduits.common.network.ConduitSavedData;
import com.enderio.conduits.common.redstone.RedstoneInsertFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

@Mod.EventBusSubscriber
public class ConduitBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ConduitBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(WATERLOGGED, false));
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.BLOCK;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canBeReplaced(BlockState pState, Fluid pFluid) {
        return false;
    }

    // region Water-logging

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos,
        BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // endregion

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit) {
            conduit.updateConnections(level, pos, fromPos, true);
        }

        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ConduitBlockEntities.CONDUIT.create(pos, state);
    }

    /**
     * A faced bundle is a full block (Ender IO 7.1, 78e627263), except for a player who holds an item that hides the facades: that
     * player aims at the conduits behind it (7de602e53).
     */
    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ConduitBlockEntity conduit) {
            if (conduit.getBundle().hasFacade() && areFacadesVisible(context)) {
                return Shapes.block();
            }

            return conduit.getShape().getTotalShape();
        }
        return Shapes.block();
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit) {
            return conduit.getBundle().hasFacade() ? Shapes.block() : conduit.getShape().getTotalShape();
        }

        return super.getCollisionShape(state, level, pos, context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit && conduit.getBundle().hasFacade()) {
            return Shapes.block();
        }

        return super.getOcclusionShape(state, level, pos);
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(ConduitBlockClientExtensions.INSTANCE);
    }

    private static boolean areFacadesVisible(CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player player) {
            return FacadeUtil.areFacadesVisible(player);
        }

        return true;
    }

    // region Block Interaction

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ConduitBlockEntity conduit) {
            if (!player.getItemInHand(hand).isEmpty()) {
                Optional<InteractionResult> interactionResult = handleProbe(player.getItemInHand(hand));
                if (interactionResult.isPresent()) {
                    return interactionResult.get();
                }
                
                interactionResult = addConduit(conduit, player, player.getItemInHand(hand), level.isClientSide());
                if (interactionResult.isPresent()) {
                    return interactionResult.get();
                }

                interactionResult = handleYeta(conduit, player, player.getItemInHand(hand), hit, level.isClientSide());
                if (interactionResult.isPresent()) {
                    return interactionResult.get();
                }

                interactionResult = handleFacade(conduit, player, player.getItemInHand(hand), level.isClientSide());
                if (interactionResult.isPresent()) {
                    return interactionResult.get();
                }

                if (player.getItemInHand(hand).getItem() instanceof ConduitBlockItem) {
                    return super.use(state, level, pos, player, hand, hit);
                }
            }
            Optional<InteractionResult> interactionResult = handleScreen(conduit, player, hit, level.isClientSide());

            if (interactionResult.isPresent()) {
                return interactionResult.get();
            }
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    private Optional<InteractionResult> handleProbe(ItemStack stack) {
        if (!(stack.getItem() instanceof ConduitProbeItem)) {
            return Optional.empty();
        }

        return Optional.of(InteractionResult.FAIL);
    }

    private Optional<InteractionResult> addConduit(ConduitBlockEntity conduit, Player player, ItemStack stack, boolean isClientSide) {
        if (!(stack.getItem() instanceof ConduitBlockItem conduitBlockItem)) {
            return Optional.empty();
        }

        RightClickAction action = conduit.addType(conduitBlockItem.getType(), player);
        if (!(action instanceof RightClickAction.Blocked)) {
            conduit.getLevel().setBlockAndUpdate(conduit.getBlockPos(), conduit.getBlockState());
        }

        InteractionResult result;

        if (action instanceof RightClickAction.Upgrade upgradeAction) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                player.getInventory().placeItemBackInInventory(upgradeAction.getNotInConduit().getConduitItem().getDefaultInstance());
            }
            result = InteractionResult.sidedSuccess(isClientSide);
        } else if (action instanceof RightClickAction.Insert) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            result = InteractionResult.sidedSuccess(isClientSide);
        } else {
            result = InteractionResult.FAIL;
        }

        if (result != InteractionResult.FAIL) {
            Level level = conduit.getLevel();
            BlockPos blockpos = conduit.getBlockPos();

            BlockState blockState = level.getBlockState(blockpos);
            SoundType soundtype = blockState.getSoundType(level, blockpos, player);
            level.playSound(player, blockpos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F,
                soundtype.getPitch() * 0.8F);
            level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(player, blockState));
        }

        return Optional.of(result);
    }

    public Optional<InteractionResult> handleBlockPlace(BlockPlaceContext context) {
        Level level = context.getLevel();
        @Nullable
        Player player = context.getPlayer();
        BlockPos blockpos = context.getClickedPos();
        ItemStack itemstack = context.getItemInHand();

        if (player != null && level.getBlockEntity(blockpos) instanceof ConduitBlockEntity conduit) {
            return addConduit(conduit, player, itemstack, level.isClientSide);
        }
        return Optional.empty();
    }

    private Optional<InteractionResult> handleYeta(ConduitBlockEntity conduit, Player player, ItemStack stack, BlockHitResult hit, boolean isClientSide) {
        if (stack.is(EIOTags.Items.WRENCH)) {
            @Nullable ConduitType<?> type = conduit.getShape().getConduit(hit.getBlockPos(), hit);
            @Nullable Direction direction = conduit.getShape().getDirection(hit.getBlockPos(), hit);
            if (type == null) {
                return Optional.empty();
            }

            if (isClientSide) {
                return Optional.of(InteractionResult.sidedSuccess(isClientSide));
            }

            if (direction != null) {
                ConnectionState connectionState = conduit.getBundle().getConnectionState(direction, type);
                if (connectionState instanceof DynamicConnectionState dyn) {
                    conduit.getBundle().setConnectionState(direction, type, dyn.withoutConnection());
                    conduit.getBundle().getNodeFor(type).clearState(direction);
                    conduit.updateShape();
                    conduit.updateConnectionToData(type);
                } else {
                    conduit.getBundle().setConnectionState(direction, type, StaticConnectionStates.DISABLED);
                    conduit.updateShape();
                    conduit.updateConnectionToData(type);

                    if (conduit.getLevel().getBlockEntity(conduit.getBlockPos().relative(direction)) instanceof ConduitBlockEntity other) {
                        other.getBundle().setConnectionState(direction.getOpposite(), type, StaticConnectionStates.DISABLED);
                        other.updateShape();
                        other.updateConnectionToData(type);
                        ConduitGraphObject<?> thisNode = conduit.getBundle().getNodeFor(type);
                        ConduitGraphObject<?> otherNode = other.getBundle().getNodeFor(type);
                        thisNode.getGraph().removeSingleEdge(thisNode, otherNode);
                        thisNode.getGraph().removeSingleEdge(otherNode, thisNode);
                        ConduitSavedData.addPotentialGraph(type, thisNode.getGraph(), (ServerLevel) conduit.getLevel());
                        ConduitSavedData.addPotentialGraph(type, otherNode.getGraph(), (ServerLevel) other.getLevel());
                    }
                }
            } else {
                ConnectionState connectionState = conduit.getBundle().getConnectionState(hit.getDirection(), type);
                if (!connectionState.isConnection()) {
                    conduit.tryConnectTo(hit.getDirection(), type, true, true, true);
                }
            }

            return Optional.of(InteractionResult.sidedSuccess(isClientSide));
        }

        return Optional.empty();
    }

    @SubscribeEvent
    public static void handleShiftYeta(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().is(EIOTags.Items.WRENCH)
            && event.getLevel().getBlockEntity(event.getPos()) instanceof ConduitBlockEntity conduit
            && event.getEntity().isSteppingCarefully()) {

            // The wrench takes the facade off before any conduit, also while it shows the conduits behind the facade.
            if (conduit.getBundle().hasFacadeProvider()) {
                Player player = event.getEntity();
                Level level = event.getLevel();
                BlockPos pos = event.getPos();
                Optional<BlockState> facadeState = conduit.getBundle().getFacadeState();
                if (facadeState.isPresent()) {
                    SoundType soundtype = facadeState.get().getSoundType(level, pos, player);
                    level.playSound(player, pos, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F,
                        soundtype.getPitch() * 0.8F);
                    level.addDestroyBlockEffect(pos, facadeState.get());
                }

                if (!level.isClientSide()) {
                    conduit.removeFacade(!player.getAbilities().instabuild);
                    level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, conduit.getBlockState()));
                }

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                return;
            }

            @Nullable ConduitType<?> type = conduit.getShape().getConduit(event.getPos(), event.getHitVec());
            if (type != null) {
                conduit.removeTypeAndDelete(event.getEntity(), type);
                event.setCanceled(true);
            }
        }
    }

    /**
     * Cover the bundle with a painted conduit facade or another mod's facade item (Ender IO 7.1, 78e627263). A bundle takes one facade,
     * so a click on a faced bundle goes on to the conduit screen, as in 8.x. Only the server changes the bundle and the stack, and the
     * bundle and the inventory sync from there, so a click the server refuses (a claim, a cancelled event) leaves no facade behind on the
     * client.
     */
    private Optional<InteractionResult> handleFacade(ConduitBlockEntity conduit, Player player, ItemStack stack, boolean isClientSide) {
        Optional<ConduitFacade> facade = ConduitFacade.of(stack);
        if (facade.isEmpty() || conduit.getBundle().hasFacadeProvider()) {
            return Optional.empty();
        }

        Level level = conduit.getLevel();
        BlockPos pos = conduit.getBlockPos();
        if (!isClientSide) {
            conduit.setFacadeProvider(stack);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, level.getBlockState(pos)));
        }

        SoundType soundtype = facade.get().state().getSoundType(level, pos, player);
        level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

        return Optional.of(InteractionResult.sidedSuccess(isClientSide));
    }

    private Optional<InteractionResult> handleScreen(ConduitBlockEntity conduit, Player player, BlockHitResult hit, boolean isClientSide) {
        Optional<OpenInformation> openInformation = getOpenInformation(conduit, hit);
        if (openInformation.isPresent()) {
            if (player instanceof ServerPlayer serverPlayer) {
                conduit.openMenu(serverPlayer, openInformation.get().direction(), openInformation.get().type());
            }

            return Optional.of(InteractionResult.sidedSuccess(isClientSide));
        }

        return Optional.empty();
    }

    private Optional<OpenInformation> getOpenInformation(ConduitBlockEntity conduit, BlockHitResult hit) {
        @Nullable ConduitType<?> type = conduit.getShape().getConduit(hit.getBlockPos(), hit);
        @Nullable Direction direction = conduit.getShape().getDirection(hit.getBlockPos(), hit);

        if (direction != null && type != null) {
            if (canBeOrIsValidConnection(conduit, type, direction)) {
                return Optional.of(new OpenInformation(direction, type));
            }
        }

        if (type != null) {
            direction = hit.getDirection();
            if (canBeValidConnection(conduit, type, direction)) {
                return Optional.of(new OpenInformation(direction, type));
            }
        }
        if (type != null) {
            for (Direction potential : Direction.values()) {
                if (canBeValidConnection(conduit, type, potential)) {
                    return Optional.of(new OpenInformation(potential, type));
                }
            }
        }
        ConduitBundle bundle = conduit.getBundle();
        //fallback
        for (Direction potential : Direction.values()) {
            if (bundle.isConnectionEnd(potential)) {
                for (ConduitType<?> potentialType : bundle.getTypes()) {
                    if (bundle.getConnectionState(potential, potentialType) instanceof DynamicConnectionState) {
                        return Optional.of(new OpenInformation(potential, potentialType));
                    }
                }
                throw new IllegalStateException("couldn't find connection even though it should be present");
            }
        }
        for (Direction potential : Direction.values()) {
            if (!(conduit.getLevel().getBlockEntity(conduit.getBlockPos().relative(potential)) instanceof ConduitBlockEntity)) {
                for (ConduitType<?> potentialType : bundle.getTypes()) {
                    if (canBeValidConnection(conduit, potentialType, potential)) {
                        return Optional.of(new OpenInformation(potential, potentialType));
                    }
                }
            }
        }

        return Optional.empty();
    }

    // endregion

    public static boolean canBeOrIsValidConnection(ConduitBlockEntity conduit, ConduitType<?> type, Direction direction) {
        ConduitBundle bundle = conduit.getBundle();

        return bundle.getConnectionState(direction, type) instanceof DynamicConnectionState
            || canBeValidConnection(conduit, type, direction);
    }

    public static boolean canBeValidConnection(ConduitBlockEntity conduit, ConduitType<?> type, Direction direction) {
        ConduitBundle bundle = conduit.getBundle();
        ConnectionState connectionState = bundle.getConnectionState(direction, type);
        return connectionState instanceof StaticConnectionStates state
            && state == StaticConnectionStates.DISABLED
            && !(conduit.getLevel().getBlockEntity(conduit.getBlockPos().relative(direction)) instanceof ConduitBlockEntity);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        // Ender IO 7.1 (78e627263): pick block on a visible facade picks the facade's block.
        if (level.getBlockEntity(pos) instanceof ConduitBlockEntity faced && FacadeUtil.areFacadesVisible(player)) {
            Optional<BlockState> facadeState = faced.getBundle().getFacadeState();
            if (facadeState.isPresent()) {
                ItemStack facadeBlock = facadeState.get().getBlock().asItem().getDefaultInstance();
                if (!facadeBlock.isEmpty()) {
                    return facadeBlock;
                }
            }
        }

        if (level instanceof Level realLevel && state.getOptionalValue(BlockStateProperties.WATERLOGGED).orElse(false)) {
            var hitResult = Item.getPlayerPOVHitResult(realLevel, player, ClipContext.Fluid.NONE);
            if (hitResult.getType() == HitResult.Type.MISS) {
                return Items.AIR.getDefaultInstance();
            }

            if (hitResult.getBlockPos().equals(pos)) {
                target = hitResult;
            } else {
                return level.getBlockState(hitResult.getBlockPos()).getCloneItemStack(hitResult, level, hitResult.getBlockPos(), player);
            }
        }

        if (level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit) {
            @Nullable ConduitType<?> type = conduit.getShape().getConduit(pos, target);
            if (type != null) {
                return type.getConduitItem().getDefaultInstance();
            }
        }
        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof ConduitBlockEntity conduitBlockEntity) {
                conduitBlockEntity.everyTick();
            }
        };
    }

    // region Place and destroy logic

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        ConduitBlockItem item = (ConduitBlockItem) stack.getItem();
        if (placer instanceof Player player) {
            if (level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit) {
                conduit.addType(item.getType(), player);
                if (!level.isClientSide()) {
                    conduit.updateClient();
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            // Not a conduit anymore, destroy all the connections
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ConduitBlockEntity conduit) {
                conduit.destroyAllConnections();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        HitResult hit = player.pick(player.getBlockReach() + 5, 1, false);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ConduitBlockEntity conduit) {
            // A visible facade breaks before the conduits behind it (Ender IO 7.1, 78e627263). Its own sound plays (7de602e53).
            Optional<BlockState> facadeState = conduit.getBundle().getFacadeState();
            if (facadeState.isPresent() && FacadeUtil.areFacadesVisible(player)) {
                SoundType soundtype = facadeState.get().getSoundType(level, pos, player);
                level.playSound(player, pos, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F,
                    soundtype.getPitch() * 0.8F);
                level.addDestroyBlockEffect(pos, facadeState.get());

                // The server takes the facade off and syncs the bundle, so a break the server cancels keeps the facade on the client too.
                if (!level.isClientSide()) {
                    conduit.removeFacade(!player.getAbilities().instabuild);
                    level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
                }

                return false;
            }

            @Nullable ConduitType<?> conduitType = conduit.getShape().getConduit(((BlockHitResult) hit).getBlockPos(), hit);
            if (conduitType == null) {
                if (!conduit.getBundle().getTypes().isEmpty()) {
                    level.playSound(player, pos, SoundEvents.GENERIC_SMALL_FALL, SoundSource.BLOCKS, 1F, 1F);
                    return false;
                }
                return true;
            }

            SoundType soundtype = state.getSoundType(level, pos, player);
            level.playSound(player, pos, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

            if (conduit.removeType(conduitType, !player.getAbilities().instabuild)) {
                return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
            }

            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
            return false;
        }

        // No block entity, get rid of it immediately
        return true;
    }

    // endregion

    // region Redstone

    //@formatter:off
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return direction != null
            && level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit
            && conduit.getBundle().getTypes().contains(EIOConduitTypes.REDSTONE.get())
            && conduit.getBundle().getConnectionState(direction.getOpposite(), EIOConduitTypes.REDSTONE.get()) instanceof DynamicConnectionState;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getSignal(BlockState pBlockState, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit
            && conduit.getBundle().getTypes().contains(EIOConduitTypes.REDSTONE.get())
            && conduit.getBundle().getConnectionState(direction.getOpposite(), EIOConduitTypes.REDSTONE.get()) instanceof DynamicConnectionState dyn
            && dyn.isInsert()
            ? getSignalOutput(dyn, conduit.getBundle().getNodeFor(EIOConduitTypes.REDSTONE.get()).getConduitData()) : 0;
    }
    //@formatter:on

    /**
     * Ender IO 8.0 (isStrongOutputSignal): an output side set to strong also puts out its signal as a direct signal, so the
     * block it faces is strongly powered and powers its own neighbours, as a repeater does.
     */
    @SuppressWarnings("deprecation")
    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit
            && conduit.getBundle().getTypes().contains(EIOConduitTypes.REDSTONE.get())) {
            if (conduit.getBundle().getNodeFor(EIOConduitTypes.REDSTONE.get()).getConduitData().isStrong(direction.getOpposite())) {
                return getSignal(state, level, pos, direction);
            }
        }
        return 0;
    }

    private int getSignalOutput(DynamicConnectionState dyn, RedstoneConduitData data) {
        return dyn.filterInsert().getCapability(EIOCapabilities.FILTER)
            .filter(filter -> filter instanceof RedstoneInsertFilter)
            .map(filter -> ((RedstoneInsertFilter) filter).getOutputSignal(data, dyn.insertChannel()))
            .orElse(data.getSignal(dyn.insertChannel()));
    }

    // endregion

    // region Facade Behaviours (Ender IO 7.1, eb53f388b)

    private static Optional<BlockState> getFacadeState(BlockGetter level, BlockPos pos) {
        if (level.getExistingBlockEntity(pos) instanceof ConduitBlockEntity conduit) {
            return conduit.getBundle().getFacadeState();
        }

        return Optional.empty();
    }

    /**
     * The block light engine of a server asks from its own thread, where {@link Level#getChunk} would wait for the main thread. So read
     * the block entity from the chunk the light engine reads.
     */
    @Nullable
    private static ConduitBlockEntity getConduitForLight(BlockGetter level, BlockPos pos) {
        BlockGetter source = level;
        if (level instanceof Level realLevel) {
            LightChunk chunk = realLevel.getChunkSource()
                .getChunkForLighting(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
            if (chunk == null) {
                return null;
            }

            source = chunk;
        }

        return source.getExistingBlockEntity(pos) instanceof ConduitBlockEntity conduit ? conduit : null;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        @Nullable ConduitBlockEntity conduit = getConduitForLight(level, pos);
        if (conduit != null) {
            Optional<BlockState> facadeState = conduit.getBundle().getFacadeState();
            if (facadeState.isPresent()) {
                return getFacadeLight(facadeState.get());
            }
        }

        return super.getLightEmission(state, level, pos);
    }

    /**
     * The light of the facade's state alone. Blocks such as enlightened fused quartz emit through the position aware getLightEmission and
     * set no light level in their properties, so ask that one too, but against an empty world, so the facade block reads nothing from the
     * level off the main thread.
     */
    private static int getFacadeLight(BlockState facade) {
        int light = facade.getLightEmission();
        try {
            light = Math.max(light, facade.getLightEmission(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        } catch (RuntimeException e) {
            // A block that expects its own block entity there keeps the light of its properties, on the light thread too.
        }

        return light;
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        Optional<BlockState> facadeState = getFacadeState(level, pos);
        if (facadeState.isPresent()) {
            return facadeState.get().getFriction(level, pos, entity);
        }

        return super.getFriction(state, level, pos, entity);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        Optional<BlockState> facadeState = getFacadeState(level, pos);
        if (facadeState.isPresent() && (!(entity instanceof Player player) || FacadeUtil.areFacadesVisible(player))) {
            return facadeState.get().getSoundType(level, pos, entity);
        }

        return super.getSoundType(state, level, pos, entity);
    }

    /**
     * A hardened facade makes the bundle resist explosions like the explosion resistant blocks of Ender IO, such as fused quartz. 8.x has
     * the flag but no behaviour for it.
     */
    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        float resistance = super.getExplosionResistance(state, level, pos, explosion);
        if (level.getExistingBlockEntity(pos) instanceof ConduitBlockEntity conduit
            && conduit.getBundle().getFacadeType().map(FacadeType::isBlastResistant).orElse(false)) {
            return Math.max(resistance, BaseConfig.COMMON.BLOCKS.EXPLOSION_RESISTANCE.get().floatValue());
        }

        return resistance;
    }

    /**
     * A hardened facade also keeps the wither and the ender dragon from breaking the bundle, so its tooltip line "resists breaking" holds
     * for them. The mining time stays that of the conduit, as upstream.
     */
    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        if (level.getExistingBlockEntity(pos) instanceof ConduitBlockEntity conduit
            && conduit.getBundle().getFacadeType().map(FacadeType::isBlastResistant).orElse(false)) {
            return false;
        }

        return super.canEntityDestroy(state, level, pos, entity);
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState,
        @Nullable BlockPos queryPos) {
        Optional<BlockState> facadeState = getFacadeState(level, pos);
        if (facadeState.isPresent()) {
            return facadeState.get();
        }

        return super.getAppearance(state, level, pos, side, queryState, queryPos);
    }

    @Override
    public boolean supportsExternalFaceHiding(BlockState state) {
        return true;
    }

    // endregion

    private record OpenInformation(Direction direction, ConduitType<?> type) {}

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {

    }
}
