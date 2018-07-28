package com.wumple.composter.bin;

import com.wumple.util.tileentity.CustomNamedTileEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityCompostBin extends CustomNamedTileEntity
{
    /**
     * This controls whether the tile entity gets replaced whenever the block state is changed. Normally only want this when block actually is replaced.
     */
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return (oldState.getBlock() != newState.getBlock());
    }

    /// CustomNamedTileEntity

    @Override
    public String getRealName()
    {
        return "container.composter.compost_bin";
    }
}
