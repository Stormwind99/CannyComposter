package com.wumple.composter.capability;

import javax.annotation.Nullable;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.thing.ThingCapProvider;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class CompostBinCapProvider extends ThingCapProvider<IThing, ICompostBinCap>
{
    public CompostBinCapProvider(Capability<ICompostBinCap> capability, @Nullable EnumFacing facing, IThing ownerIn)
    {
        super(capability, facing, (capability != null) ? capability.getDefaultInstance() : null, ownerIn);
    }

    public CompostBinCapProvider(Capability<ICompostBinCap> capability, @Nullable EnumFacing facing, ICompostBinCap instance,
            IThing ownerIn)
    {
        super(capability, facing, instance, ownerIn);
    }

    public static CompostBinCapProvider createProvider(IThing ownerIn)
    {
        return new CompostBinCapProvider(CompostBinCap.CAPABILITY, CompostBinCap.DEFAULT_FACING, ownerIn);
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return ( (capability != null) && (
                (capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) ||
                super.hasCapability(capability, facing) )
                ) ;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return (T)getInstance().handler();
        }
        if (hasCapability(capability, facing))
        {
            return (T)getCapability().cast(getInstance());
        }

        return null;
    }
}
