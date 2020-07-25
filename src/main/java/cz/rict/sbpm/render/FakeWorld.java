package cz.rict.sbpm.render;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.Explosion;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ITickList;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class FakeWorld extends World
{
    public static final FakeWorld INSTANCE = new FakeWorld();
    private Map<BlockPos, BlockInfo> blocks;

    public FakeWorld()
    {
        super(new FakeWorldInfo(), DimensionType.OVERWORLD, (a, b) -> null, Minecraft.getInstance().getProfiler(), false);
    }

    public void setBlocks(final List<BlockInfo> blocksIn)
    {
        if (blocksIn == null)
        {
            blocks = null;
            return;
        }
        blocks = new Object2ObjectOpenHashMap<>(blocksIn.size());
        blocksIn.forEach(bi -> blocks.put(bi.pos, bi));
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        if (blocks.containsKey(pos) && blocks.get(pos).nbt != null)
        {
            final TileEntity te = TileEntity.create(blocks.get(pos).nbt);
            te.setWorldAndPos(this, pos);
            return te;
        }
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos)
    {
        return blocks.containsKey(pos) ? blocks.get(pos).state : Blocks.AIR.getDefaultState();
    }

    @Override
    public IFluidState getFluidState(BlockPos pos)
    {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getBlockColor(BlockPos blockPosIn, ColorResolver colorResolverIn)
    {
        return -1;
    }

    @Override
    public boolean canSeeSky(BlockPos blockPosIn)
    {
        return true;
    }

    @Override
    public int getLightFor(LightType lightTypeIn, BlockPos blockPosIn)
    {
        return 15;
    }

    @Override
    public int getLightSubtracted(BlockPos blockPosIn, int amount)
    {
        return 15;
    }

    @Override
    public boolean addTileEntity(final TileEntity tile)
    {
        // TODO Auto-generated method stub
        return super.addTileEntity(tile);
    }

    @Override
    public IChunk getChunk(final int x, final int z, final ChunkStatus requiredStatus, final boolean nonnull)
    {
        // TODO Auto-generated method stub
        return super.getChunk(x, z, requiredStatus, nonnull);
    }

    @Override
    public int getHeight(final Type heightmapType, final int x, final int z)
    {
        // TODO Auto-generated method stub, is this needed?
        return super.getHeight(heightmapType, x, z);
    }

    @Override
    public boolean isBlockPresent(final BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.isBlockPresent(pos);
    }

    @Override
    public boolean isTopSolid(final BlockPos pos, final Entity entityIn)
    {
        // TODO Auto-generated method stub
        return super.isTopSolid(pos, entityIn);
    }

    @Override
    public ITickList<Block> getPendingBlockTicks()
    {
        return null;
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks()
    {
        return null;
    }

    @Override
    public void playEvent(final PlayerEntity player, final int type, final BlockPos pos, final int data)
    {
        // Noop
    }

    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        return null;
    }

    @Override
    public Biome getNoiseBiomeRaw(final int x, final int y, final int z)
    {
        return null;
    }

    @Override
    public void notifyBlockUpdate(final BlockPos pos, final BlockState oldState, final BlockState newState, final int flags)
    {
        // Noop
    }

    @Override
    public void playSound(final PlayerEntity player,
        final double x,
        final double y,
        final double z,
        final SoundEvent soundIn,
        final SoundCategory category,
        final float volume,
        final float pitch)
    {
        // Noop
    }

    @Override
    public void playMovingSound(final PlayerEntity playerIn,
        final Entity entityIn,
        final SoundEvent eventIn,
        final SoundCategory categoryIn,
        final float volume,
        final float pitch)
    {
        // Noop
    }

    @Override
    public Entity getEntityByID(final int id)
    {
        // Noop, hopefully not needed
        return null;
    }

    @Override
    public MapData getMapData(final String mapName)
    {
        return null;
    }

    @Override
    public void registerMapData(final MapData mapDataIn)
    {
        // Noop
    }

    @Override
    public int getNextMapId()
    {
        return 0;
    }

    @Override
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress)
    {
        // Noop
    }

    @Override
    public Scoreboard getScoreboard()
    {
        return null;
    }

    @Override
    public RecipeManager getRecipeManager()
    {
        return null;
    }

    @Override
    public NetworkTagManager getTags()
    {
        return null;
    }

    @Override
    public void addBlockEvent(final BlockPos pos, final Block blockIn, final int eventID, final int eventParam)
    {
        // Noop, we are static world
    }

    @Override
    public void addOptionalParticle(final IParticleData particleData,
        final double x,
        final double y,
        final double z,
        final double xSpeed,
        final double ySpeed,
        final double zSpeed)
    {
        // Noop
    }

    @Override
    public void addOptionalParticle(final IParticleData particleData,
        final boolean ignoreRange,
        final double x,
        final double y,
        final double z,
        final double xSpeed,
        final double ySpeed,
        final double zSpeed)
    {
        // Noop
    }

    @Override
    public void addParticle(final IParticleData particleData,
        final double x,
        final double y,
        final double z,
        final double xSpeed,
        final double ySpeed,
        final double zSpeed)
    {
        // Noop
    }

    @Override
    public void addParticle(final IParticleData particleData,
        final boolean forceAlwaysRender,
        final double x,
        final double y,
        final double z,
        final double xSpeed,
        final double ySpeed,
        final double zSpeed)
    {
        // Noop
    }

    @Override
    public void addTileEntities(final Collection<TileEntity> tileEntityCollection)
    {
        for (final TileEntity tileentity : tileEntityCollection)
        {
            this.addTileEntity(tileentity);
        }
    }

    @Override
    protected void advanceTime()
    {
        // Noop
    }

    @Override
    public void calculateInitialSkylight()
    {
        // Noop
    }

    @Override
    protected void calculateInitialWeather()
    {
        // Noop
    }

    @Override
    public void calculateInitialWeatherBody()
    {
        // Noop
    }

    @Override
    public boolean canMineBlockBody(final PlayerEntity player, final BlockPos pos)
    {
        // Noop, we are static world
        return false;
    }

    @Override
    public boolean checkBlockCollision(final AxisAlignedBB bb)
    {
        return super.checkBlockCollision(bb);
    }

    @Override
    public void close() throws IOException
    {
        // Noop
    }

    @Override
    public Explosion createExplosion(final Entity entityIn,
        final double xIn,
        final double yIn,
        final double zIn,
        final float explosionRadius,
        final Mode modeIn)
    {
        // Noop
        return null;
    }

    @Override
    public Explosion createExplosion(final Entity entityIn,
        final double xIn,
        final double yIn,
        final double zIn,
        final float explosionRadius,
        final boolean causesFire,
        final Mode modeIn)
    {
        // Noop
        return null;
    }

    @Override
    public Explosion createExplosion(final Entity entityIn,
        final DamageSource damageSourceIn,
        final double xIn,
        final double yIn,
        final double zIn,
        final float explosionRadius,
        final boolean causesFire,
        final Mode modeIn)
    {
        // Noop
        return null;
    }

    @Override
    public boolean extinguishFire(final PlayerEntity player, final BlockPos pos, final Direction side)
    {
        // Noop
        return false;
    }

    @Override
    public CrashReportCategory fillCrashReport(final CrashReport report)
    {
        final CrashReportCategory crashreportcategory = report.makeCategoryDepth("sbpm Rendering Wrapper", 1);
        crashreportcategory.addDetail("Rendered data",
            () -> {
                // TODO Auto-generated method stub
                return "";
            });
        return crashreportcategory;
    }

    @Override
    public BlockState findBlockstateInArea(final AxisAlignedBB area, final Block blockIn)
    {
        return super.findBlockstateInArea(area, blockIn);
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesWithinAABB(final Class<? extends T> p_225316_1_,
        final AxisAlignedBB p_225316_2_,
        final Predicate<? super T> p_225316_3_)
    {
        return super.getLoadedEntitiesWithinAABB(p_225316_1_, p_225316_2_, p_225316_3_);
    }

    @Override
    public boolean destroyBlock(final BlockPos p_225521_1_, final boolean p_225521_2_, final Entity p_225521_3_)
    {
        // Noop, we are static world
        return false;
    }

    @Override
    public int getActualHeight()
    {
        return super.getActualHeight();
    }

    @Override
    public BiomeManager getBiomeManager()
    {
        // Noop
        return null;
    }

    @Override
    public BlockPos getBlockRandomPos(final int p_217383_1_, final int p_217383_2_, final int p_217383_3_, final int p_217383_4_)
    {
        return super.getBlockRandomPos(p_217383_1_, p_217383_2_, p_217383_3_, p_217383_4_);
    }

    @Override
    public IBlockReader getBlockReader(final int chunkX, final int chunkZ)
    {
        return super.getBlockReader(chunkX, chunkZ);
    }

    @Override
    public float getCelestialAngleRadians(final float partialTicks)
    {
        // Noop
        return 0.0f;
    }

    @Override
    public Chunk getChunk(final int chunkX, final int chunkZ)
    {
        // Noop, we don't have vanilla chunks
        return null;
    }

    @Override
    public Chunk getChunkAt(final BlockPos pos)
    {
        // Noop, we don't have vanilla chunks
        return null;
    }

    @Override
    public AbstractChunkProvider getChunkProvider()
    {
        // Noop
        return null;
    }

    @Override
    public long getDayTime()
    {
        // Noop
        return 0;
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(final BlockPos pos)
    {
        // Noop
        return null;
    }

    @Override
    public Dimension getDimension()
    {
        // Noop
        return null;
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(final Entity entityIn,
        final AxisAlignedBB boundingBox,
        final Predicate<? super Entity> predicate)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(final EntityType<T> type,
        final AxisAlignedBB boundingBox,
        final Predicate<? super T> predicate)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(final Class<? extends T> clazz,
        final AxisAlignedBB aabb,
        final Predicate<? super T> filter)
    {
        // Noop
        return null;
    }

    @Override
    public GameRules getGameRules()
    {
        // Noop
        return null;
    }

    @Override
    public long getGameTime()
    {
        // Noop
        return 0;
    }

    @Override
    public BlockState getGroundAboveSeaLevel(final BlockPos pos)
    {
        return super.getGroundAboveSeaLevel(pos);
    }

    @Override
    public WorldLightManager getLightManager()
    {
        // TODO Auto-generated method stub
        return super.getLightManager();
    }

    @Override
    public double getMaxEntityRadius()
    {
        // Noop
        return 0.0d;
    }

    @Override
    public IProfiler getProfiler()
    {
        return super.getProfiler();
    }

    @Override
    public String getProviderName()
    {
        // Noop
        return "sbpm Rendering Wrapper";
    }

    @Override
    public float getRainStrength(final float delta)
    {
        // Noop
        return 0.0f;
    }

    @Override
    public Random getRandom()
    {
        return super.getRandom();
    }

    @Override
    public int getRedstonePower(final BlockPos pos, final Direction facing)
    {
        return super.getRedstonePower(pos, facing);
    }

    @Override
    public int getRedstonePowerFromNeighbors(final BlockPos pos)
    {
        return super.getRedstonePowerFromNeighbors(pos);
    }

    @Override
    public int getSeaLevel()
    {
        // Noop
        return 0;
    }

    @Override
    public long getSeed()
    {
        // Noop
        return 0;
    }

    @Override
    public MinecraftServer getServer()
    {
        // Noop
        return null;
    }

    @Override
    public int getSkylightSubtracted()
    {
        // Noop
        return 0;
    }

    @Override
    public BlockPos getSpawnPoint()
    {
        // Noop
        return null;
    }

    @Override
    public int getStrongPower(final BlockPos pos)
    {
        // Noop
        return 0;
    }

    @Override
    public float getThunderStrength(final float delta)
    {
        // Noop
        return 0.0f;
    }

    @Override
    public World getWorld()
    {
        return super.getWorld();
    }

    @Override
    public WorldBorder getWorldBorder()
    {
        // Noop
        return null;
    }

    @Override
    public WorldInfo getWorldInfo()
    {
        // Noop
        return null;
    }

    @Override
    public WorldType getWorldType()
    {
        // Noop
        return null;
    }

    @Override
    public void guardEntityTick(final Consumer<Entity> consumerEntity, final Entity entityIn)
    {
        // Noop
    }

    @Override
    public boolean hasBlockState(final BlockPos p_217375_1_, final Predicate<BlockState> p_217375_2_)
    {
        return super.hasBlockState(p_217375_1_, p_217375_2_);
    }

    @Override
    public double increaseMaxEntityRadius(final double value)
    {
        // Noop
        return 0.0d;
    }

    @Override
    public boolean isBlockModifiable(final PlayerEntity player, final BlockPos pos)
    {
        // Noop
        return false;
    }

    @Override
    public boolean isBlockPowered(final BlockPos pos)
    {
        return super.isBlockPowered(pos);
    }

    @Override
    public boolean isBlockinHighHumidity(final BlockPos pos)
    {
        // Noop
        return false;
    }

    @Override
    public boolean isDaytime()
    {
        // Noop
        return true;
    }

    @Override
    public boolean isFlammableWithin(final AxisAlignedBB bb)
    {
        // Noop
        return false;
    }

    @Override
    public boolean isMaterialInBB(final AxisAlignedBB bb, final Material materialIn)
    {
        return super.isMaterialInBB(bb, materialIn);
    }

    @Override
    public boolean isNightTime()
    {
        // Noop
        return false;
    }

    @Override
    public boolean isRaining()
    {
        // Noop
        return false;
    }

    @Override
    public boolean isRainingAt(final BlockPos position)
    {
        // Noop
        return false;
    }

    @Override
    public boolean isRemote()
    {
        // Noop
        return false;
    }

    @Override
    public boolean isSaveDisabled()
    {
        // Noop
        return true;
    }

    @Override
    public boolean isSidePowered(final BlockPos pos, final Direction side)
    {
        return super.isSidePowered(pos, side);
    }

    @Override
    public boolean isThundering()
    {
        // Noop
        return false;
    }

    @Override
    public void makeFireworks(final double x,
        final double y,
        final double z,
        final double motionX,
        final double motionY,
        final double motionZ,
        final CompoundNBT compound)
    {
        // Noop
    }

    @Override
    public void markAndNotifyBlock(final BlockPos pos,
        final Chunk chunk,
        final BlockState blockstate,
        final BlockState newState,
        final int flags)
    {
        // TODO Auto-generated method stub
        super.markAndNotifyBlock(pos, chunk, blockstate, newState, flags);
    }

    @Override
    public void markBlockRangeForRenderUpdate(final BlockPos blockPosIn, final BlockState oldState, final BlockState newState)
    {
        // TODO Auto-generated method stub
        super.markBlockRangeForRenderUpdate(blockPosIn, oldState, newState);
    }

    @Override
    public void markChunkDirty(final BlockPos pos, final TileEntity unusedTileEntity)
    {
        // Noop
    }

    @Override
    public void neighborChanged(final BlockPos pos, final Block blockIn, final BlockPos fromPos)
    {
        // TODO Auto-generated method stub
        super.neighborChanged(pos, blockIn, fromPos);
    }

    @Override
    public void notifyNeighbors(final BlockPos pos, final Block blockIn)
    {
        // TODO Auto-generated method stub
        super.notifyNeighbors(pos, blockIn);
    }

    @Override
    public void notifyNeighborsOfStateChange(final BlockPos pos, final Block blockIn)
    {
        // TODO Auto-generated method stub
        super.notifyNeighborsOfStateChange(pos, blockIn);
    }

    @Override
    public void notifyNeighborsOfStateExcept(final BlockPos pos, final Block blockType, final Direction skipSide)
    {
        // TODO Auto-generated method stub
        super.notifyNeighborsOfStateExcept(pos, blockType, skipSide);
    }

    @Override
    public void onBlockStateChange(final BlockPos p_217393_1_, final BlockState p_217393_2_, final BlockState p_217393_3_)
    {
        // TODO Auto-generated method stub
        super.onBlockStateChange(p_217393_1_, p_217393_2_, p_217393_3_);
    }

    @Override
    public void playBroadcastSound(final int id, final BlockPos pos, final int data)
    {
        // Noop
    }

    @Override
    public void playSound(final PlayerEntity player,
        final BlockPos pos,
        final SoundEvent soundIn,
        final SoundCategory category,
        final float volume,
        final float pitch)
    {
        // Noop
    }

    @Override
    public void playSound(final double x,
        final double y,
        final double z,
        final SoundEvent soundIn,
        final SoundCategory category,
        final float volume,
        final float pitch,
        final boolean distanceDelay)
    {
        // Noop
    }

    @Override
    public boolean removeBlock(final BlockPos pos, final boolean isMoving)
    {
        // TODO Auto-generated method stub
        return super.removeBlock(pos, isMoving);
    }

    @Override
    public void removeTileEntity(final BlockPos pos)
    {
        // TODO Auto-generated method stub
        super.removeTileEntity(pos);
    }

    @Override
    public void sendPacketToServer(final IPacket<?> packetIn)
    {
        // Noop
    }

    @Override
    public void sendQuittingDisconnectingPacket()
    {
        // Noop
    }

    @Override
    public void setAllowedSpawnTypes(final boolean hostile, final boolean peaceful)
    {
        // Noop
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState state)
    {
        // TODO Auto-generated method stub
        return super.setBlockState(pos, state);
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState newState, final int flags)
    {
        // TODO Auto-generated method stub
        return super.setBlockState(pos, newState, flags);
    }

    @Override
    public void setDayTime(final long time)
    {
        // Noop
    }

    @Override
    public void setEntityState(final Entity entityIn, final byte state)
    {
        // Noop
    }

    @Override
    public void setGameTime(final long worldTime)
    {
        // Noop
    }

    @Override
    public void setInitialSpawnLocation()
    {
        // Noop
    }

    @Override
    public void setRainStrength(final float strength)
    {
        // Noop
    }

    @Override
    public void setSpawnPoint(final BlockPos pos)
    {
        // Noop
    }

    @Override
    public void setThunderStrength(final float strength)
    {
        // Noop
    }

    @Override
    public void setTileEntity(final BlockPos pos, final TileEntity tileEntityIn)
    {
        // TODO Auto-generated method stub
        super.setTileEntity(pos, tileEntityIn);
    }

    @Override
    public void setTimeLightningFlash(final int timeFlashIn)
    {
        // Noop
    }

    @Override
    public void tickBlockEntities()
    {
        // Noop
    }

    @Override
    public void updateComparatorOutputLevel(final BlockPos pos, final Block blockIn)
    {
        // TODO Auto-generated method stub
        super.updateComparatorOutputLevel(pos, blockIn);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        // Noop
        return null;
    }

    @Override
    protected void invalidateCaps()
    {
        // Noop
    }

    @Override
    protected void reviveCaps()
    {
        // Noop
    }

    @Override
    public boolean checkNoEntityCollision(final Entity entityIn, final VoxelShape shape)
    {
        // TODO Auto-generated method stub
        return super.checkNoEntityCollision(entityIn, shape);
    }

    @Override
    public boolean chunkExists(final int chunkX, final int chunkZ)
    {
        // TODO Auto-generated method stub
        return super.chunkExists(chunkX, chunkZ);
    }

    @Override
    public float getCelestialAngle(final float partialTicks)
    {
        // Noop
        return 0.0f;
    }

    @Override
    public float getCurrentMoonPhaseFactor()
    {
        // Noop
        return 0.0f;
    }

    @Override
    public Difficulty getDifficulty()
    {
        // Noop
        return Difficulty.PEACEFUL;
    }

    @Override
    public Stream<VoxelShape> getEmptyCollisionShapes(final Entity entityIn, final AxisAlignedBB aabb, final Set<Entity> entitiesToIgnore)
    {
        // TODO Auto-generated method stub
        return super.getEmptyCollisionShapes(entityIn, aabb, entitiesToIgnore);
    }

    @Override
    public BlockPos getHeight(final Type heightmapType, final BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.getHeight(heightmapType, pos);
    }

    @Override
    public int getMoonPhase()
    {
        // Noop
        return 0;
    }

    @Override
    public void playEvent(final int type, final BlockPos pos, final int data)
    {
        // Noop
    }

    @Override
    public <T extends Entity> List<T> func_225317_b(final Class<? extends T> p_225317_1_, final AxisAlignedBB p_225317_2_)
    {
        // TODO Auto-generated method stub
        return super.func_225317_b(p_225317_1_, p_225317_2_);
    }

    @Override
    public <T extends LivingEntity> T func_225318_b(final Class<? extends T> p_225318_1_,
        final EntityPredicate p_225318_2_,
        final LivingEntity p_225318_3_,
        final double p_225318_4_,
        final double p_225318_6_,
        final double p_225318_8_,
        final AxisAlignedBB p_225318_10_)
    {
        // TODO Auto-generated method stub
        return super.func_225318_b(p_225318_1_, p_225318_2_, p_225318_3_, p_225318_4_, p_225318_6_, p_225318_8_, p_225318_10_);
    }

    @Override
    public <T extends LivingEntity> T getClosestEntity(final List<? extends T> p_217361_1_,
        final EntityPredicate p_217361_2_,
        final LivingEntity p_217361_3_,
        final double p_217361_4_,
        final double p_217361_6_,
        final double p_217361_8_)
    {
        // TODO Auto-generated method stub
        return super.getClosestEntity(p_217361_1_, p_217361_2_, p_217361_3_, p_217361_4_, p_217361_6_, p_217361_8_);
    }

    @Override
    public <T extends LivingEntity> T getClosestEntityWithinAABB(final Class<? extends T> p_217360_1_,
        final EntityPredicate p_217360_2_,
        final LivingEntity p_217360_3_,
        final double p_217360_4_,
        final double p_217360_6_,
        final double p_217360_8_,
        final AxisAlignedBB p_217360_10_)
    {
        // TODO Auto-generated method stub
        return super.getClosestEntityWithinAABB(p_217360_1_, p_217360_2_, p_217360_3_, p_217360_4_, p_217360_6_, p_217360_8_, p_217360_10_);
    }

    @Override
    public PlayerEntity getClosestPlayer(final Entity p_217362_1_, final double p_217362_2_)
    {
        // Noop
        return null;
    }

    @Override
    public PlayerEntity getClosestPlayer(final EntityPredicate p_217370_1_, final LivingEntity p_217370_2_)
    {
        // Noop
        return null;
    }

    @Override
    public PlayerEntity getClosestPlayer(final double p_217365_1_, final double p_217365_3_, final double p_217365_5_)
    {
        // Noop
        return null;
    }

    @Override
    public PlayerEntity getClosestPlayer(final EntityPredicate p_217359_1_,
        final double p_217359_2_,
        final double p_217359_4_,
        final double p_217359_6_)
    {
        // Noop
        return null;
    }

    @Override
    public PlayerEntity getClosestPlayer(final double p_190525_1_,
        final double p_190525_3_,
        final double p_190525_5_,
        final double p_190525_7_,
        final Predicate<Entity> p_190525_9_)
    {
        // Noop
        return null;
    }

    @Override
    public PlayerEntity getClosestPlayer(final double p_217366_1_,
        final double p_217366_3_,
        final double p_217366_5_,
        final double p_217366_7_,
        final boolean p_217366_9_)
    {
        // Noop
        return null;
    }

    @Override
    public PlayerEntity getClosestPlayer(final EntityPredicate p_217372_1_,
        final LivingEntity p_217372_2_,
        final double p_217372_3_,
        final double p_217372_5_,
        final double p_217372_7_)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(final Class<? extends T> p_217357_1_, final AxisAlignedBB p_217357_2_)
    {
        // TODO Auto-generated method stub
        return super.getEntitiesWithinAABB(p_217357_1_, p_217357_2_);
    }

    @Override
    public List<Entity> getEntitiesWithinAABBExcludingEntity(final Entity p_72839_1_, final AxisAlignedBB p_72839_2_)
    {
        // TODO Auto-generated method stub
        return super.getEntitiesWithinAABBExcludingEntity(p_72839_1_, p_72839_2_);
    }

    @Override
    public PlayerEntity getPlayerByUuid(final UUID p_217371_1_)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends LivingEntity> List<T> getTargettableEntitiesWithinAABB(final Class<? extends T> p_217374_1_,
        final EntityPredicate p_217374_2_,
        final LivingEntity p_217374_3_,
        final AxisAlignedBB p_217374_4_)
    {
        // TODO Auto-generated method stub
        return super.getTargettableEntitiesWithinAABB(p_217374_1_, p_217374_2_, p_217374_3_, p_217374_4_);
    }

    @Override
    public List<PlayerEntity> getTargettablePlayersWithinAABB(final EntityPredicate p_217373_1_,
        final LivingEntity p_217373_2_,
        final AxisAlignedBB p_217373_3_)
    {
        // TODO Auto-generated method stub
        return super.getTargettablePlayersWithinAABB(p_217373_1_, p_217373_2_, p_217373_3_);
    }

    @Override
    public boolean isPlayerWithin(final double p_217358_1_, final double p_217358_3_, final double p_217358_5_, final double p_217358_7_)
    {
        // Noop
        return false;
    }

    @Override
    public boolean canBlockSeeSky(final BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.canBlockSeeSky(pos);
    }

    @Override
    public boolean containsAnyLiquid(final AxisAlignedBB bb)
    {
        // TODO Auto-generated method stub
        return super.containsAnyLiquid(bb);
    }

    @Override
    public Biome getBiome(final BlockPos p_226691_1_)
    {
        // Noop
        return null;
    }

    @Override
    public float getBrightness(final BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.getBrightness(pos);
    }

    @Override
    public IChunk getChunk(final BlockPos pos)
    {
        return super.getChunk(pos);
    }

    @Override
    public IChunk getChunk(final int chunkX, final int chunkZ, final ChunkStatus requiredStatus)
    {
        return super.getChunk(chunkX, chunkZ, requiredStatus);
    }

    @Override
    public int getLight(final BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.getLight(pos);
    }

    @Override
    public int getNeighborAwareLightSubtracted(final BlockPos pos, final int amount)
    {
        // TODO Auto-generated method stub
        return super.getNeighborAwareLightSubtracted(pos, amount);
    }

    @Override
    public Biome getNoiseBiome(final int x, final int y, final int z)
    {
        // Noop
        return null;
    }

    @Override
    public int getStrongPower(final BlockPos pos, final Direction direction)
    {
        return super.getStrongPower(pos, direction);
    }

    @Override
    public boolean hasWater(final BlockPos pos)
    {
        return super.hasWater(pos);
    }

    @Override
    public boolean isAirBlock(final BlockPos pos)
    {
        return super.isAirBlock(pos);
    }

    @Override
    public boolean isAreaLoaded(final BlockPos center, final int range)
    {
        // Noop
        return true;
    }

    @Override
    public boolean isAreaLoaded(final BlockPos from, final BlockPos to)
    {
        // Noop
        return true;
    }

    @Override
    public boolean isAreaLoaded(final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ)
    {
        // Noop
        return true;
    }

    @Override
    public boolean isBlockLoaded(final BlockPos pos)
    {
        // Noop
        return true;
    }

    @Override
    public int getHeight()
    {
        // TODO Auto-generated method stub
        return super.getHeight();
    }

    @Override
    public int getLightValue(final BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.getLightValue(pos);
    }

    @Override
    public int getMaxLightLevel()
    {
        // TODO Auto-generated method stub
        return super.getMaxLightLevel();
    }

    @Override
    public BlockRayTraceResult rayTraceBlocks(final RayTraceContext context)
    {
        // Noop
        return null;
    }

    @Override
    public BlockRayTraceResult rayTraceBlocks(final Vec3d p_217296_1_,
        final Vec3d p_217296_2_,
        final BlockPos p_217296_3_,
        final VoxelShape p_217296_4_,
        final BlockState p_217296_5_)
    {
        // Noop
        return null;
    }

    @Override
    public boolean hasNoCollisions(final Entity p_226662_1_, final AxisAlignedBB p_226662_2_, final Set<Entity> p_226662_3_)
    {
        // TODO Auto-generated method stub
        return super.hasNoCollisions(p_226662_1_, p_226662_2_, p_226662_3_);
    }

    @Override
    public boolean func_226663_a_(final BlockState p_226663_1_, final BlockPos p_226663_2_, final ISelectionContext p_226663_3_)
    {
        // TODO Auto-generated method stub
        return super.func_226663_a_(p_226663_1_, p_226663_2_, p_226663_3_);
    }

    @Override
    public boolean hasNoCollisions(final AxisAlignedBB p_226664_1_)
    {
        // TODO Auto-generated method stub
        return super.hasNoCollisions(p_226664_1_);
    }

    @Override
    public boolean hasNoCollisions(final Entity p_226665_1_, final AxisAlignedBB p_226665_2_)
    {
        // TODO Auto-generated method stub
        return super.hasNoCollisions(p_226665_1_, p_226665_2_);
    }

    @Override
    public Stream<VoxelShape> getCollisionShapes(final Entity p_226666_1_, final AxisAlignedBB p_226666_2_)
    {
        // TODO Auto-generated method stub
        return super.getCollisionShapes(p_226666_1_, p_226666_2_);
    }

    @Override
    public Stream<VoxelShape> getCollisionShapes(final Entity p_226667_1_, final AxisAlignedBB p_226667_2_, final Set<Entity> p_226667_3_)
    {
        // TODO Auto-generated method stub
        return super.getCollisionShapes(p_226667_1_, p_226667_2_, p_226667_3_);
    }

    @Override
    public boolean checkNoEntityCollision(final Entity p_226668_1_)
    {
        // TODO Auto-generated method stub
        return super.checkNoEntityCollision(p_226668_1_);
    }

    @Override
    public boolean hasNoCollisions(final Entity p_226669_1_)
    {
        // TODO Auto-generated method stub
        return super.hasNoCollisions(p_226669_1_);
    }

    @Override
    public int getMaxHeight()
    {
        // TODO Auto-generated method stub
        return super.getMaxHeight();
    }

    @Override
    public boolean addEntity(final Entity p_217376_1_)
    {
        // TODO Auto-generated method stub
        return super.addEntity(p_217376_1_);
    }

    @Override
    public boolean destroyBlock(final BlockPos p_175655_1_, final boolean p_175655_2_)
    {
        // TODO Auto-generated method stub
        return super.destroyBlock(p_175655_1_, p_175655_2_);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap)
    {
        // Noop
        return null;
    }
}
