package com.wumple.composter.bin;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
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
    @Override
    public boolean hasCustomName()
    {
        return this.customName != null && !this.customName.isEmpty();
    }
    
    @Nullable
    public ITextComponent getDisplayName()
    {
        return this.hasCustomName() ? new
                TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }
    
    public String getRealName()
    {
        return "container.composter.compost_bin";
    }
    
    public void setName(String name)
    {
        this.customName = name;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        
        if (compound.hasKey("CustomName", 8))
        {
            this.customName = compound.getString("CustomName");
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        
        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.customName);
        }
        
        return compound;
    }
}
