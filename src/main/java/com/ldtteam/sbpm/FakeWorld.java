package com.ldtteam.sbpm;

import java.util.List;
import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;

public class FakeWorld implements ILightReader
{
    public static final FakeWorld INSTANCE = new FakeWorld();
    private Map<BlockPos, BlockInfo> blocks;

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
        return blocks.containsKey(pos) && blocks.get(pos).nbt != null ? TileEntity.create(blocks.get(pos).nbt) : null;
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
    public WorldLightManager getLightManager()
    {
        return null;
    }

    @Override
    public int getBlockColor(BlockPos blockPosIn, ColorResolver colorResolverIn)
    {
        // TODO Auto-generated method stub
        return 0;
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
}
