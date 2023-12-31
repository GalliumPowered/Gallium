package net.minecraft.server;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Features;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Snooper;
import net.minecraft.world.SnooperPopulator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.galliumpowered.Gallium;
import org.galliumpowered.Mod;
import org.galliumpowered.annotation.Plugin;
import org.galliumpowered.event.system.ServerShutdownEvent;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.galliumpowered.plugin.PluginContainer;
import org.galliumpowered.plugin.metadata.PluginMetadataLoader;

@Plugin( // Gallium start: Minecraft plugin
        name = "Minecraft",
        id = "minecraft",
        description = "Minecraft",
        authors = "Mojang",
        version = "1.17.1"
) // Gallium end
public abstract class MinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements SnooperPopulator, CommandSource, AutoCloseable {
    protected Logger logger; // Gallium
    protected static final Logger LOGGER = LogManager.getLogger("minecraft"); // Gallium
    private static final float AVERAGE_TICK_TIME_SMOOTHING = 0.8F;
    private static final int TICK_STATS_SPAN = 100;
    public static final int MS_PER_TICK = 50;
    private static final int SNOOPER_UPDATE_INTERVAL = 6000;
    private static final int OVERLOADED_THRESHOLD = 2000;
    private static final int OVERLOADED_WARNING_INTERVAL = 15000;
    public static final String LEVEL_STORAGE_PROTOCOL = "level";
    public static final String LEVEL_STORAGE_SCHEMA = "level://";
    private static final long STATUS_EXPIRE_TIME_NS = 5000000000L;
    private static final int MAX_STATUS_PLAYER_SAMPLE = 12;
    public static final String MAP_RESOURCE_FILE = "resources.zip";
    public static final File USERID_CACHE_FILE = new File("usercache.json");
    public static final int START_CHUNK_RADIUS = 11;
    private static final int START_TICKING_CHUNK_COUNT = 441;
    private static final int AUTOSAVE_INTERVAL = 6000;
    private static final int MAX_TICK_LATENCY = 3;
    public static final int ABSOLUTE_MAX_WORLD_SIZE = 29999984;
    public static final LevelSettings DEMO_SETTINGS;
    private static final long DELAYED_TASKS_TICK_EXTENSION = 50L;
    protected final LevelStorageSource.LevelStorageAccess storageSource;
    protected final PlayerDataStorage playerDataStorage;
    private final Snooper snooper = new Snooper("server", this, Util.getMillis());
    private final List<Runnable> tickables = Lists.newArrayList();
    private MetricsRecorder metricsRecorder;
    private ProfilerFiller profiler;
    private Consumer<ProfileResults> onMetricsRecordingStopped;
    private Consumer<Path> onMetricsRecordingFinished;
    private boolean willStartRecordingMetrics;
    @Nullable
    private TimeProfiler debugCommandProfiler;
    private boolean debugCommandProfilerDelayStart;
    private final ServerConnectionListener connection;
    private final ChunkProgressListenerFactory progressListenerFactory;
    private final ServerStatus status;
    private final Random random;
    private final DataFixer fixerUpper;
    private String localIp;
    private int port;
    protected final RegistryAccess.RegistryHolder registryHolder;
    private final Map<ResourceKey<Level>, ServerLevel> levels;
    private PlayerList playerList;
    private volatile boolean running;
    private boolean stopped;
    private int tickCount;
    protected final Proxy proxy;
    private boolean onlineMode;
    private boolean preventProxyConnections;
    private boolean pvp;
    private boolean allowFlight;
    @Nullable
    private String motd;
    private int playerIdleTimeout;
    public final long[] tickTimes;
    @Nullable
    private KeyPair keyPair;
    @Nullable
    private String singleplayerName;
    private boolean isDemo;
    private String resourcePack;
    private String resourcePackHash;
    private volatile boolean isReady;
    private long lastOverloadWarning;
    private final MinecraftSessionService sessionService;
    @Nullable
    private final GameProfileRepository profileRepository;
    @Nullable
    private final GameProfileCache profileCache;
    private long lastServerStatus;
    private final Thread serverThread;
    private long nextTickTime;
    private long delayedTasksMaxNextTickTime;
    private boolean mayHaveDelayedTasks;
    private final PackRepository packRepository;
    private final ServerScoreboard scoreboard;
    @Nullable
    private CommandStorage commandStorage;
    private final CustomBossEvents customBossEvents;
    private final ServerFunctionManager functionManager;
    private final FrameTimer frameTimer;
    private boolean enforceWhitelist;
    private float averageTickTime;
    private final Executor executor;
    @Nullable
    private String serverId;
    private ServerResources resources;
    private final StructureManager structureManager;
    protected final WorldData worldData;
    protected PluginContainer pluginContainer; // Gallium

    public static <S extends MinecraftServer> S spin(Function<Thread, S> function) {
        AtomicReference<S> atomicReference = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            ((MinecraftServer)atomicReference.get()).runServer();
        }, "Server thread");
        thread.setUncaughtExceptionHandler((threadx, throwable) -> {
            LOGGER.error(throwable);
        });
        S minecraftServer = (S) function.apply(thread);
        atomicReference.set(minecraftServer);
        thread.start();
        return minecraftServer;
    }

    public MinecraftServer(Thread thread, RegistryAccess.RegistryHolder registryHolder, LevelStorageSource.LevelStorageAccess levelStorageAccess, WorldData worldData, PackRepository packRepository, Proxy proxy, DataFixer dataFixer, ServerResources serverResources, @Nullable MinecraftSessionService minecraftSessionService, @Nullable GameProfileRepository gameProfileRepository, @Nullable GameProfileCache gameProfileCache, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super("Server");
        this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
        this.profiler = this.metricsRecorder.getProfiler();
        this.onMetricsRecordingStopped = (profileResults) -> {
            this.stopRecordingMetrics();
        };
        this.onMetricsRecordingFinished = (path) -> {
        };
        this.status = new ServerStatus();
        this.random = new Random();
        this.port = -1;
        this.levels = Maps.newLinkedHashMap();
        this.running = true;
        this.tickTimes = new long[100];
        this.resourcePack = "";
        this.resourcePackHash = "";
        this.nextTickTime = Util.getMillis();
        this.scoreboard = new ServerScoreboard(this);
        this.customBossEvents = new CustomBossEvents();
        this.frameTimer = new FrameTimer();
        this.registryHolder = registryHolder;
        this.worldData = worldData;
        this.proxy = proxy;
        this.packRepository = packRepository;
        this.resources = serverResources;
        this.sessionService = minecraftSessionService;
        this.profileRepository = gameProfileRepository;
        this.profileCache = gameProfileCache;
        if (gameProfileCache != null) {
            gameProfileCache.setExecutor(this);
        }

        this.connection = new ServerConnectionListener(this);
        this.progressListenerFactory = chunkProgressListenerFactory;
        this.storageSource = levelStorageAccess;
        this.playerDataStorage = levelStorageAccess.createPlayerStorage();
        this.fixerUpper = dataFixer;
        this.functionManager = new ServerFunctionManager(this, serverResources.getFunctionLibrary());
        this.structureManager = new StructureManager(serverResources.getResourceManager(), levelStorageAccess, dataFixer);
        this.serverThread = thread;
        this.executor = Util.backgroundExecutor();

        // Gallium start: Internal Minecraft plugin
        // TODO: Register Minecraft commands under Minecraft plugin ID
        pluginContainer = new PluginContainer();
        pluginContainer.setMetadata(PluginMetadataLoader.getPluginMetaFromAnnotation(MinecraftServer.class));
        pluginContainer.setInstance(this); // No inject for this plugin

        logger = pluginContainer.getLogger();

        Mod.setMinecraftServer(this);
        // Gallium end
    }

    private void readScoreboard(DimensionDataStorage dimensionDataStorage) {
        ServerScoreboard var10001 = this.getScoreboard();
        Objects.requireNonNull(var10001);
        Function<net.minecraft.nbt.CompoundTag, net.minecraft.world.scores.ScoreboardSaveData> var2 = var10001::createData; //Gallium
        ServerScoreboard var10002 = this.getScoreboard();
        Objects.requireNonNull(var10002);
        dimensionDataStorage.computeIfAbsent(var2, var10002::createData, "scoreboard");
    }

    protected abstract boolean initServer() throws IOException;

    public static void convertFromRegionFormatIfNeeded(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        if (levelStorageAccess.requiresConversion()) {
            LOGGER.info("Converting map!");
            levelStorageAccess.convertLevel(new ProgressListener() {
                private long timeStamp = Util.getMillis();

                public void progressStartNoAbort(Component component) {
                }

                public void progressStart(Component component) {
                }

                public void progressStagePercentage(int i) {
                    if (Util.getMillis() - this.timeStamp >= 1000L) {
                        this.timeStamp = Util.getMillis();
                        LOGGER.info("Converting... {}%", i);
                    }

                }

                public void stop() {
                }

                public void progressStage(Component component) {
                }
            });
        }

    }

    protected void loadLevel() {
        this.detectBundledResources();
        this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().isPresent());
        ChunkProgressListener chunkProgressListener = this.progressListenerFactory.create(11);
        this.createLevels(chunkProgressListener);
        this.forceDifficulty();
        this.prepareLevels(chunkProgressListener);
    }

    protected void forceDifficulty() {
    }

    protected void createLevels(ChunkProgressListener chunkProgressListener) {
        ServerLevelData serverLevelData = this.worldData.overworldData();
        WorldGenSettings worldGenSettings = this.worldData.worldGenSettings();
        boolean bl = worldGenSettings.isDebug();
        long l = worldGenSettings.seed();
        long m = BiomeManager.obfuscateSeed(l);
        List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(serverLevelData));
        MappedRegistry<LevelStem> mappedRegistry = worldGenSettings.dimensions();
        LevelStem levelStem = (LevelStem)mappedRegistry.get(LevelStem.OVERWORLD);
        ChunkGenerator chunkGenerator2;
        DimensionType dimensionType2;
        if (levelStem == null) {
            dimensionType2 = (DimensionType)this.registryHolder.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getOrThrow(DimensionType.OVERWORLD_LOCATION);
            chunkGenerator2 = WorldGenSettings.makeDefaultOverworld(this.registryHolder.registryOrThrow(Registry.BIOME_REGISTRY), this.registryHolder.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY), (new Random()).nextLong());
        } else {
            dimensionType2 = levelStem.type();
            chunkGenerator2 = levelStem.generator();
        }

        ServerLevel serverLevel = new ServerLevel(this, this.executor, this.storageSource, serverLevelData, Level.OVERWORLD, dimensionType2, chunkProgressListener, chunkGenerator2, bl, m, list, true);
        this.levels.put(Level.OVERWORLD, serverLevel);
        DimensionDataStorage dimensionDataStorage = serverLevel.getDataStorage();
        this.readScoreboard(dimensionDataStorage);
        this.commandStorage = new CommandStorage(dimensionDataStorage);
        WorldBorder worldBorder = serverLevel.getWorldBorder();
        worldBorder.applySettings(serverLevelData.getWorldBorder());
        if (!serverLevelData.isInitialized()) {
            try {
                setInitialSpawn(serverLevel, serverLevelData, worldGenSettings.generateBonusChest(), bl);
                serverLevelData.setInitialized(true);
                if (bl) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable var26) {
                CrashReport crashReport = CrashReport.forThrowable(var26, "Exception initializing level");

                try {
                    serverLevel.fillReportDetails(crashReport);
                } catch (Throwable var25) {
                }

                throw new ReportedException(crashReport);
            }

            serverLevelData.setInitialized(true);
        }

        this.getPlayerList().setLevel(serverLevel);
        if (this.worldData.getCustomBossEvents() != null) {
            this.getCustomBossEvents().load(this.worldData.getCustomBossEvents());
        }

        Iterator<Map.Entry<ResourceKey<LevelStem>, LevelStem>> var17 = mappedRegistry.entrySet().iterator();

        while(var17.hasNext()) {
            Map.Entry<ResourceKey<LevelStem>, LevelStem> entry = var17.next();
            ResourceKey<LevelStem> resourceKey = (ResourceKey<LevelStem>)entry.getKey();
            if (resourceKey != LevelStem.OVERWORLD) {
                ResourceKey<Level> resourceKey2 = ResourceKey.create(Registry.DIMENSION_REGISTRY, resourceKey.location());
                DimensionType dimensionType3 = ((LevelStem)entry.getValue()).type();
                ChunkGenerator chunkGenerator3 = ((LevelStem)entry.getValue()).generator();
                DerivedLevelData derivedLevelData = new DerivedLevelData(this.worldData, serverLevelData);
                ServerLevel serverLevel2 = new ServerLevel(this, this.executor, this.storageSource, derivedLevelData, resourceKey2, dimensionType3, chunkProgressListener, chunkGenerator3, bl, m, ImmutableList.of(), false);
                worldBorder.addListener(new BorderChangeListener.DelegateBorderChangeListener(serverLevel2.getWorldBorder()));
                this.levels.put(resourceKey2, serverLevel2);
            }
        }

    }

    private static void setInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean bl, boolean bl2) {
        if (bl2) {
            serverLevelData.setSpawn(BlockPos.ZERO.above(80), 0.0F);
        } else {
            ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
            BiomeSource biomeSource = chunkGenerator.getBiomeSource();
            Random random = new Random(serverLevel.getSeed());
            BlockPos blockPos = biomeSource.findBiomeHorizontal(0, serverLevel.getSeaLevel(), 0, 256, (biome) -> {
                return biome.getMobSettings().playerSpawnFriendly();
            }, random);
            ChunkPos chunkPos = blockPos == null ? new ChunkPos(0, 0) : new ChunkPos(blockPos);
            if (blockPos == null) {
                LOGGER.warn("Unable to find spawn biome");
            }

            boolean bl3 = false;
            Iterator<Block> var10 = BlockTags.VALID_SPAWN.getValues().iterator();

            while(var10.hasNext()) {
                Block block = var10.next();
                if (biomeSource.getSurfaceBlocks().contains(block.defaultBlockState())) {
                    bl3 = true;
                    break;
                }
            }

            int i = chunkGenerator.getSpawnHeight(serverLevel);
            if (i < serverLevel.getMinBuildHeight()) {
                BlockPos blockPos2 = chunkPos.getWorldPosition();
                i = serverLevel.getHeight(Types.WORLD_SURFACE, blockPos2.getX() + 8, blockPos2.getZ() + 8);
            }

            serverLevelData.setSpawn(chunkPos.getWorldPosition().offset(8, i, 8), 0.0F);
            int j = 0;
            int k = 0;
            int l = 0;
            int m = -1;
            boolean n = true;

            for(int o = 0; o < 1024; ++o) {
                if (j > -16 && j <= 16 && k > -16 && k <= 16) {
                    BlockPos blockPos3 = PlayerRespawnLogic.getSpawnPosInChunk(serverLevel, new ChunkPos(chunkPos.x + j, chunkPos.z + k), bl3);
                    if (blockPos3 != null) {
                        serverLevelData.setSpawn(blockPos3, 0.0F);
                        break;
                    }
                }

                if (j == k || j < 0 && j == -k || j > 0 && j == 1 - k) {
                    int p = l;
                    l = -m;
                    m = p;
                }

                j += l;
                k += m;
            }

            if (bl) {
                ConfiguredFeature<?, ?> configuredFeature = Features.BONUS_CHEST;
                configuredFeature.place(serverLevel, chunkGenerator, serverLevel.random, new BlockPos(serverLevelData.getXSpawn(), serverLevelData.getYSpawn(), serverLevelData.getZSpawn()));
            }

        }
    }

    private void setupDebugLevel(WorldData worldData) {
        worldData.setDifficulty(Difficulty.PEACEFUL);
        worldData.setDifficultyLocked(true);
        ServerLevelData serverLevelData = worldData.overworldData();
        serverLevelData.setRaining(false);
        serverLevelData.setThundering(false);
        serverLevelData.setClearWeatherTime(1000000000);
        serverLevelData.setDayTime(6000L);
        serverLevelData.setGameType(GameType.SPECTATOR);
    }

    private void prepareLevels(ChunkProgressListener chunkProgressListener) {
        ServerLevel serverLevel = this.overworld();
        logger.info("Preparing start region for dimension {}", serverLevel.dimension().location());
        BlockPos blockPos = serverLevel.getSharedSpawnPos();
        chunkProgressListener.updateSpawnPos(new ChunkPos(blockPos));
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        serverChunkCache.getLightEngine().setTaskPerBatch(500);
        this.nextTickTime = Util.getMillis();
        serverChunkCache.addRegionTicket(TicketType.START, new ChunkPos(blockPos), 11, Unit.INSTANCE);

        while(serverChunkCache.getTickingGenerated() != 441) {
            this.nextTickTime = Util.getMillis() + 10L;
            this.waitUntilNextTick();
        }

        this.nextTickTime = Util.getMillis() + 10L;
        this.waitUntilNextTick();
        Iterator<ServerLevel> var5 = this.levels.values().iterator();

        while(true) {
            ServerLevel serverLevel2;
            ForcedChunksSavedData forcedChunksSavedData;
            do {
                if (!var5.hasNext()) {
                    this.nextTickTime = Util.getMillis() + 10L;
                    this.waitUntilNextTick();
                    chunkProgressListener.stop();
                    serverChunkCache.getLightEngine().setTaskPerBatch(5);
                    this.updateMobSpawningFlags();
                    return;
                }

                serverLevel2 = var5.next();
                forcedChunksSavedData = (ForcedChunksSavedData)serverLevel2.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
            } while(forcedChunksSavedData == null);

            LongIterator longIterator = forcedChunksSavedData.getChunks().iterator();

            while(longIterator.hasNext()) {
                long l = longIterator.nextLong();
                ChunkPos chunkPos = new ChunkPos(l);
                serverLevel2.getChunkSource().updateChunkForced(chunkPos, true);
            }
        }
    }

    protected void detectBundledResources() {
        File file = this.storageSource.getLevelPath(LevelResource.MAP_RESOURCE_FILE).toFile();
        if (file.isFile()) {
            String string = this.storageSource.getLevelId();

            try {
                this.setResourcePack("level://" + URLEncoder.encode(string, StandardCharsets.UTF_8.toString()) + "/resources.zip", "");
            } catch (UnsupportedEncodingException var4) {
                logger.warn("Something went wrong url encoding {}", string);
            }
        }

    }

    public GameType getDefaultGameType() {
        return this.worldData.getGameType();
    }

    public boolean isHardcore() {
        return this.worldData.isHardcore();
    }

    public abstract int getOperatorUserPermissionLevel();

    public abstract int getFunctionCompilationLevel();

    public abstract boolean shouldRconBroadcast();

    public boolean saveAllChunks(boolean bl, boolean bl2, boolean bl3) {
        boolean bl4 = false;

        for(Iterator<ServerLevel> var5 = this.getAllLevels().iterator(); var5.hasNext(); bl4 = true) {
            ServerLevel serverLevel = var5.next();
            if (!bl) {
                logger.info("Saving chunks for level '{}'/{}", serverLevel, serverLevel.dimension().location());
            }

            serverLevel.save((ProgressListener)null, bl2, serverLevel.noSave && !bl3);
        }

        ServerLevel serverLevel2 = this.overworld();
        ServerLevelData serverLevelData = this.worldData.overworldData();
        serverLevelData.setWorldBorder(serverLevel2.getWorldBorder().createSettings());
        this.worldData.setCustomBossEvents(this.getCustomBossEvents().save());
        this.storageSource.saveDataTag(this.registryHolder, this.worldData, this.getPlayerList().getSingleplayerData());
        if (bl2) {
            Iterator<ServerLevel> var7 = this.getAllLevels().iterator();

            while(var7.hasNext()) {
                ServerLevel serverLevel3 = var7.next();
                logger.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", serverLevel3.getChunkSource().chunkMap.getStorageName());
            }

            logger.info("ThreadedAnvilChunkStorage: All dimensions are saved");
        }

        return bl4;
    }

    public void close() {
        this.stopServer();
    }

    public void stopServer() {
        // Gallium start
        new ServerShutdownEvent("I haven't added the reason part of this yet").call();
        Gallium.getPluginManager().unloadPlugins();
        // Gallium end

        logger.info("Stopping server");
        if (this.getConnection() != null) {
            this.getConnection().stop();
        }

        if (this.playerList != null) {
            logger.info("Saving players");
            this.playerList.saveAll();
            this.playerList.removeAll();
        }

        logger.info("Saving worlds");
        Iterator<ServerLevel> var1 = this.getAllLevels().iterator();

        ServerLevel serverLevel2;
        while(var1.hasNext()) {
            serverLevel2 = var1.next();
            if (serverLevel2 != null) {
                serverLevel2.noSave = false;
            }
        }

        this.saveAllChunks(false, true, false);
        var1 = this.getAllLevels().iterator();

        while(var1.hasNext()) {
            serverLevel2 = var1.next();
            if (serverLevel2 != null) {
                try {
                    serverLevel2.close();
                } catch (IOException var5) {
                    logger.error("Exception closing the level", var5);
                }
            }
        }

        if (this.snooper.isStarted()) {
            this.snooper.interrupt();
        }

        this.resources.close();

        try {
            this.storageSource.close();
        } catch (IOException var4) {
            logger.error("Failed to unlock level {}", this.storageSource.getLevelId(), var4);
        }

    }

    public String getLocalIp() {
        return this.localIp;
    }

    public void setLocalIp(String string) {
        this.localIp = string;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void halt(boolean bl) {
        this.running = false;
        if (bl) {
            try {
                this.serverThread.join();
            } catch (InterruptedException var3) {
                logger.error("Error while shutting down", var3);
            }
        }

    }

    protected void runServer() {
        try {
            if (this.initServer()) {
                this.nextTickTime = Util.getMillis();
                this.status.setDescription(new TextComponent(this.motd));
                this.status.setVersion(new ServerStatus.Version(SharedConstants.getCurrentVersion().getName(), SharedConstants.getCurrentVersion().getProtocolVersion()));
                this.updateStatusIcon(this.status);

                while(this.running) {
                    long l = Util.getMillis() - this.nextTickTime;
                    if (l > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L) {
                        long m = l / 50L;
                        logger.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", l, m);
                        this.nextTickTime += m * 50L;
                        this.lastOverloadWarning = this.nextTickTime;
                    }

                    if (this.debugCommandProfilerDelayStart) {
                        this.debugCommandProfilerDelayStart = false;
                        this.debugCommandProfiler = new TimeProfiler(Util.getNanos(), this.tickCount);
                    }

                    this.nextTickTime += 50L;
                    this.startMetricsRecordingTick();
                    this.profiler.push("tick");
                    this.tickServer(this::haveTime);
                    this.profiler.popPush("nextTickWait");
                    this.mayHaveDelayedTasks = true;
                    this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
                    this.waitUntilNextTick();
                    this.profiler.pop();
                    this.endMetricsRecordingTick();
                    this.isReady = true;
                }
            } else {
                this.onServerCrash((CrashReport)null);
            }
        } catch (Throwable var44) {
            logger.error("Encountered an unexpected exception", var44);
            CrashReport crashReport2;
            if (var44 instanceof ReportedException) {
                crashReport2 = ((ReportedException)var44).getReport();
            } else {
                crashReport2 = new CrashReport("Exception in server tick loop", var44);
            }

            this.fillSystemReport(crashReport2.getSystemReport());
            File var10002 = new File(this.getServerDirectory(), "crash-reports");
            SimpleDateFormat var10003 = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
            Date var10004 = new Date();
            File file = new File(var10002, "crash-" + var10003.format(var10004) + "-server.txt");
            if (crashReport2.saveToFile(file)) {
                logger.error("This crash report has been saved to: {}", file.getAbsolutePath());
            } else {
                logger.error("We were unable to save this crash report to disk.");
            }

            this.onServerCrash(crashReport2);
        } finally {
            try {
                this.stopped = true;
                this.stopServer();
            } catch (Throwable var42) {
                logger.error("Exception stopping the server", var42);
            } finally {
                this.onServerExit();
            }

        }

    }

    private boolean haveTime() {
        return this.runningTask() || Util.getMillis() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTime : this.nextTickTime);
    }

    protected void waitUntilNextTick() {
        this.runAllTasks();
        this.managedBlock(() -> {
            return !this.haveTime();
        });
    }

    protected TickTask wrapRunnable(Runnable runnable) {
        return new TickTask(this.tickCount, runnable);
    }

    protected boolean shouldRun(TickTask tickTask) {
        return tickTask.getTick() + 3 < this.tickCount || this.haveTime();
    }

    public boolean pollTask() {
        boolean bl = this.pollTaskInternal();
        this.mayHaveDelayedTasks = bl;
        return bl;
    }

    private boolean pollTaskInternal() {
        if (super.pollTask()) {
            return true;
        } else {
            if (this.haveTime()) {
                Iterator<ServerLevel> var1 = this.getAllLevels().iterator();

                while(var1.hasNext()) {
                    ServerLevel serverLevel = var1.next();
                    if (serverLevel.getChunkSource().pollTask()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public void doRunTask(TickTask tickTask) {
        this.getProfiler().incrementCounter("runTask");
        super.doRunTask(tickTask);
    }

    private void updateStatusIcon(ServerStatus serverStatus) {
        Optional<File> optional = Optional.of(this.getFile("server-icon.png")).filter(File::isFile);
        if (!optional.isPresent()) {
            optional = this.storageSource.getIconFile().map(Path::toFile).filter(File::isFile);
        }

        optional.ifPresent((file) -> {
            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                Validate.validState(bufferedImage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                Validate.validState(bufferedImage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
                byte[] bs = Base64.getEncoder().encode(byteArrayOutputStream.toByteArray());
                String var10001 = new String(bs, StandardCharsets.UTF_8);
                serverStatus.setFavicon("data:image/png;base64," + var10001);
            } catch (Exception var5) {
                logger.error("Couldn't load server icon", var5);
            }

        });
    }

    public Optional<Path> getWorldScreenshotFile() {
        return this.storageSource.getIconFile();
    }

    public File getServerDirectory() {
        return new File(".");
    }

    protected void onServerCrash(CrashReport crashReport) {
    }

    public void onServerExit() {
    }

    public void tickServer(BooleanSupplier booleanSupplier) {
        long l = Util.getNanos();
        ++this.tickCount;
        this.tickChildren(booleanSupplier);
        if (l - this.lastServerStatus >= 5000000000L) {
            this.lastServerStatus = l;
            this.status.setPlayers(new ServerStatus.Players(this.getMaxPlayers(), this.getPlayerCount()));
            GameProfile[] gameProfiles = new GameProfile[Math.min(this.getPlayerCount(), 12)];
            int i = Mth.nextInt(this.random, 0, this.getPlayerCount() - gameProfiles.length);

            for(int j = 0; j < gameProfiles.length; ++j) {
                gameProfiles[j] = ((ServerPlayer)this.playerList.getPlayers().get(i + j)).getGameProfile();
            }

            Collections.shuffle(Arrays.asList(gameProfiles));
            this.status.getPlayers().setSample(gameProfiles);
        }

        if (this.tickCount % 6000 == 0) {
            logger.debug("Autosave started");
            this.profiler.push("save");
            this.playerList.saveAll();
            this.saveAllChunks(true, false, false);
            this.profiler.pop();
            logger.debug("Autosave finished");
        }

        this.profiler.push("snooper");
        if (!this.snooper.isStarted() && this.tickCount > 100) {
            this.snooper.start();
        }

        if (this.tickCount % 6000 == 0) {
            this.snooper.prepare();
        }

        this.profiler.pop();
        this.profiler.push("tallying");
        long m = this.tickTimes[this.tickCount % 100] = Util.getNanos() - l;
        this.averageTickTime = this.averageTickTime * 0.8F + (float)m / 1000000.0F * 0.19999999F;
        long n = Util.getNanos();
        this.frameTimer.logFrameDuration(n - l);
        this.profiler.pop();
    }

    public void tickChildren(BooleanSupplier booleanSupplier) {
        this.profiler.push("commandFunctions");
        this.getFunctions().tick();
        this.profiler.popPush("levels");
        Iterator<ServerLevel> var2 = this.getAllLevels().iterator();

        while(var2.hasNext()) {
            ServerLevel serverLevel = var2.next();
            this.profiler.push(() -> {
                return "" + serverLevel + " " + serverLevel.dimension().location();
            });
            if (this.tickCount % 20 == 0) {
                this.profiler.push("timeSync");
                this.playerList.broadcastAll(new ClientboundSetTimePacket(serverLevel.getGameTime(), serverLevel.getDayTime(), serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), serverLevel.dimension());
                this.profiler.pop();
            }

            this.profiler.push("tick");

            try {
                serverLevel.tick(booleanSupplier);
            } catch (Throwable var6) {
                CrashReport crashReport = CrashReport.forThrowable(var6, "Exception ticking world");
                serverLevel.fillReportDetails(crashReport);
                throw new ReportedException(crashReport);
            }

            this.profiler.pop();
            this.profiler.pop();
        }

        this.profiler.popPush("connection");
        this.getConnection().tick();
        this.profiler.popPush("players");
        this.playerList.tick();
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            GameTestTicker.SINGLETON.tick();
        }

        this.profiler.popPush("server gui refresh");

        for(int i = 0; i < this.tickables.size(); ++i) {
            ((Runnable)this.tickables.get(i)).run();
        }

        this.profiler.pop();
    }

    public boolean isNetherEnabled() {
        return true;
    }

    public void addTickable(Runnable runnable) {
        this.tickables.add(runnable);
    }

    protected void setId(String string) {
        this.serverId = string;
    }

    public boolean isShutdown() {
        return !this.serverThread.isAlive();
    }

    public File getFile(String string) {
        return new File(this.getServerDirectory(), string);
    }

    public final ServerLevel overworld() {
        return (ServerLevel)this.levels.get(Level.OVERWORLD);
    }

    @Nullable
    public ServerLevel getLevel(ResourceKey<Level> resourceKey) {
        return (ServerLevel)this.levels.get(resourceKey);
    }

    public Set<ResourceKey<Level>> levelKeys() {
        return this.levels.keySet();
    }

    public Iterable<ServerLevel> getAllLevels() {
        return this.levels.values();
    }

    public String getServerVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    public int getPlayerCount() {
        return this.playerList.getPlayerCount();
    }

    public int getMaxPlayers() {
        return this.playerList.getMaxPlayers();
    }

    public String[] getPlayerNames() {
        return this.playerList.getPlayerNamesArray();
    }

    @DontObfuscate
    public String getServerModName() {
        return "Gallium";
    }

    public SystemReport fillSystemReport(SystemReport systemReport) {
        if (this.playerList != null) {
            systemReport.setDetail("Player Count", () -> {
                int var10000 = this.playerList.getPlayerCount();
                return "" + var10000 + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers();
            });
        }

        systemReport.setDetail("Data Packs", () -> {
            StringBuilder stringBuilder = new StringBuilder();
            Iterator<Pack> var2 = this.packRepository.getSelectedPacks().iterator();

            while(var2.hasNext()) {
                Pack pack = var2.next();
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }

                stringBuilder.append(pack.getId());
                if (!pack.getCompatibility().isCompatible()) {
                    stringBuilder.append(" (incompatible)");
                }
            }

            return stringBuilder.toString();
        });
        if (this.serverId != null) {
            systemReport.setDetail("Server Id", () -> {
                return this.serverId;
            });
        }

        return this.fillServerSystemReport(systemReport);
    }

    public abstract SystemReport fillServerSystemReport(SystemReport systemReport);

    public abstract Optional<String> getModdedStatus();

    public void sendMessage(Component component, UUID uUID) {
        logger.info(component.getString());
    }

    public KeyPair getKeyPair() {
        return this.keyPair;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int i) {
        this.port = i;
    }

    public String getSingleplayerName() {
        return this.singleplayerName;
    }

    public void setSingleplayerName(String string) {
        this.singleplayerName = string;
    }

    public boolean isSingleplayer() {
        return this.singleplayerName != null;
    }

    protected void initializeKeyPair() {
        logger.info("Generating keypair");

        try {
            this.keyPair = Crypt.generateKeyPair();
        } catch (CryptException var2) {
            throw new IllegalStateException("Failed to generate key pair", var2);
        }
    }

    public void setDifficulty(Difficulty difficulty, boolean bl) {
        if (bl || !this.worldData.isDifficultyLocked()) {
            this.worldData.setDifficulty(this.worldData.isHardcore() ? Difficulty.HARD : difficulty);
            this.updateMobSpawningFlags();
            this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
        }
    }

    public int getScaledTrackingDistance(int i) {
        return i;
    }

    private void updateMobSpawningFlags() {
        Iterator<ServerLevel> var1 = this.getAllLevels().iterator();

        while(var1.hasNext()) {
            ServerLevel serverLevel = var1.next();
            serverLevel.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
        }

    }

    public void setDifficultyLocked(boolean bl) {
        this.worldData.setDifficultyLocked(bl);
        this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
    }

    private void sendDifficultyUpdate(ServerPlayer serverPlayer) {
        LevelData levelData = serverPlayer.getLevel().getLevelData();
        serverPlayer.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
    }

    public boolean isSpawningMonsters() {
        return this.worldData.getDifficulty() != Difficulty.PEACEFUL;
    }

    public boolean isDemo() {
        return this.isDemo;
    }

    public void setDemo(boolean bl) {
        this.isDemo = bl;
    }

    public String getResourcePack() {
        return this.resourcePack;
    }

    public String getResourcePackHash() {
        return this.resourcePackHash;
    }

    public void setResourcePack(String string, String string2) {
        this.resourcePack = string;
        this.resourcePackHash = string2;
    }

    public void populateSnooper(Snooper snooper) {
        snooper.setDynamicData("whitelist_enabled", false);
        snooper.setDynamicData("whitelist_count", 0);
        if (this.playerList != null) {
            snooper.setDynamicData("players_current", this.getPlayerCount());
            snooper.setDynamicData("players_max", this.getMaxPlayers());
            snooper.setDynamicData("players_seen", this.playerDataStorage.getSeenPlayers().length);
        }

        snooper.setDynamicData("uses_auth", this.onlineMode);
        snooper.setDynamicData("gui_state", this.hasGui() ? "enabled" : "disabled");
        snooper.setDynamicData("run_time", (Util.getMillis() - snooper.getStartupTime()) / 60L * 1000L);
        snooper.setDynamicData("avg_tick_ms", (int)(Mth.average(this.tickTimes) * 1.0E-6));
        int i = 0;
        Iterator<ServerLevel> var3 = this.getAllLevels().iterator();

        while(var3.hasNext()) {
            ServerLevel serverLevel = var3.next();
            if (serverLevel != null) {
                snooper.setDynamicData("world[" + i + "][dimension]", serverLevel.dimension().location());
                snooper.setDynamicData("world[" + i + "][mode]", this.worldData.getGameType());
                snooper.setDynamicData("world[" + i + "][difficulty]", serverLevel.getDifficulty());
                snooper.setDynamicData("world[" + i + "][hardcore]", this.worldData.isHardcore());
                snooper.setDynamicData("world[" + i + "][height]", serverLevel.getMaxBuildHeight());
                snooper.setDynamicData("world[" + i + "][chunks_loaded]", serverLevel.getChunkSource().getLoadedChunksCount());
                ++i;
            }
        }

        snooper.setDynamicData("worlds", i);
    }

    public void populateSnooperInitial(Snooper snooper) {
        snooper.setFixedData("singleplayer", this.isSingleplayer());
        snooper.setFixedData("server_brand", this.getServerModName());
        snooper.setFixedData("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
        snooper.setFixedData("dedicated", this.isDedicatedServer());
    }

    public boolean isSnooperEnabled() {
        return true;
    }

    public abstract boolean isDedicatedServer();

    public abstract int getRateLimitPacketsPerSecond();

    public boolean usesAuthentication() {
        return this.onlineMode;
    }

    public void setUsesAuthentication(boolean bl) {
        this.onlineMode = bl;
    }

    public boolean getPreventProxyConnections() {
        return this.preventProxyConnections;
    }

    public void setPreventProxyConnections(boolean bl) {
        this.preventProxyConnections = bl;
    }

    public boolean isSpawningAnimals() {
        return true;
    }

    public boolean areNpcsEnabled() {
        return true;
    }

    public abstract boolean isEpollEnabled();

    public boolean isPvpAllowed() {
        return this.pvp;
    }

    public void setPvpAllowed(boolean bl) {
        this.pvp = bl;
    }

    public boolean isFlightAllowed() {
        return this.allowFlight;
    }

    public void setFlightAllowed(boolean bl) {
        this.allowFlight = bl;
    }

    public abstract boolean isCommandBlockEnabled();

    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String string) {
        this.motd = string;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public PlayerList getPlayerList() {
        return this.playerList;
    }

    public void setPlayerList(PlayerList playerList) {
        this.playerList = playerList;
    }

    public abstract boolean isPublished();

    public void setDefaultGameType(GameType gameType) {
        this.worldData.setGameType(gameType);
    }

    @Nullable
    public ServerConnectionListener getConnection() {
        return this.connection;
    }

    public boolean isReady() {
        return this.isReady;
    }

    public boolean hasGui() {
        return false;
    }

    public boolean publishServer(@Nullable GameType gameType, boolean bl, int i) {
        return false;
    }

    public int getTickCount() {
        return this.tickCount;
    }

    public Snooper getSnooper() {
        return this.snooper;
    }

    public int getSpawnProtectionRadius() {
        return 16;
    }

    public boolean isUnderSpawnProtection(ServerLevel serverLevel, BlockPos blockPos, Player player) {
        return false;
    }

    public boolean repliesToStatus() {
        return true;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public int getPlayerIdleTimeout() {
        return this.playerIdleTimeout;
    }

    public void setPlayerIdleTimeout(int i) {
        this.playerIdleTimeout = i;
    }

    public MinecraftSessionService getSessionService() {
        return this.sessionService;
    }

    public GameProfileRepository getProfileRepository() {
        return this.profileRepository;
    }

    public GameProfileCache getProfileCache() {
        return this.profileCache;
    }

    public ServerStatus getStatus() {
        return this.status;
    }

    public void invalidateStatus() {
        this.lastServerStatus = 0L;
    }

    public int getAbsoluteMaxWorldSize() {
        return 29999984;
    }

    public boolean scheduleExecutables() {
        return super.scheduleExecutables() && !this.isStopped();
    }

    public Thread getRunningThread() {
        return this.serverThread;
    }

    public int getCompressionThreshold() {
        return 256;
    }

    public long getNextTickTime() {
        return this.nextTickTime;
    }

    public DataFixer getFixerUpper() {
        return this.fixerUpper;
    }

    public int getSpawnRadius(@Nullable ServerLevel serverLevel) {
        return serverLevel != null ? serverLevel.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS) : 10;
    }

    public ServerAdvancementManager getAdvancements() {
        return this.resources.getAdvancements();
    }

    public ServerFunctionManager getFunctions() {
        return this.functionManager;
    }

    public CompletableFuture<Void> reloadResources(Collection<String> collection) {
        CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
            Stream<String> var10000 = collection.stream(); // Gallium
            PackRepository var10001 = this.packRepository;
            Objects.requireNonNull(var10001);
            return (ImmutableList)var10000.map(var10001::getPack).filter(Objects::nonNull).map(Pack::open).collect(ImmutableList.toImmutableList());
        }, this).thenCompose((immutableList) -> {
            return ServerResources.loadResources(immutableList, this.registryHolder, this.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED, this.getFunctionCompilationLevel(), this.executor, this);
        }).thenAcceptAsync((serverResources) -> {
            this.resources.close();
            this.resources = (ServerResources) serverResources;
            this.packRepository.setSelected(collection);
            this.worldData.setDataPackConfig(getSelectedPacks(this.packRepository));
            ((ServerResources) serverResources).updateGlobals();
            this.getPlayerList().saveAll();
            this.getPlayerList().reloadResources();
            this.functionManager.replaceLibrary(this.resources.getFunctionLibrary());
            this.structureManager.onResourceManagerReload(this.resources.getResourceManager());
        }, this);
        if (this.isSameThread()) {
            Objects.requireNonNull(completableFuture);
            this.managedBlock(completableFuture::isDone);
        }

        return completableFuture;
    }

    public static DataPackConfig configurePackRepository(PackRepository packRepository, DataPackConfig dataPackConfig, boolean bl) {
        packRepository.reload();
        if (bl) {
            packRepository.setSelected(Collections.singleton("vanilla"));
            return new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of());
        } else {
            Set<String> set = Sets.newLinkedHashSet();
            Iterator var4 = dataPackConfig.getEnabled().iterator();

            while(var4.hasNext()) {
                String string = (String)var4.next();
                if (packRepository.isAvailable(string)) {
                    set.add(string);
                } else {
                    LOGGER.warn("Missing data pack {}", string);
                }
            }

            var4 = packRepository.getAvailablePacks().iterator();

            while(var4.hasNext()) {
                Pack pack = (Pack)var4.next();
                String string2 = pack.getId();
                if (!dataPackConfig.getDisabled().contains(string2) && !set.contains(string2)) {
                    LOGGER.info("Found new data pack {}, loading it automatically", string2);
                    set.add(string2);
                }
            }

            if (set.isEmpty()) {
                LOGGER.info("No datapacks selected, forcing vanilla");
                set.add("vanilla");
            }

            packRepository.setSelected(set);
            return getSelectedPacks(packRepository);
        }
    }

    private static DataPackConfig getSelectedPacks(PackRepository packRepository) {
        Collection<String> collection = packRepository.getSelectedIds();
        List<String> list = ImmutableList.copyOf(collection);
        List<String> list2 = (List<String>)packRepository.getAvailableIds().stream().filter((string) -> {
            return !collection.contains(string);
        }).collect(ImmutableList.toImmutableList());
        return new DataPackConfig(list, list2);
    }

    public void kickUnlistedPlayers(CommandSourceStack commandSourceStack) {
        if (this.isEnforceWhitelist()) {
            PlayerList playerList = commandSourceStack.getServer().getPlayerList();
            UserWhiteList userWhiteList = playerList.getWhiteList();
            List<ServerPlayer> list = Lists.newArrayList(playerList.getPlayers());
            Iterator<ServerPlayer> var5 = list.iterator();

            while(var5.hasNext()) {
                ServerPlayer serverPlayer = var5.next();
                if (!userWhiteList.isWhiteListed(serverPlayer.getGameProfile())) {
                    serverPlayer.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.not_whitelisted"));
                }
            }

        }
    }

    public PackRepository getPackRepository() {
        return this.packRepository;
    }

    public Commands getCommands() {
        return this.resources.getCommands();
    }

    public CommandSourceStack createCommandSourceStack() {
        ServerLevel serverLevel = this.overworld();
        return new CommandSourceStack(this, serverLevel == null ? Vec3.ZERO : Vec3.atLowerCornerOf(serverLevel.getSharedSpawnPos()), Vec2.ZERO, serverLevel, 4, "Server", new TextComponent("Server"), this, (Entity)null);
    }

    public boolean acceptsSuccess() {
        return true;
    }

    public boolean acceptsFailure() {
        return true;
    }

    public abstract boolean shouldInformAdmins();

    public RecipeManager getRecipeManager() {
        return this.resources.getRecipeManager();
    }

    public TagContainer getTags() {
        return this.resources.getTags();
    }

    public ServerScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public CommandStorage getCommandStorage() {
        if (this.commandStorage == null) {
            throw new NullPointerException("Called before server init");
        } else {
            return this.commandStorage;
        }
    }

    public LootTables getLootTables() {
        return this.resources.getLootTables();
    }

    public PredicateManager getPredicateManager() {
        return this.resources.getPredicateManager();
    }

    public ItemModifierManager getItemModifierManager() {
        return this.resources.getItemModifierManager();
    }

    public GameRules getGameRules() {
        return this.overworld().getGameRules();
    }

    public CustomBossEvents getCustomBossEvents() {
        return this.customBossEvents;
    }

    public boolean isEnforceWhitelist() {
        return this.enforceWhitelist;
    }

    public void setEnforceWhitelist(boolean bl) {
        this.enforceWhitelist = bl;
    }

    public float getAverageTickTime() {
        return this.averageTickTime;
    }

    public int getProfilePermissions(GameProfile gameProfile) {
        if (this.getPlayerList().isOp(gameProfile)) {
            ServerOpListEntry serverOpListEntry = (ServerOpListEntry)this.getPlayerList().getOps().get(gameProfile);
            if (serverOpListEntry != null) {
                return serverOpListEntry.getLevel();
            } else if (this.isSingleplayerOwner(gameProfile)) {
                return 4;
            } else if (this.isSingleplayer()) {
                return this.getPlayerList().isAllowCheatsForAllPlayers() ? 4 : 0;
            } else {
                return this.getOperatorUserPermissionLevel();
            }
        } else {
            return 0;
        }
    }

    public FrameTimer getFrameTimer() {
        return this.frameTimer;
    }

    public ProfilerFiller getProfiler() {
        return this.profiler;
    }

    public abstract boolean isSingleplayerOwner(GameProfile gameProfile);

    public void dumpServerProperties(Path path) throws IOException {
    }

    private void saveDebugReport(Path path) {
        Path path2 = path.resolve("levels");

        try {
            Iterator<Map.Entry<ResourceKey<Level>, ServerLevel>> var3 = this.levels.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<ResourceKey<Level>, ServerLevel> entry = var3.next();
                ResourceLocation resourceLocation = ((ResourceKey)entry.getKey()).location();
                Path path3 = path2.resolve(resourceLocation.getNamespace()).resolve(resourceLocation.getPath());
                Files.createDirectories(path3);
                ((ServerLevel)entry.getValue()).saveDebugReport(path3);
            }

            this.dumpGameRules(path.resolve("gamerules.txt"));
            this.dumpClasspath(path.resolve("classpath.txt"));
            this.dumpMiscStats(path.resolve("stats.txt"));
            this.dumpThreads(path.resolve("threads.txt"));
            this.dumpServerProperties(path.resolve("server.properties.txt"));
        } catch (IOException var7) {
            logger.warn("Failed to save debug report", var7);
        }

    }

    private void dumpMiscStats(Path path) throws IOException {
        Writer writer = Files.newBufferedWriter(path);

        try {
            writer.write(String.format("pending_tasks: %d\n", this.getPendingTasksCount()));
            writer.write(String.format("average_tick_time: %f\n", this.getAverageTickTime()));
            writer.write(String.format("tick_times: %s\n", Arrays.toString(this.tickTimes)));
            writer.write(String.format("queue: %s\n", Util.backgroundExecutor()));
        } catch (Throwable var6) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }
            }

            throw var6;
        }

        if (writer != null) {
            writer.close();
        }

    }

    private void dumpGameRules(Path path) throws IOException {
        Writer writer = Files.newBufferedWriter(path);

        try {
            final List<String> list = Lists.newArrayList();
            final GameRules gameRules = this.getGameRules();
            GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
                public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                    list.add(String.format("%s=%s\n", key.getId(), gameRules.getRule(key)));
                }
            });
            Iterator<String> var5 = list.iterator();

            while(var5.hasNext()) {
                String string = var5.next();
                writer.write(string);
            }
        } catch (Throwable var8) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable var7) {
                    var8.addSuppressed(var7);
                }
            }

            throw var8;
        }

        if (writer != null) {
            writer.close();
        }

    }

    private void dumpClasspath(Path path) throws IOException {
        Writer writer = Files.newBufferedWriter(path);

        try {
            String string = System.getProperty("java.class.path");
            String string2 = System.getProperty("path.separator");
            Iterator<String> var5 = Splitter.on(string2).split(string).iterator();

            while(var5.hasNext()) {
                String string3 = var5.next();
                writer.write(string3);
                writer.write("\n");
            }
        } catch (Throwable var8) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable var7) {
                    var8.addSuppressed(var7);
                }
            }

            throw var8;
        }

        if (writer != null) {
            writer.close();
        }

    }

    private void dumpThreads(Path path) throws IOException {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        Arrays.sort(threadInfos, Comparator.comparing(ThreadInfo::getThreadName));
        Writer writer = Files.newBufferedWriter(path);

        try {
            ThreadInfo[] var5 = threadInfos;
            int var6 = threadInfos.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                ThreadInfo threadInfo = var5[var7];
                writer.write(threadInfo.toString());
                writer.write(10);
            }
        } catch (Throwable var10) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable var9) {
                    var10.addSuppressed(var9);
                }
            }

            throw var10;
        }

        if (writer != null) {
            writer.close();
        }

    }

    private void startMetricsRecordingTick() {
        if (this.willStartRecordingMetrics) {
            this.metricsRecorder = ActiveMetricsRecorder.createStarted(new ServerMetricsSamplersProvider(Util.timeSource, this.isDedicatedServer()), Util.timeSource, Util.ioPool(), new MetricsPersister("server"), this.onMetricsRecordingStopped, (path) -> {
                this.executeBlocking(() -> {
                    this.saveDebugReport(path.resolve("server"));
                });
                this.onMetricsRecordingFinished.accept(path);
            });
            this.willStartRecordingMetrics = false;
        }

        this.profiler = SingleTickProfiler.decorateFiller(this.metricsRecorder.getProfiler(), SingleTickProfiler.createTickProfiler("Server"));
        this.metricsRecorder.startTick();
        this.profiler.startTick();
    }

    private void endMetricsRecordingTick() {
        this.profiler.endTick();
        this.metricsRecorder.endTick();
    }

    public boolean isRecordingMetrics() {
        return this.metricsRecorder.isRecording();
    }

    public void startRecordingMetrics(Consumer<ProfileResults> consumer, Consumer<Path> consumer2) {
        this.onMetricsRecordingStopped = (profileResults) -> {
            this.stopRecordingMetrics();
            consumer.accept(profileResults);
        };
        this.onMetricsRecordingFinished = consumer2;
        this.willStartRecordingMetrics = true;
    }

    public void stopRecordingMetrics() {
        this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    }

    public void finishRecordingMetrics() {
        this.metricsRecorder.end();
    }

    public Path getWorldPath(LevelResource levelResource) {
        return this.storageSource.getLevelPath(levelResource);
    }

    public boolean forceSynchronousWrites() {
        return true;
    }

    public StructureManager getStructureManager() {
        return this.structureManager;
    }

    public WorldData getWorldData() {
        return this.worldData;
    }

    public RegistryAccess registryAccess() {
        return this.registryHolder;
    }

    public TextFilter createTextFilterForPlayer(ServerPlayer serverPlayer) {
        return TextFilter.DUMMY;
    }

    public boolean isResourcePackRequired() {
        return false;
    }

    public ServerPlayerGameMode createGameModeForPlayer(ServerPlayer serverPlayer) {
        return (ServerPlayerGameMode)(this.isDemo() ? new DemoMode(serverPlayer) : new ServerPlayerGameMode(serverPlayer));
    }

    @Nullable
    public GameType getForcedGameType() {
        return null;
    }

    public ResourceManager getResourceManager() {
        return this.resources.getResourceManager();
    }

    @Nullable
    public Component getResourcePackPrompt() {
        return null;
    }

    public boolean isTimeProfilerRunning() {
        return this.debugCommandProfilerDelayStart || this.debugCommandProfiler != null;
    }

    public void startTimeProfiler() {
        this.debugCommandProfilerDelayStart = true;
    }

    public ProfileResults stopTimeProfiler() {
        if (this.debugCommandProfiler == null) {
            return EmptyProfileResults.EMPTY;
        } else {
            ProfileResults profileResults = this.debugCommandProfiler.stop(Util.getNanos(), this.tickCount);
            this.debugCommandProfiler = null;
            return profileResults;
        }
    }

    static {
        DEMO_SETTINGS = new LevelSettings("Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), DataPackConfig.DEFAULT);
    }

    private static class TimeProfiler {
        final long startNanos;
        final int startTick;

        TimeProfiler(long l, int i) {
            this.startNanos = l;
            this.startTick = i;
        }

        ProfileResults stop(final long l, final int i) {
            return new ProfileResults() {
                public List<ResultField> getTimes(String string) {
                    return Collections.emptyList();
                }

                public boolean saveResults(Path path) {
                    return false;
                }

                public long getStartTimeNano() {
                    return TimeProfiler.this.startNanos;
                }

                public int getStartTimeTicks() {
                    return TimeProfiler.this.startTick;
                }

                public long getEndTimeNano() {
                    return l;
                }

                public int getEndTimeTicks() {
                    return i;
                }

                public String getProfilerResults() {
                    return "";
                }
            };
        }
    }
}
