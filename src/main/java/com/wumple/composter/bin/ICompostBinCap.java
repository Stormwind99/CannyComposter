package com.wumple.composter.bin;

import java.util.List;

import javax.annotation.Nullable;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.CapabilityUtils;
import com.wumple.util.capability.tickingthing.ITickingThingCap;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface ICompostBinCap extends IItemHandlerModifiable, IInventory, ITickingThingCap<IThing>
{
    NBTBase writeNBT(NBTTagCompound compound);
    void readNBT(NBTTagCompound compound);
    
    boolean isActive();
    int getDecomposeTimeRemainingScaled(int size);
    void updateBlockState();
    
    void detectAndSendChanges(Container containerIn, List<IContainerListener> listeners);
    void updateProgressBar(int id, int value);
    
    static ICompostBinCap getCap(@Nullable ICapabilityProvider provider)
    {
        return CapabilityUtils.fetchCapability(provider, CompostBinCap.CAPABILITY, CompostBinCap.DEFAULT_FACING);
    }

}