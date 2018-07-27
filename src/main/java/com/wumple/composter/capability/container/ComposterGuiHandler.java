package com.wumple.composter.capability.container;

import com.wumple.composter.capability.CompostBinCap;
import com.wumple.composter.capability.ICompostBinCap;
import com.wumple.util.capability.CapabilityUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ComposterGuiHandler implements IGuiHandler
{
    public static int compostBinGuiID = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        ICompostBinCap cap = CapabilityUtils.fetchCapability(tileEntity, CompostBinCap.CAPABILITY, CompostBinCap.DEFAULT_FACING);
        return (cap != null) ? new ContainerCompostBin(player.inventory, cap) : null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        ICompostBinCap cap = CapabilityUtils.fetchCapability(tileEntity, CompostBinCap.CAPABILITY, CompostBinCap.DEFAULT_FACING);
        return (cap != null) ? new GuiCompostBin(player.inventory, cap) : null;
    }

}