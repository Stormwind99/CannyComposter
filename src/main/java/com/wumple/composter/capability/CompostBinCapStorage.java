package com.wumple.composter.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class CompostBinCapStorage implements IStorage<ICompostBinCap>
{
    @Override
    public NBTBase writeNBT(Capability<ICompostBinCap> capability, ICompostBinCap instance, EnumFacing side)
    {
        if (instance != null)
        {
            return instance.serializeNBT();
        }

        return null;
    }

    @Override
    public void readNBT(Capability<ICompostBinCap> capability, ICompostBinCap instance, EnumFacing side, NBTBase nbt)
    {
        if ((nbt != null) && (instance != null))
        {
            instance.deserializeNBT(nbt);
        }
    }
}