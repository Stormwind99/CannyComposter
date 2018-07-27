package com.wumple.composter.bin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;

public class TileEntityCompostBin extends TileEntity implements IWorldNameable
{
    private String customName;
    
    /**
    * This controls whether the tile entity gets replaced whenever the block state 
    * is changed. Normally only want this when block actually is replaced.
    */
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
    	return (oldState.getBlock() != newState.getBlock());
    }
        
    // ----------------------------------------------------------------------
    // IWorldNameable

    /**
     * Get the name of this object. For players this returns their username
     */
    @Override
    public String getName()
    {
        return this.hasCustomName() ? this.customName : getRealName();
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        return this.customName != null && !this.customName.isEmpty();
    }
    
    public String getRealName()
    {
        return "container.composter.compost_bin";
    }
}
