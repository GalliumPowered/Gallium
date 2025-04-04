package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;

import java.util.*;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.Team.Visibility;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galliumpowered.event.player.PlayerDeathEvent;
import org.galliumpowered.world.entity.PlayerImpl;

public class ServerPlayer extends Player {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_XZ = 32;
    private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_Y = 10;
    public ServerGamePacketListenerImpl connection;
    public final MinecraftServer server;
    public final ServerPlayerGameMode gameMode;
    private final PlayerAdvancements advancements;
    private final ServerStatsCounter stats;
    private float lastRecordedHealthAndAbsorption = Float.MIN_VALUE;
    private int lastRecordedFoodLevel = Integer.MIN_VALUE;
    private int lastRecordedAirLevel = Integer.MIN_VALUE;
    private int lastRecordedArmor = Integer.MIN_VALUE;
    private int lastRecordedLevel = Integer.MIN_VALUE;
    private int lastRecordedExperience = Integer.MIN_VALUE;
    private float lastSentHealth = -1.0E8F;
    private int lastSentFood = -99999999;
    private boolean lastFoodSaturationZero = true;
    private int lastSentExp = -99999999;
    private int spawnInvulnerableTime = 60;
    private ChatVisiblity chatVisibility;
    private boolean canChatColor;
    private long lastActionTime;
    private Entity camera;
    private boolean isChangingDimension;
    private boolean seenCredits;
    private final ServerRecipeBook recipeBook;
    private Vec3 levitationStartPos;
    private int levitationStartTime;
    private boolean disconnected;
    @Nullable
    private Vec3 enteredNetherPosition;
    private SectionPos lastSectionPos;
    private ResourceKey<Level> respawnDimension;
    @Nullable
    private BlockPos respawnPosition;
    private boolean respawnForced;
    private float respawnAngle;
    private final TextFilter textFilter;
    private boolean textFilteringEnabled;
    private final ContainerSynchronizer containerSynchronizer;
    private final ContainerListener containerListener;
    private int containerCounter;
    public int latency;
    public boolean wonGame;

    public ServerPlayer(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile) {
        super(serverLevel, serverLevel.getSharedSpawnPos(), serverLevel.getSharedSpawnAngle(), gameProfile);
        this.chatVisibility = ChatVisiblity.FULL;
        this.canChatColor = true;
        this.lastActionTime = Util.getMillis();
        this.recipeBook = new ServerRecipeBook();
        this.lastSectionPos = SectionPos.of(0, 0, 0);
        this.respawnDimension = Level.OVERWORLD;
        this.containerSynchronizer = new ContainerSynchronizer() {
            public void sendInitialData(AbstractContainerMenu abstractContainerMenu, NonNullList<ItemStack> nonNullList, ItemStack itemStack, int[] is) {
                ServerPlayer.this.connection.send(new ClientboundContainerSetContentPacket(abstractContainerMenu.containerId, abstractContainerMenu.incrementStateId(), nonNullList, itemStack));

                for(int i = 0; i < is.length; ++i) {
                    this.broadcastDataValue(abstractContainerMenu, i, is[i]);
                }

            }

            public void sendSlotChange(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
                ServerPlayer.this.connection.send(new ClientboundContainerSetSlotPacket(abstractContainerMenu.containerId, abstractContainerMenu.incrementStateId(), i, itemStack));
            }

            public void sendCarriedChange(AbstractContainerMenu abstractContainerMenu, ItemStack itemStack) {
                ServerPlayer.this.connection.send(new ClientboundContainerSetSlotPacket(-1, abstractContainerMenu.incrementStateId(), -1, itemStack));
            }

            public void sendDataChange(AbstractContainerMenu abstractContainerMenu, int i, int j) {
                this.broadcastDataValue(abstractContainerMenu, i, j);
            }

            private void broadcastDataValue(AbstractContainerMenu abstractContainerMenu, int i, int j) {
                ServerPlayer.this.connection.send(new ClientboundContainerSetDataPacket(abstractContainerMenu.containerId, i, j));
            }
        };
        this.containerListener = new ContainerListener() {
            public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
                Slot slot = abstractContainerMenu.getSlot(i);
                if (!(slot instanceof ResultSlot)) {
                    if (slot.container == ServerPlayer.this.getInventory()) {
                        CriteriaTriggers.INVENTORY_CHANGED.trigger(ServerPlayer.this, ServerPlayer.this.getInventory(), itemStack);
                    }

                }
            }

            public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
            }
        };
        this.textFilter = minecraftServer.createTextFilterForPlayer(this);
        this.gameMode = minecraftServer.createGameModeForPlayer(this);
        this.server = minecraftServer;
        this.stats = minecraftServer.getPlayerList().getPlayerStats(this);
        this.advancements = minecraftServer.getPlayerList().getPlayerAdvancements(this);
        this.maxUpStep = 1.0F;
        this.fudgeSpawnLocation(serverLevel);
    }

    private void fudgeSpawnLocation(ServerLevel serverLevel) {
        BlockPos blockPos = serverLevel.getSharedSpawnPos();
        if (serverLevel.dimensionType().hasSkyLight() && serverLevel.getServer().getWorldData().getGameType() != GameType.ADVENTURE) {
            int i = Math.max(0, this.server.getSpawnRadius(serverLevel));
            int j = Mth.floor(serverLevel.getWorldBorder().getDistanceToBorder((double)blockPos.getX(), (double)blockPos.getZ()));
            if (j < i) {
                i = j;
            }

            if (j <= 1) {
                i = 1;
            }

            long l = (long)(i * 2 + 1);
            long m = l * l;
            int k = m > 2147483647L ? Integer.MAX_VALUE : (int)m;
            int n = this.getCoprime(k);
            int o = (new Random()).nextInt(k);

            for(int p = 0; p < k; ++p) {
                int q = (o + n * p) % k;
                int r = q % (i * 2 + 1);
                int s = q / (i * 2 + 1);
                BlockPos blockPos2 = PlayerRespawnLogic.getOverworldRespawnPos(serverLevel, blockPos.getX() + r - i, blockPos.getZ() + s - i, false);
                if (blockPos2 != null) {
                    this.moveTo(blockPos2, 0.0F, 0.0F);
                    if (serverLevel.noCollision(this)) {
                        break;
                    }
                }
            }
        } else {
            this.moveTo(blockPos, 0.0F, 0.0F);

            while(!serverLevel.noCollision(this) && this.getY() < (double)(serverLevel.getMaxBuildHeight() - 1)) {
                this.setPos(this.getX(), this.getY() + 1.0, this.getZ());
            }
        }

    }

    private int getCoprime(int i) {
        return i <= 16 ? i - 1 : 17;
    }

    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("enteredNetherPosition", 10)) {
            CompoundTag compoundTag2 = compoundTag.getCompound("enteredNetherPosition");
            this.enteredNetherPosition = new Vec3(compoundTag2.getDouble("x"), compoundTag2.getDouble("y"), compoundTag2.getDouble("z"));
        }

        this.seenCredits = compoundTag.getBoolean("seenCredits");
        if (compoundTag.contains("recipeBook", 10)) {
            this.recipeBook.fromNbt(compoundTag.getCompound("recipeBook"), this.server.getRecipeManager());
        }

        if (this.isSleeping()) {
            this.stopSleeping();
        }

        if (compoundTag.contains("SpawnX", 99) && compoundTag.contains("SpawnY", 99) && compoundTag.contains("SpawnZ", 99)) {
            this.respawnPosition = new BlockPos(compoundTag.getInt("SpawnX"), compoundTag.getInt("SpawnY"), compoundTag.getInt("SpawnZ"));
            this.respawnForced = compoundTag.getBoolean("SpawnForced");
            this.respawnAngle = compoundTag.getFloat("SpawnAngle");
            if (compoundTag.contains("SpawnDimension")) {
                DataResult var10001 = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, compoundTag.get("SpawnDimension"));
                Logger var10002 = LOGGER;
                Objects.requireNonNull(var10002);
                this.respawnDimension = (ResourceKey)var10001.resultOrPartial(var10002::error).orElse(Level.OVERWORLD);
            }
        }

    }

    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.storeGameTypes(compoundTag);
        compoundTag.putBoolean("seenCredits", this.seenCredits);
        if (this.enteredNetherPosition != null) {
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.putDouble("x", this.enteredNetherPosition.x);
            compoundTag2.putDouble("y", this.enteredNetherPosition.y);
            compoundTag2.putDouble("z", this.enteredNetherPosition.z);
            compoundTag.put("enteredNetherPosition", compoundTag2);
        }

        Entity entity = this.getRootVehicle();
        Entity entity2 = this.getVehicle();
        if (entity2 != null && entity != this && entity.hasExactlyOnePlayerPassenger()) {
            CompoundTag compoundTag3 = new CompoundTag();
            CompoundTag compoundTag4 = new CompoundTag();
            entity.save(compoundTag4);
            compoundTag3.putUUID("Attach", entity2.getUUID());
            compoundTag3.put("Entity", compoundTag4);
            compoundTag.put("RootVehicle", compoundTag3);
        }

        compoundTag.put("recipeBook", this.recipeBook.toNbt());
        compoundTag.putString("Dimension", this.level.dimension().location().toString());
        if (this.respawnPosition != null) {
            compoundTag.putInt("SpawnX", this.respawnPosition.getX());
            compoundTag.putInt("SpawnY", this.respawnPosition.getY());
            compoundTag.putInt("SpawnZ", this.respawnPosition.getZ());
            compoundTag.putBoolean("SpawnForced", this.respawnForced);
            compoundTag.putFloat("SpawnAngle", this.respawnAngle);
            DataResult var10000 = ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, this.respawnDimension.location());
            Logger var10001 = LOGGER;
            Objects.requireNonNull(var10001);
            var10000.resultOrPartial(var10001::error).ifPresent((tag) -> {
                compoundTag.put("SpawnDimension", (Tag) tag);
            });
        }

    }

    public void setExperiencePoints(int i) {
        float f = (float)this.getXpNeededForNextLevel();
        float g = (f - 1.0F) / f;
        this.experienceProgress = Mth.clamp((float)i / f, 0.0F, g);
        this.lastSentExp = -1;
    }

    public void setExperienceLevels(int i) {
        this.experienceLevel = i;
        this.lastSentExp = -1;
    }

    public void giveExperienceLevels(int i) {
        super.giveExperienceLevels(i);
        this.lastSentExp = -1;
    }

    public void onEnchantmentPerformed(ItemStack itemStack, int i) {
        super.onEnchantmentPerformed(itemStack, i);
        this.lastSentExp = -1;
    }

    private void initMenu(AbstractContainerMenu abstractContainerMenu) {
        abstractContainerMenu.addSlotListener(this.containerListener);
        abstractContainerMenu.setSynchronizer(this.containerSynchronizer);
    }

    public void initInventoryMenu() {
        this.initMenu(this.inventoryMenu);
    }

    public void onEnterCombat() {
        super.onEnterCombat();
        this.connection.send(new ClientboundPlayerCombatEnterPacket());
    }

    public void onLeaveCombat() {
        super.onLeaveCombat();
        this.connection.send(new ClientboundPlayerCombatEndPacket(this.getCombatTracker()));
    }

    protected void onInsideBlock(BlockState blockState) {
        CriteriaTriggers.ENTER_BLOCK.trigger(this, blockState);
    }

    protected ItemCooldowns createItemCooldowns() {
        return new ServerItemCooldowns(this);
    }

    public void tick() {
        this.gameMode.tick();
        --this.spawnInvulnerableTime;
        if (this.invulnerableTime > 0) {
            --this.invulnerableTime;
        }

        this.containerMenu.broadcastChanges();
        if (!this.level.isClientSide && !this.containerMenu.stillValid(this)) {
            this.closeContainer();
            this.containerMenu = this.inventoryMenu;
        }

        Entity entity = this.getCamera();
        if (entity != this) {
            if (entity.isAlive()) {
                this.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                this.getLevel().getChunkSource().move(this);
                if (this.wantsToStopRiding()) {
                    this.setCamera(this);
                }
            } else {
                this.setCamera(this);
            }
        }

        CriteriaTriggers.TICK.trigger(this);
        if (this.levitationStartPos != null) {
            CriteriaTriggers.LEVITATION.trigger(this, this.levitationStartPos, this.tickCount - this.levitationStartTime);
        }

        this.advancements.flushDirty(this);
    }

    public void doTick() {
        try {
            if (!this.isSpectator() || !this.touchingUnloadedChunk()) {
                super.tick();
            }

            for(int i = 0; i < this.getInventory().getContainerSize(); ++i) {
                ItemStack itemStack = this.getInventory().getItem(i);
                if (itemStack.getItem().isComplex()) {
                    Packet<?> packet = ((ComplexItem)itemStack.getItem()).getUpdatePacket(itemStack, this.level, this);
                    if (packet != null) {
                        this.connection.send(packet);
                    }
                }
            }

            if (this.getHealth() != this.lastSentHealth || this.lastSentFood != this.foodData.getFoodLevel() || this.foodData.getSaturationLevel() == 0.0F != this.lastFoodSaturationZero) {
                this.connection.send(new ClientboundSetHealthPacket(this.getHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
                this.lastSentHealth = this.getHealth();
                this.lastSentFood = this.foodData.getFoodLevel();
                this.lastFoodSaturationZero = this.foodData.getSaturationLevel() == 0.0F;
            }

            if (this.getHealth() + this.getAbsorptionAmount() != this.lastRecordedHealthAndAbsorption) {
                this.lastRecordedHealthAndAbsorption = this.getHealth() + this.getAbsorptionAmount();
                this.updateScoreForCriteria(ObjectiveCriteria.HEALTH, Mth.ceil(this.lastRecordedHealthAndAbsorption));
            }

            if (this.foodData.getFoodLevel() != this.lastRecordedFoodLevel) {
                this.lastRecordedFoodLevel = this.foodData.getFoodLevel();
                this.updateScoreForCriteria(ObjectiveCriteria.FOOD, Mth.ceil((float)this.lastRecordedFoodLevel));
            }

            if (this.getAirSupply() != this.lastRecordedAirLevel) {
                this.lastRecordedAirLevel = this.getAirSupply();
                this.updateScoreForCriteria(ObjectiveCriteria.AIR, Mth.ceil((float)this.lastRecordedAirLevel));
            }

            if (this.getArmorValue() != this.lastRecordedArmor) {
                this.lastRecordedArmor = this.getArmorValue();
                this.updateScoreForCriteria(ObjectiveCriteria.ARMOR, Mth.ceil((float)this.lastRecordedArmor));
            }

            if (this.totalExperience != this.lastRecordedExperience) {
                this.lastRecordedExperience = this.totalExperience;
                this.updateScoreForCriteria(ObjectiveCriteria.EXPERIENCE, Mth.ceil((float)this.lastRecordedExperience));
            }

            if (this.experienceLevel != this.lastRecordedLevel) {
                this.lastRecordedLevel = this.experienceLevel;
                this.updateScoreForCriteria(ObjectiveCriteria.LEVEL, Mth.ceil((float)this.lastRecordedLevel));
            }

            if (this.totalExperience != this.lastSentExp) {
                this.lastSentExp = this.totalExperience;
                this.connection.send(new ClientboundSetExperiencePacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
            }

            if (this.tickCount % 20 == 0) {
                CriteriaTriggers.LOCATION.trigger(this);
            }

        } catch (Throwable var4) {
            CrashReport crashReport = CrashReport.forThrowable(var4, "Ticking player");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Player being ticked");
            this.fillCrashReportCategory(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    private void updateScoreForCriteria(ObjectiveCriteria objectiveCriteria, int i) {
        this.getScoreboard().forAllObjectives(objectiveCriteria, this.getScoreboardName(), (score) -> {
            score.setScore(i);
        });
    }

    public void die(DamageSource damageSource) {
        // the part where he kills you!
        // Gallium start: event

        String[] deathMessage = damageSource.getLocalizedDeathMessage(this).getString().split(" ");
        String finalDeathMessage = String.join(" ", Arrays.copyOfRange(deathMessage, 1, deathMessage.length));


        PlayerDeathEvent event = (PlayerDeathEvent) new PlayerDeathEvent(new PlayerImpl(this), finalDeathMessage).call();

        if (event.isCancelled) {
            return;
        }

        // Gallium end
        boolean bl = this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
        if (bl) {
            Component component = this.getCombatTracker().getDeathMessage();
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getCombatTracker(), component), (future) -> {
                if (!future.isSuccess()) {
                    boolean i = true;
                    String string = component.getString(256);
                    Component component2 = new TranslatableComponent("death.attack.message_too_long", new Object[]{(new TextComponent(string)).withStyle(ChatFormatting.YELLOW)});
                    Component component3 = (new TranslatableComponent("death.attack.even_more_magic", new Object[]{this.getDisplayName()})).withStyle((style) -> {
                        return style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, component2));
                    });
                    this.connection.send(new ClientboundPlayerCombatKillPacket(this.getCombatTracker(), component3));
                }

            });
            Team team = this.getTeam();
            if (team != null && team.getDeathMessageVisibility() != Visibility.ALWAYS) {
                if (team.getDeathMessageVisibility() == Visibility.HIDE_FOR_OTHER_TEAMS) {
                    this.server.getPlayerList().broadcastToTeam(this, component);
                } else if (team.getDeathMessageVisibility() == Visibility.HIDE_FOR_OWN_TEAM) {
                    this.server.getPlayerList().broadcastToAllExceptTeam(this, component);
                }
            } else {
                this.server.getPlayerList().broadcastMessage(component, ChatType.SYSTEM, Util.NIL_UUID);
            }
        } else {
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getCombatTracker(), TextComponent.EMPTY));
        }

        this.removeEntitiesOnShoulder();
        if (this.level.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            this.tellNeutralMobsThatIDied();
        }

        if (!this.isSpectator()) {
            this.dropAllDeathLoot(damageSource);
        }

        this.getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this.getScoreboardName(), Score::increment);
        LivingEntity livingEntity = this.getKillCredit();
        if (livingEntity != null) {
            this.awardStat(Stats.ENTITY_KILLED_BY.get(livingEntity.getType()));
            livingEntity.awardKillScore(this, this.deathScore, damageSource);
            this.createWitherRose(livingEntity);
        }

        this.level.broadcastEntityEvent(this, (byte)3);
        this.awardStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.clearFire();
        this.setTicksFrozen(0);
        this.setSharedFlagOnFire(false);
        this.getCombatTracker().recheckStatus();
    }

    private void tellNeutralMobsThatIDied() {
        AABB aABB = (new AABB(this.blockPosition())).inflate(32.0, 10.0, 32.0);
        this.level.getEntitiesOfClass(Mob.class, aABB, EntitySelector.NO_SPECTATORS).stream().filter((mob) -> {
            return mob instanceof NeutralMob;
        }).forEach((mob) -> {
            ((NeutralMob)mob).playerDied(this);
        });
    }

    public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
        if (entity != this) {
            super.awardKillScore(entity, i, damageSource);
            this.increaseScore(i);
            String string = this.getScoreboardName();
            String string2 = entity.getScoreboardName();
            this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_ALL, string, Score::increment);
            if (entity instanceof Player) {
                this.awardStat(Stats.PLAYER_KILLS);
                this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_PLAYERS, string, Score::increment);
            } else {
                this.awardStat(Stats.MOB_KILLS);
            }

            this.handleTeamKill(string, string2, ObjectiveCriteria.TEAM_KILL);
            this.handleTeamKill(string2, string, ObjectiveCriteria.KILLED_BY_TEAM);
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(this, entity, damageSource);
        }
    }

    private void handleTeamKill(String string, String string2, ObjectiveCriteria[] objectiveCriterias) {
        PlayerTeam playerTeam = this.getScoreboard().getPlayersTeam(string2);
        if (playerTeam != null) {
            int i = playerTeam.getColor().getId();
            if (i >= 0 && i < objectiveCriterias.length) {
                this.getScoreboard().forAllObjectives(objectiveCriterias[i], string, Score::increment);
            }
        }

    }

    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        } else {
            boolean bl = this.server.isDedicatedServer() && this.isPvpAllowed() && "fall".equals(damageSource.msgId);
            if (!bl && this.spawnInvulnerableTime > 0 && damageSource != DamageSource.OUT_OF_WORLD) {
                return false;
            } else {
                if (damageSource instanceof EntityDamageSource) {
                    Entity entity = damageSource.getEntity();
                    if (entity instanceof Player && !this.canHarmPlayer((Player)entity)) {
                        return false;
                    }

                    if (entity instanceof AbstractArrow) {
                        AbstractArrow abstractArrow = (AbstractArrow)entity;
                        Entity entity2 = abstractArrow.getOwner();
                        if (entity2 instanceof Player && !this.canHarmPlayer((Player)entity2)) {
                            return false;
                        }
                    }
                }

                return super.hurt(damageSource, f);
            }
        }
    }

    public boolean canHarmPlayer(Player player) {
        return !this.isPvpAllowed() ? false : super.canHarmPlayer(player);
    }

    private boolean isPvpAllowed() {
        return this.server.isPvpAllowed();
    }

    @Nullable
    protected PortalInfo findDimensionEntryPoint(ServerLevel serverLevel) {
        PortalInfo portalInfo = super.findDimensionEntryPoint(serverLevel);
        if (portalInfo != null && this.level.dimension() == Level.OVERWORLD && serverLevel.dimension() == Level.END) {
            Vec3 vec3 = portalInfo.pos.add(0.0, -1.0, 0.0);
            return new PortalInfo(vec3, Vec3.ZERO, 90.0F, 0.0F);
        } else {
            return portalInfo;
        }
    }

    @Nullable
    public Entity changeDimension(ServerLevel serverLevel) {
        this.isChangingDimension = true;
        ServerLevel serverLevel2 = this.getLevel();
        ResourceKey<Level> resourceKey = serverLevel2.dimension();
        if (resourceKey == Level.END && serverLevel.dimension() == Level.OVERWORLD) {
            this.unRide();
            this.getLevel().removePlayerImmediately(this, RemovalReason.CHANGED_DIMENSION);
            if (!this.wonGame) {
                this.wonGame = true;
                this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, this.seenCredits ? 0.0F : 1.0F));
                this.seenCredits = true;
            }

            return this;
        } else {
            LevelData levelData = serverLevel.getLevelData();
            this.connection.send(new ClientboundRespawnPacket(serverLevel.dimensionType(), serverLevel.dimension(), BiomeManager.obfuscateSeed(serverLevel.getSeed()), this.gameMode.getGameModeForPlayer(), this.gameMode.getPreviousGameModeForPlayer(), serverLevel.isDebug(), serverLevel.isFlat(), true));
            this.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
            PlayerList playerList = this.server.getPlayerList();
            playerList.sendPlayerPermissionLevel(this);
            serverLevel2.removePlayerImmediately(this, RemovalReason.CHANGED_DIMENSION);
            this.unsetRemoved();
            PortalInfo portalInfo = this.findDimensionEntryPoint(serverLevel);
            if (portalInfo != null) {
                serverLevel2.getProfiler().push("moving");
                if (resourceKey == Level.OVERWORLD && serverLevel.dimension() == Level.NETHER) {
                    this.enteredNetherPosition = this.position();
                } else if (serverLevel.dimension() == Level.END) {
                    this.createEndPlatform(serverLevel, new BlockPos(portalInfo.pos));
                }

                serverLevel2.getProfiler().pop();
                serverLevel2.getProfiler().push("placing");
                this.setLevel(serverLevel);
                serverLevel.addDuringPortalTeleport(this);
                this.setRot(portalInfo.yRot, portalInfo.xRot);
                this.moveTo(portalInfo.pos.x, portalInfo.pos.y, portalInfo.pos.z);
                serverLevel2.getProfiler().pop();
                this.triggerDimensionChangeTriggers(serverLevel2);
                this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
                playerList.sendLevelInfo(this, serverLevel);
                playerList.sendAllPlayerInfo(this);
                Iterator var7 = this.getActiveEffects().iterator();

                while(var7.hasNext()) {
                    MobEffectInstance mobEffectInstance = (MobEffectInstance)var7.next();
                    this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance));
                }

                this.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
                this.lastSentExp = -1;
                this.lastSentHealth = -1.0F;
                this.lastSentFood = -1;
            }

            return this;
        }
    }

    private void createEndPlatform(ServerLevel serverLevel, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

        for(int i = -2; i <= 2; ++i) {
            for(int j = -2; j <= 2; ++j) {
                for(int k = -1; k < 3; ++k) {
                    BlockState blockState = k == -1 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                    serverLevel.setBlockAndUpdate(mutableBlockPos.set(blockPos).move(j, k, i), blockState);
                }
            }
        }

    }

    protected Optional<BlockUtil.FoundRectangle> getExitPortal(ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        Optional<BlockUtil.FoundRectangle> optional = super.getExitPortal(serverLevel, blockPos, bl);
        if (optional.isPresent()) {
            return optional;
        } else {
            Direction.Axis axis = (Direction.Axis)this.level.getBlockState(this.portalEntrancePos).getOptionalValue(NetherPortalBlock.AXIS).orElse(Axis.X);
            Optional<BlockUtil.FoundRectangle> optional2 = serverLevel.getPortalForcer().createPortal(blockPos, axis);
            if (!optional2.isPresent()) {
                LOGGER.error("Unable to create a portal, likely target out of worldborder");
            }

            return optional2;
        }
    }

    private void triggerDimensionChangeTriggers(ServerLevel serverLevel) {
        ResourceKey<Level> resourceKey = serverLevel.dimension();
        ResourceKey<Level> resourceKey2 = this.level.dimension();
        CriteriaTriggers.CHANGED_DIMENSION.trigger(this, resourceKey, resourceKey2);
        if (resourceKey == Level.NETHER && resourceKey2 == Level.OVERWORLD && this.enteredNetherPosition != null) {
            CriteriaTriggers.NETHER_TRAVEL.trigger(this, this.enteredNetherPosition);
        }

        if (resourceKey2 != Level.NETHER) {
            this.enteredNetherPosition = null;
        }

    }

    public boolean broadcastToPlayer(ServerPlayer serverPlayer) {
        if (serverPlayer.isSpectator()) {
            return this.getCamera() == this;
        } else {
            return this.isSpectator() ? false : super.broadcastToPlayer(serverPlayer);
        }
    }

    private void broadcast(BlockEntity blockEntity) {
        if (blockEntity != null) {
            ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket = blockEntity.getUpdatePacket();
            if (clientboundBlockEntityDataPacket != null) {
                this.connection.send(clientboundBlockEntityDataPacket);
            }
        }

    }

    public void take(Entity entity, int i) {
        super.take(entity, i);
        this.containerMenu.broadcastChanges();
    }

    public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockPos) {
        Direction direction = (Direction)this.level.getBlockState(blockPos).getValue(HorizontalDirectionalBlock.FACING);
        if (!this.isSleeping() && this.isAlive()) {
            if (!this.level.dimensionType().natural()) {
                return Either.left(BedSleepingProblem.NOT_POSSIBLE_HERE);
            } else if (!this.bedInRange(blockPos, direction)) {
                return Either.left(BedSleepingProblem.TOO_FAR_AWAY);
            } else if (this.bedBlocked(blockPos, direction)) {
                return Either.left(BedSleepingProblem.OBSTRUCTED);
            } else {
                this.setRespawnPosition(this.level.dimension(), blockPos, this.getYRot(), false, true);
                if (this.level.isDay()) {
                    return Either.left(BedSleepingProblem.NOT_POSSIBLE_NOW);
                } else {
                    if (!this.isCreative()) {
                        double d = 8.0;
                        double e = 5.0;
                        Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
                        List<Monster> list = this.level.getEntitiesOfClass(Monster.class, new AABB(vec3.x() - 8.0, vec3.y() - 5.0, vec3.z() - 8.0, vec3.x() + 8.0, vec3.y() + 5.0, vec3.z() + 8.0), (monster) -> {
                            return monster.isPreventingPlayerRest(this);
                        });
                        if (!list.isEmpty()) {
                            return Either.left(BedSleepingProblem.NOT_SAFE);
                        }
                    }

                    Either<Player.BedSleepingProblem, Unit> either = super.startSleepInBed(blockPos).ifRight((unit) -> {
                        this.awardStat(Stats.SLEEP_IN_BED);
                        CriteriaTriggers.SLEPT_IN_BED.trigger(this);
                    });
                    if (!this.getLevel().canSleepThroughNights()) {
                        this.displayClientMessage(new TranslatableComponent("sleep.not_possible"), true);
                    }

                    ((ServerLevel)this.level).updateSleepingPlayerList();
                    return either;
                }
            }
        } else {
            return Either.left(BedSleepingProblem.OTHER_PROBLEM);
        }
    }

    public void startSleeping(BlockPos blockPos) {
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        super.startSleeping(blockPos);
    }

    private boolean bedInRange(BlockPos blockPos, Direction direction) {
        return this.isReachableBedBlock(blockPos) || this.isReachableBedBlock(blockPos.relative(direction.getOpposite()));
    }

    private boolean isReachableBedBlock(BlockPos blockPos) {
        Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
        return Math.abs(this.getX() - vec3.x()) <= 3.0 && Math.abs(this.getY() - vec3.y()) <= 2.0 && Math.abs(this.getZ() - vec3.z()) <= 3.0;
    }

    private boolean bedBlocked(BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = blockPos.above();
        return !this.freeAt(blockPos2) || !this.freeAt(blockPos2.relative(direction.getOpposite()));
    }

    public void stopSleepInBed(boolean bl, boolean bl2) {
        if (this.isSleeping()) {
            this.getLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(this, 2));
        }

        super.stopSleepInBed(bl, bl2);
        if (this.connection != null) {
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }

    }

    public boolean startRiding(Entity entity, boolean bl) {
        Entity entity2 = this.getVehicle();
        if (!super.startRiding(entity, bl)) {
            return false;
        } else {
            Entity entity3 = this.getVehicle();
            if (entity3 != entity2 && this.connection != null) {
                this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            }

            return true;
        }
    }

    public void stopRiding() {
        Entity entity = this.getVehicle();
        super.stopRiding();
        Entity entity2 = this.getVehicle();
        if (entity2 != entity && this.connection != null) {
            this.connection.dismount(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }

    }

    public void dismountTo(double d, double e, double f) {
        this.removeVehicle();
        if (this.connection != null) {
            this.connection.dismount(d, e, f, this.getYRot(), this.getXRot());
        }

    }

    public boolean isInvulnerableTo(DamageSource damageSource) {
        return super.isInvulnerableTo(damageSource) || this.isChangingDimension() || this.getAbilities().invulnerable && damageSource == DamageSource.WITHER;
    }

    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
    }

    protected void onChangedBlock(BlockPos blockPos) {
        if (!this.isSpectator()) {
            super.onChangedBlock(blockPos);
        }

    }

    public void doCheckFallDamage(double d, boolean bl) {
        if (!this.touchingUnloadedChunk()) {
            BlockPos blockPos = this.getOnPos();
            super.checkFallDamage(d, bl, this.level.getBlockState(blockPos), blockPos);
        }
    }

    public void openTextEdit(SignBlockEntity signBlockEntity) {
        signBlockEntity.setAllowedPlayerEditor(this.getUUID());
        this.connection.send(new ClientboundBlockUpdatePacket(this.level, signBlockEntity.getBlockPos()));
        this.connection.send(new ClientboundOpenSignEditorPacket(signBlockEntity.getBlockPos()));
    }

    private void nextContainerCounter() {
        this.containerCounter = this.containerCounter % 100 + 1;
    }

    public OptionalInt openMenu(@Nullable MenuProvider menuProvider) {
        if (menuProvider == null) {
            return OptionalInt.empty();
        } else {
            if (this.containerMenu != this.inventoryMenu) {
                this.closeContainer();
            }

            this.nextContainerCounter();
            AbstractContainerMenu abstractContainerMenu = menuProvider.createMenu(this.containerCounter, this.getInventory(), this);
            if (abstractContainerMenu == null) {
                if (this.isSpectator()) {
                    this.displayClientMessage((new TranslatableComponent("container.spectatorCantOpen")).withStyle(ChatFormatting.RED), true);
                }

                return OptionalInt.empty();
            } else {
                this.connection.send(new ClientboundOpenScreenPacket(abstractContainerMenu.containerId, abstractContainerMenu.getType(), menuProvider.getDisplayName()));
                this.initMenu(abstractContainerMenu);
                this.containerMenu = abstractContainerMenu;
                return OptionalInt.of(this.containerCounter);
            }
        }
    }

    public void sendMerchantOffers(int i, MerchantOffers merchantOffers, int j, int k, boolean bl, boolean bl2) {
        this.connection.send(new ClientboundMerchantOffersPacket(i, merchantOffers, j, k, bl, bl2));
    }

    public void openHorseInventory(AbstractHorse abstractHorse, Container container) {
        if (this.containerMenu != this.inventoryMenu) {
            this.closeContainer();
        }

        this.nextContainerCounter();
        this.connection.send(new ClientboundHorseScreenOpenPacket(this.containerCounter, container.getContainerSize(), abstractHorse.getId()));
        this.containerMenu = new HorseInventoryMenu(this.containerCounter, this.getInventory(), container, abstractHorse);
        this.initMenu(this.containerMenu);
    }

    public void openItemGui(ItemStack itemStack, InteractionHand interactionHand) {
        if (itemStack.is(Items.WRITTEN_BOOK)) {
            if (WrittenBookItem.resolveBookComponents(itemStack, this.createCommandSourceStack(), this)) {
                this.containerMenu.broadcastChanges();
            }

            this.connection.send(new ClientboundOpenBookPacket(interactionHand));
        }

    }

    public void openCommandBlock(CommandBlockEntity commandBlockEntity) {
        commandBlockEntity.setSendToClient(true);
        this.broadcast(commandBlockEntity);
    }

    public void closeContainer() {
        this.connection.send(new ClientboundContainerClosePacket(this.containerMenu.containerId));
        this.doCloseContainer();
    }

    public void doCloseContainer() {
        this.containerMenu.removed(this);
        this.inventoryMenu.transferState(this.containerMenu);
        this.containerMenu = this.inventoryMenu;
    }

    public void setPlayerInput(float f, float g, boolean bl, boolean bl2) {
        if (this.isPassenger()) {
            if (f >= -1.0F && f <= 1.0F) {
                this.xxa = f;
            }

            if (g >= -1.0F && g <= 1.0F) {
                this.zza = g;
            }

            this.jumping = bl;
            this.setShiftKeyDown(bl2);
        }

    }

    public void awardStat(Stat<?> stat, int i) {
        this.stats.increment(this, stat, i);
        this.getScoreboard().forAllObjectives(stat, this.getScoreboardName(), (score) -> {
            score.add(i);
        });
    }

    public void resetStat(Stat<?> stat) {
        this.stats.setValue(this, stat, 0);
        this.getScoreboard().forAllObjectives(stat, this.getScoreboardName(), Score::reset);
    }

    public int awardRecipes(Collection<Recipe<?>> collection) {
        return this.recipeBook.addRecipes(collection, this);
    }

    public void awardRecipesByKey(ResourceLocation[] resourceLocations) {
        List<Recipe<?>> list = Lists.newArrayList();
        ResourceLocation[] var3 = resourceLocations;
        int var4 = resourceLocations.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            ResourceLocation resourceLocation = var3[var5];
            Optional var10000 = this.server.getRecipeManager().byKey(resourceLocation);
            Objects.requireNonNull(list);
            var10000.ifPresent(val -> list.add((Recipe<?>) val));
        }

        this.awardRecipes(list);
    }

    public int resetRecipes(Collection<Recipe<?>> collection) {
        return this.recipeBook.removeRecipes(collection, this);
    }

    public void giveExperiencePoints(int i) {
        super.giveExperiencePoints(i);
        this.lastSentExp = -1;
    }

    public void disconnect() {
        this.disconnected = true;
        this.ejectPassengers();
        if (this.isSleeping()) {
            this.stopSleepInBed(true, false);
        }

    }

    public boolean hasDisconnected() {
        return this.disconnected;
    }

    public void resetSentInfo() {
        this.lastSentHealth = -1.0E8F;
    }

    public void displayClientMessage(Component component, boolean bl) {
        this.sendMessage(component, bl ? ChatType.GAME_INFO : ChatType.CHAT, Util.NIL_UUID);
    }

    protected void completeUsingItem() {
        if (!this.useItem.isEmpty() && this.isUsingItem()) {
            this.connection.send(new ClientboundEntityEventPacket(this, (byte)9));
            super.completeUsingItem();
        }

    }

    public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 vec3) {
        super.lookAt(anchor, vec3);
        this.connection.send(new ClientboundPlayerLookAtPacket(anchor, vec3.x, vec3.y, vec3.z));
    }

    public void lookAt(EntityAnchorArgument.Anchor anchor, Entity entity, EntityAnchorArgument.Anchor anchor2) {
        Vec3 vec3 = anchor2.apply(entity);
        super.lookAt(anchor, vec3);
        this.connection.send(new ClientboundPlayerLookAtPacket(anchor, entity, anchor2));
    }

    public void restoreFrom(ServerPlayer serverPlayer, boolean bl) {
        this.textFilteringEnabled = serverPlayer.textFilteringEnabled;
        this.gameMode.setGameModeForPlayer(serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.gameMode.getPreviousGameModeForPlayer());
        if (bl) {
            this.getInventory().replaceWith(serverPlayer.getInventory());
            this.setHealth(serverPlayer.getHealth());
            this.foodData = serverPlayer.foodData;
            this.experienceLevel = serverPlayer.experienceLevel;
            this.totalExperience = serverPlayer.totalExperience;
            this.experienceProgress = serverPlayer.experienceProgress;
            this.setScore(serverPlayer.getScore());
            this.portalEntrancePos = serverPlayer.portalEntrancePos;
        } else if (this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || serverPlayer.isSpectator()) {
            this.getInventory().replaceWith(serverPlayer.getInventory());
            this.experienceLevel = serverPlayer.experienceLevel;
            this.totalExperience = serverPlayer.totalExperience;
            this.experienceProgress = serverPlayer.experienceProgress;
            this.setScore(serverPlayer.getScore());
        }

        this.enchantmentSeed = serverPlayer.enchantmentSeed;
        this.enderChestInventory = serverPlayer.enderChestInventory;
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (Byte)serverPlayer.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
        this.lastSentExp = -1;
        this.lastSentHealth = -1.0F;
        this.lastSentFood = -1;
        this.recipeBook.copyOverData(serverPlayer.recipeBook);
        this.seenCredits = serverPlayer.seenCredits;
        this.enteredNetherPosition = serverPlayer.enteredNetherPosition;
        this.setShoulderEntityLeft(serverPlayer.getShoulderEntityLeft());
        this.setShoulderEntityRight(serverPlayer.getShoulderEntityRight());
    }

    protected void onEffectAdded(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        super.onEffectAdded(mobEffectInstance, entity);
        this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance));
        if (mobEffectInstance.getEffect() == MobEffects.LEVITATION) {
            this.levitationStartTime = this.tickCount;
            this.levitationStartPos = this.position();
        }

        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, entity);
    }

    protected void onEffectUpdated(MobEffectInstance mobEffectInstance, boolean bl, @Nullable Entity entity) {
        super.onEffectUpdated(mobEffectInstance, bl, entity);
        this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance));
        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, entity);
    }

    protected void onEffectRemoved(MobEffectInstance mobEffectInstance) {
        super.onEffectRemoved(mobEffectInstance);
        this.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), mobEffectInstance.getEffect()));
        if (mobEffectInstance.getEffect() == MobEffects.LEVITATION) {
            this.levitationStartPos = null;
        }

        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, (Entity)null);
    }

    public void teleportTo(double d, double e, double f) {
        this.connection.teleport(d, e, f, this.getYRot(), this.getXRot());
    }

    public void moveTo(double d, double e, double f) {
        this.teleportTo(d, e, f);
        this.connection.resetPosition();
    }

    public void crit(Entity entity) {
        this.getLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(entity, 4));
    }

    public void magicCrit(Entity entity) {
        this.getLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(entity, 5));
    }

    public void onUpdateAbilities() {
        if (this.connection != null) {
            this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
            this.updateInvisibilityStatus();
        }
    }

    public ServerLevel getLevel() {
        return (ServerLevel)this.level;
    }

    public boolean setGameMode(GameType gameType) {
        if (!this.gameMode.changeGameModeForPlayer(gameType)) {
            return false;
        } else {
            this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float)gameType.getId()));
            if (gameType == GameType.SPECTATOR) {
                this.removeEntitiesOnShoulder();
                this.stopRiding();
            } else {
                this.setCamera(this);
            }

            this.onUpdateAbilities();
            this.updateEffectVisibility();
            return true;
        }
    }

    public boolean isSpectator() {
        return this.gameMode.getGameModeForPlayer() == GameType.SPECTATOR;
    }

    public boolean isCreative() {
        return this.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
    }

    public void sendMessage(Component component, UUID uUID) {
        this.sendMessage(component, ChatType.SYSTEM, uUID);
    }

    public void sendMessage(Component component, ChatType chatType, UUID uUID) {
        if (this.acceptsChat(chatType)) {
            this.connection.send(new ClientboundChatPacket(component, chatType, uUID), (future) -> {
                if (!future.isSuccess() && (chatType == ChatType.GAME_INFO || chatType == ChatType.SYSTEM) && this.acceptsChat(ChatType.SYSTEM)) {
                    boolean i = true;
                    String string = component.getString(256);
                    Component component2 = (new TextComponent(string)).withStyle(ChatFormatting.YELLOW);
                    this.connection.send(new ClientboundChatPacket((new TranslatableComponent("multiplayer.message_not_delivered", new Object[]{component2})).withStyle(ChatFormatting.RED), ChatType.SYSTEM, uUID));
                }

            });
        }
    }

    public String getIpAddress() {
        String string = this.connection.connection.getRemoteAddress().toString();
        string = string.substring(string.indexOf("/") + 1);
        string = string.substring(0, string.indexOf(":"));
        return string;
    }

    public void updateOptions(ServerboundClientInformationPacket serverboundClientInformationPacket) {
        this.chatVisibility = serverboundClientInformationPacket.getChatVisibility();
        this.canChatColor = serverboundClientInformationPacket.getChatColors();
        this.textFilteringEnabled = serverboundClientInformationPacket.isTextFilteringEnabled();
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte)serverboundClientInformationPacket.getModelCustomisation());
        this.getEntityData().set(DATA_PLAYER_MAIN_HAND, (byte)(serverboundClientInformationPacket.getMainHand() == HumanoidArm.LEFT ? 0 : 1));
    }

    public boolean canChatInColor() {
        return this.canChatColor;
    }

    public ChatVisiblity getChatVisibility() {
        return this.chatVisibility;
    }

    private boolean acceptsChat(ChatType chatType) {
        switch (this.chatVisibility) {
            case HIDDEN:
                return chatType == ChatType.GAME_INFO;
            case SYSTEM:
                return chatType == ChatType.SYSTEM || chatType == ChatType.GAME_INFO;
            case FULL:
            default:
                return true;
        }
    }

    public void sendTexturePack(String string, String string2, boolean bl, @Nullable Component component) {
        this.connection.send(new ClientboundResourcePackPacket(string, string2, bl, component));
    }

    protected int getPermissionLevel() {
        return this.server.getProfilePermissions(this.getGameProfile());
    }

    public void resetLastActionTime() {
        this.lastActionTime = Util.getMillis();
    }

    public ServerStatsCounter getStats() {
        return this.stats;
    }

    public ServerRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    protected void updateInvisibilityStatus() {
        if (this.isSpectator()) {
            this.removeEffectParticles();
            this.setInvisible(true);
        } else {
            super.updateInvisibilityStatus();
        }

    }

    public Entity getCamera() {
        return (Entity)(this.camera == null ? this : this.camera);
    }

    public void setCamera(Entity entity) {
        Entity entity2 = this.getCamera();
        this.camera = (Entity)(entity == null ? this : entity);
        if (entity2 != this.camera) {
            this.connection.send(new ClientboundSetCameraPacket(this.camera));
            this.teleportTo(this.camera.getX(), this.camera.getY(), this.camera.getZ());
        }

    }

    protected void processPortalCooldown() {
        if (!this.isChangingDimension) {
            super.processPortalCooldown();
        }

    }

    public void attack(Entity entity) {
        if (this.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            this.setCamera(entity);
        } else {
            super.attack(entity);
        }

    }

    public long getLastActionTime() {
        return this.lastActionTime;
    }

    @Nullable
    public Component getTabListDisplayName() {
        return null;
    }

    public void swing(InteractionHand interactionHand) {
        super.swing(interactionHand);
        this.resetAttackStrengthTicker();
    }

    public boolean isChangingDimension() {
        return this.isChangingDimension;
    }

    public void hasChangedDimension() {
        this.isChangingDimension = false;
    }

    public PlayerAdvancements getAdvancements() {
        return this.advancements;
    }

    public void teleportTo(ServerLevel serverLevel, double d, double e, double f, float g, float h) {
        this.setCamera(this);
        this.stopRiding();
        if (serverLevel == this.level) {
            this.connection.teleport(d, e, f, g, h);
        } else {
            ServerLevel serverLevel2 = this.getLevel();
            LevelData levelData = serverLevel.getLevelData();
            this.connection.send(new ClientboundRespawnPacket(serverLevel.dimensionType(), serverLevel.dimension(), BiomeManager.obfuscateSeed(serverLevel.getSeed()), this.gameMode.getGameModeForPlayer(), this.gameMode.getPreviousGameModeForPlayer(), serverLevel.isDebug(), serverLevel.isFlat(), true));
            this.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
            this.server.getPlayerList().sendPlayerPermissionLevel(this);
            serverLevel2.removePlayerImmediately(this, RemovalReason.CHANGED_DIMENSION);
            this.unsetRemoved();
            this.moveTo(d, e, f, g, h);
            this.setLevel(serverLevel);
            serverLevel.addDuringCommandTeleport(this);
            this.triggerDimensionChangeTriggers(serverLevel2);
            this.connection.teleport(d, e, f, g, h);
            this.server.getPlayerList().sendLevelInfo(this, serverLevel);
            this.server.getPlayerList().sendAllPlayerInfo(this);
        }

    }

    @Nullable
    public BlockPos getRespawnPosition() {
        return this.respawnPosition;
    }

    public float getRespawnAngle() {
        return this.respawnAngle;
    }

    public ResourceKey<Level> getRespawnDimension() {
        return this.respawnDimension;
    }

    public boolean isRespawnForced() {
        return this.respawnForced;
    }

    public void setRespawnPosition(ResourceKey<Level> resourceKey, @Nullable BlockPos blockPos, float f, boolean bl, boolean bl2) {
        if (blockPos != null) {
            boolean bl3 = blockPos.equals(this.respawnPosition) && resourceKey.equals(this.respawnDimension);
            if (bl2 && !bl3) {
                this.sendMessage(new TranslatableComponent("block.minecraft.set_spawn"), Util.NIL_UUID);
            }

            this.respawnPosition = blockPos;
            this.respawnDimension = resourceKey;
            this.respawnAngle = f;
            this.respawnForced = bl;
        } else {
            this.respawnPosition = null;
            this.respawnDimension = Level.OVERWORLD;
            this.respawnAngle = 0.0F;
            this.respawnForced = false;
        }

    }

    public void trackChunk(ChunkPos chunkPos, Packet<?> packet, Packet<?> packet2) {
        this.connection.send(packet2);
        this.connection.send(packet);
    }

    public void untrackChunk(ChunkPos chunkPos) {
        if (this.isAlive()) {
            this.connection.send(new ClientboundForgetLevelChunkPacket(chunkPos.x, chunkPos.z));
        }

    }

    public SectionPos getLastSectionPos() {
        return this.lastSectionPos;
    }

    public void setLastSectionPos(SectionPos sectionPos) {
        this.lastSectionPos = sectionPos;
    }

    public void playNotifySound(SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
        this.connection.send(new ClientboundSoundPacket(soundEvent, soundSource, this.getX(), this.getY(), this.getZ(), f, g));
    }

    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddPlayerPacket(this);
    }

    public ItemEntity drop(ItemStack itemStack, boolean bl, boolean bl2) {
        ItemEntity itemEntity = super.drop(itemStack, bl, bl2);
        if (itemEntity == null) {
            return null;
        } else {
            this.level.addFreshEntity(itemEntity);
            ItemStack itemStack2 = itemEntity.getItem();
            if (bl2) {
                if (!itemStack2.isEmpty()) {
                    this.awardStat(Stats.ITEM_DROPPED.get(itemStack2.getItem()), itemStack.getCount());
                }

                this.awardStat(Stats.DROP);
            }

            return itemEntity;
        }
    }

    public TextFilter getTextFilter() {
        return this.textFilter;
    }

    public void setLevel(ServerLevel serverLevel) {
        this.level = serverLevel;
        this.gameMode.setLevel(serverLevel);
    }

    @Nullable
    private static GameType readPlayerMode(@Nullable CompoundTag compoundTag, String string) {
        return compoundTag != null && compoundTag.contains(string, 99) ? GameType.byId(compoundTag.getInt(string)) : null;
    }

    private GameType calculateGameModeForNewPlayer(@Nullable GameType gameType) {
        GameType gameType2 = this.server.getForcedGameType();
        if (gameType2 != null) {
            return gameType2;
        } else {
            return gameType != null ? gameType : this.server.getDefaultGameType();
        }
    }

    public void loadGameTypes(@Nullable CompoundTag compoundTag) {
        this.gameMode.setGameModeForPlayer(this.calculateGameModeForNewPlayer(readPlayerMode(compoundTag, "playerGameType")), readPlayerMode(compoundTag, "previousPlayerGameType"));
    }

    private void storeGameTypes(CompoundTag compoundTag) {
        compoundTag.putInt("playerGameType", this.gameMode.getGameModeForPlayer().getId());
        GameType gameType = this.gameMode.getPreviousGameModeForPlayer();
        if (gameType != null) {
            compoundTag.putInt("previousPlayerGameType", gameType.getId());
        }

    }

    public boolean isTextFilteringEnabled() {
        return this.textFilteringEnabled;
    }

    public boolean shouldFilterMessageTo(ServerPlayer serverPlayer) {
        if (serverPlayer == this) {
            return false;
        } else {
            return this.textFilteringEnabled || serverPlayer.textFilteringEnabled;
        }
    }

    public boolean mayInteract(Level level, BlockPos blockPos) {
        return super.mayInteract(level, blockPos) && level.mayInteract(this, blockPos);
    }

    protected void updateUsingItem(ItemStack itemStack) {
        CriteriaTriggers.USING_ITEM.trigger(this, itemStack);
        super.updateUsingItem(itemStack);
    }

    public boolean drop(boolean bl) {
        Inventory inventory = this.getInventory();
        ItemStack itemStack = inventory.removeFromSelected(bl);
        this.containerMenu.findSlot(inventory, inventory.selected).ifPresent((i) -> {
            this.containerMenu.setRemoteSlot(i, inventory.getSelected());
        });
        return this.drop(itemStack, false, true) != null;
    }
}
