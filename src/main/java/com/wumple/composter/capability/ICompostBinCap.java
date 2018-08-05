package com.wumple.composter.capability;

import java.util.List;

import javax.annotation.Nullable;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.tickingthing.ITickingThingCap;
import com.wumple.util.container.capabilitylistener.CapabilityUtils;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface ICompostBinCap extends IItemHandlerModifiable, IInventory, ITickingThingCap<IThing>, INBTSerializable<NBTBase>
{
    boolean isActive();
    int getDecomposeTimeRemainingScaled(int size);
    void updateBlockState();
    
    void onBlockBreak(World worldIn, BlockPos pos);

    void onRightBlockClicked(PlayerInteractEvent.RightClickBlock event);
    
    void detectAndSendChanges(Container containerIn, List<IContainerListener> listeners);
    void updateProgressBar(int id, int value);
    
    static ICompostBinCap getCap(@Nullable ICapabilityProvider provider)
    {
        return CapabilityUtils.fetchCapability(provider, CompostBinCap.CAPABILITY, CompostBinCap.DEFAULT_FACING);
    }

    static ICompostBinCap getCap(World worldIn, @Nullable BlockPos pos)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return ICompostBinCap.getCap(tileentity);
    } 
}