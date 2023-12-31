//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.server.level;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.galliumpowered.event.player.PlayerBreakBlockEvent;
import org.galliumpowered.world.WorldImpl;
import org.galliumpowered.world.entity.PlayerImpl;
import org.galliumpowered.world.block.WorldBlockImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerPlayerGameMode {
    private static final Logger LOGGER = LogManager.getLogger();
    protected ServerLevel level;
    protected final ServerPlayer player;
    private GameType gameModeForPlayer;
    @Nullable
    private GameType previousGameModeForPlayer;
    private boolean isDestroyingBlock;
    private int destroyProgressStart;
    private BlockPos destroyPos;
    private int gameTicks;
    private boolean hasDelayedDestroy;
    private BlockPos delayedDestroyPos;
    private int delayedTickStart;
    private int lastSentState;

    public ServerPlayerGameMode(ServerPlayer serverPlayer) {
        this.gameModeForPlayer = GameType.DEFAULT_MODE;
        this.destroyPos = BlockPos.ZERO;
        this.delayedDestroyPos = BlockPos.ZERO;
        this.lastSentState = -1;
        this.player = serverPlayer;
        this.level = serverPlayer.getLevel();
    }

    public boolean changeGameModeForPlayer(GameType gameType) {
        if (gameType == this.gameModeForPlayer) {
            return false;
        } else {
            this.setGameModeForPlayer(gameType, this.gameModeForPlayer);
            return true;
        }
    }

    protected void setGameModeForPlayer(GameType gameType, @Nullable GameType gameType2) {
        this.previousGameModeForPlayer = gameType2;
        this.gameModeForPlayer = gameType;
        gameType.updatePlayerAbilities(this.player.getAbilities());
        this.player.onUpdateAbilities();
        this.player.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoPacket(Action.UPDATE_GAME_MODE, new ServerPlayer[]{this.player}));
        this.level.updateSleepingPlayerList();
    }

    public GameType getGameModeForPlayer() {
        return this.gameModeForPlayer;
    }

    @Nullable
    public GameType getPreviousGameModeForPlayer() {
        return this.previousGameModeForPlayer;
    }

    public boolean isSurvival() {
        return this.gameModeForPlayer.isSurvival();
    }

    public boolean isCreative() {
        return this.gameModeForPlayer.isCreative();
    }

    public void tick() {
        ++this.gameTicks;
        BlockState blockState2;
        if (this.hasDelayedDestroy) {
            blockState2 = this.level.getBlockState(this.delayedDestroyPos);
            if (blockState2.isAir()) {
                this.hasDelayedDestroy = false;
            } else {
                float f = this.incrementDestroyProgress(blockState2, this.delayedDestroyPos, this.delayedTickStart);
                if (f >= 1.0F) {
                    this.hasDelayedDestroy = false;
                    this.destroyBlock(this.delayedDestroyPos);
                }
            }
        } else if (this.isDestroyingBlock) {
            blockState2 = this.level.getBlockState(this.destroyPos);
            if (blockState2.isAir()) {
                this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                this.lastSentState = -1;
                this.isDestroyingBlock = false;
            } else {
                this.incrementDestroyProgress(blockState2, this.destroyPos, this.destroyProgressStart);
            }
        }

    }

    private float incrementDestroyProgress(BlockState blockState, BlockPos blockPos, int i) {
        int j = this.gameTicks - i;
        float f = blockState.getDestroyProgress(this.player, this.player.level, blockPos) * (float)(j + 1);
        int k = (int)(f * 10.0F);
        if (k != this.lastSentState) {
            this.level.destroyBlockProgress(this.player.getId(), blockPos, k);
            this.lastSentState = k;
        }

        return f;
    }

    public void handleBlockBreakAction(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i) {
        double d = this.player.getX() - ((double)blockPos.getX() + 0.5);
        double e = this.player.getY() - ((double)blockPos.getY() + 0.5) + 1.5;
        double f = this.player.getZ() - ((double)blockPos.getZ() + 0.5);
        double g = d * d + e * e + f * f;
        if (g > 36.0) {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(blockPos, this.level.getBlockState(blockPos), action, false, "too far"));
        } else if (blockPos.getY() >= i) {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(blockPos, this.level.getBlockState(blockPos), action, false, "too high"));
        } else {
            BlockState blockState;
            if (action == net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                if (!this.level.mayInteract(this.player, blockPos)) {
                    this.player.connection.send(new ClientboundBlockBreakAckPacket(blockPos, this.level.getBlockState(blockPos), action, false, "may not interact"));
                    return;
                }

                if (this.isCreative()) {
                    this.destroyAndAck(blockPos, action, "creative destroy");
                    return;
                }

                if (this.player.blockActionRestricted(this.level, blockPos, this.gameModeForPlayer)) {
                    this.player.connection.send(new ClientboundBlockBreakAckPacket(blockPos, this.level.getBlockState(blockPos), action, false, "block action restricted"));
                    return;
                }

                this.destroyProgressStart = this.gameTicks;
                float h = 1.0F;
                blockState = this.level.getBlockState(blockPos);
                if (!blockState.isAir()) {
                    blockState.attack(this.level, blockPos, this.player);
                    h = blockState.getDestroyProgress(this.player, this.player.level, blockPos);
                }

                if (!blockState.isAir() && h >= 1.0F) {
                    this.destroyAndAck(blockPos, action, "insta mine");
                } else {
                    if (this.isDestroyingBlock) {
                        this.player.connection.send(new ClientboundBlockBreakAckPacket(this.destroyPos, this.level.getBlockState(this.destroyPos), net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, false, "abort destroying since another started (client insta mine, server disagreed)"));
                    }

                    this.isDestroyingBlock = true;
                    this.destroyPos = blockPos.immutable();
                    int j = (int)(h * 10.0F);
                    this.level.destroyBlockProgress(this.player.getId(), blockPos, j);
                    this.player.connection.send(new ClientboundBlockBreakAckPacket(blockPos, this.level.getBlockState(blockPos), action, true, "actual start of destroying"));
                    this.lastSentState = j;
                }
            } else if (action == net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
                if (blockPos.equals(this.destroyPos)) {
                    int k = this.gameTicks - this.destroyProgressStart;
                    blockState = this.level.getBlockState(blockPos);
                    if (!blockState.isAir()) {
                        float l = blockState.getDestroyProgress(this.player, this.player.level, blockPos) * (float)(k + 1);
                        if (l >= 0.7F) {
                            this.isDestroyingBlock = false;
                            this.level.destroyBlockProgress(this.player.getId(), blockPos, -1);
                            this.destroyAndAck(blockPos, action, "destroyed");
                            return;
                        }

                        if (!this.hasDelayedDestroy) {
                            this.isDestroyingBlock = false;
                            this.hasDelayedDestroy = true;
                            this.delayedDestroyPos = blockPos;
                            this.delayedTickStart = this.destroyProgressStart;
                        }
                    }
                }

                this.player.connection.send(new ClientboundBlockBreakAckPacket(blockPos, this.level.getBlockState(blockPos), action, true, "stopped destroying"));
            } else if (action == net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                this.isDestroyingBlock = false;
                if (!Objects.equals(this.destroyPos, blockPos)) {
                    LOGGER.warn("Mismatch in destroy block pos: {} {}", this.destroyPos, blockPos);
                    this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                    this.player.connection.send(new ClientboundBlockBreakAckPacket(this.destroyPos, this.level.getBlockState(this.destroyPos), action, true, "aborted mismatched destroying"));
                }

                this.level.destroyBlockProgress(this.player.getId(), blockPos, -1);
                this.player.connection.send(new ClientboundBlockBreakAckPacket(blockPos, this.level.getBlockState(blockPos), action, true, "aborted destroying"));
            }

        }
    }

    public void destroyAndAck(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, String string) {
        if (this.destroyBlock(blockPos)) {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(blockPos, this.level.getBlockState(blockPos), action, true, string));
        } else {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(blockPos, this.level.getBlockState(blockPos), action, false, string));
        }

    }

    public boolean destroyBlock(BlockPos blockPos) {
        BlockState blockState = this.level.getBlockState(blockPos);
        if (!this.player.getMainHandItem().getItem().canAttackBlock(blockState, this.level, blockPos, this.player)) {
            return false;
        } else {
            BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
            Block block = blockState.getBlock();
            if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
                this.level.sendBlockUpdated(blockPos, blockState, blockState, 3);
                return false;
            } else if (this.player.blockActionRestricted(this.level, blockPos, this.gameModeForPlayer)) {
                return false;
            } else {
                block.playerWillDestroy(this.level, blockPos, blockState, this.player);
                boolean bl = this.level.removeBlock(blockPos, false);
                if (bl) {
                    // Gallium start: block break event
                    PlayerBreakBlockEvent event = (PlayerBreakBlockEvent) new PlayerBreakBlockEvent(new PlayerImpl(player), new WorldBlockImpl(block, blockPos, new WorldImpl(level))).call();
                    if (!event.isCancelled) {
                        block.destroy(this.level, blockPos, blockState);
                    }
                    // Gallium end
                }

                if (this.isCreative()) {
                    return true;
                } else {
                    ItemStack itemStack = this.player.getMainHandItem();
                    ItemStack itemStack2 = itemStack.copy();
                    boolean bl2 = this.player.hasCorrectToolForDrops(blockState);
                    itemStack.mineBlock(this.level, blockState, blockPos, this.player);
                    if (bl && bl2) {
                        block.playerDestroy(this.level, this.player, blockPos, blockState, blockEntity, itemStack2);
                    }

                    return true;
                }
            }
        }
    }

    public InteractionResult useItem(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand) {
        if (this.gameModeForPlayer == GameType.SPECTATOR) {
            return InteractionResult.PASS;
        } else if (serverPlayer.getCooldowns().isOnCooldown(itemStack.getItem())) {
            return InteractionResult.PASS;
        } else {
            int i = itemStack.getCount();
            int j = itemStack.getDamageValue();
            InteractionResultHolder<ItemStack> interactionResultHolder = itemStack.use(level, serverPlayer, interactionHand);
            ItemStack itemStack2 = (ItemStack)interactionResultHolder.getObject();
            if (itemStack2 == itemStack && itemStack2.getCount() == i && itemStack2.getUseDuration() <= 0 && itemStack2.getDamageValue() == j) {
                return interactionResultHolder.getResult();
            } else if (interactionResultHolder.getResult() == InteractionResult.FAIL && itemStack2.getUseDuration() > 0 && !serverPlayer.isUsingItem()) {
                return interactionResultHolder.getResult();
            } else {
                serverPlayer.setItemInHand(interactionHand, itemStack2);
                if (this.isCreative()) {
                    itemStack2.setCount(i);
                    if (itemStack2.isDamageableItem() && itemStack2.getDamageValue() != j) {
                        itemStack2.setDamageValue(j);
                    }
                }

                if (itemStack2.isEmpty()) {
                    serverPlayer.setItemInHand(interactionHand, ItemStack.EMPTY);
                }

                if (!serverPlayer.isUsingItem()) {
                    serverPlayer.inventoryMenu.sendAllDataToRemote();
                }

                return interactionResultHolder.getResult();
            }
        }
    }

    public InteractionResult useItemOn(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (this.gameModeForPlayer == GameType.SPECTATOR) {
            MenuProvider menuProvider = blockState.getMenuProvider(level, blockPos);
            if (menuProvider != null) {
                serverPlayer.openMenu(menuProvider);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        } else {
            boolean bl = !serverPlayer.getMainHandItem().isEmpty() || !serverPlayer.getOffhandItem().isEmpty();
            boolean bl2 = serverPlayer.isSecondaryUseActive() && bl;
            ItemStack itemStack2 = itemStack.copy();
            if (!bl2) {
                InteractionResult interactionResult = blockState.use(level, serverPlayer, interactionHand, blockHitResult);
                if (interactionResult.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack2);
                    return interactionResult;
                }
            }

            if (!itemStack.isEmpty() && !serverPlayer.getCooldowns().isOnCooldown(itemStack.getItem())) {
                UseOnContext useOnContext = new UseOnContext(serverPlayer, interactionHand, blockHitResult);
                InteractionResult interactionResult3;
                if (this.isCreative()) {
                    int i = itemStack.getCount();
                    interactionResult3 = itemStack.useOn(useOnContext);
                    itemStack.setCount(i);
                } else {
                    interactionResult3 = itemStack.useOn(useOnContext);
                }

                if (interactionResult3.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack2);
                }

                return interactionResult3;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public void setLevel(ServerLevel serverLevel) {
        this.level = serverLevel;
    }
}
