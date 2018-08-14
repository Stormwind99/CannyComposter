package com.wumple.composter.integration.waila;

import java.util.List;

import com.wumple.composter.Composter;
import com.wumple.composter.bin.TileEntityCompostBin;
import com.wumple.composter.capability.ICompostBinCap;
import com.wumple.composter.config.ModConfig;
import com.wumple.util.placeholder.TileEntityPlaceholder;
import com.wumple.util.tooltip.ITooltipProvider;
import com.wumple.util.tooltip.TooltipUtils;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WailaDataProvider implements IWailaDataProvider
{
    public static final WailaDataProvider INSTANCE = new WailaDataProvider();

    private WailaDataProvider()
    {
    }

    private static boolean loaded;

    public static void load(IWailaRegistrar registrar)
    {
        Composter.logger.info("Enabled support for Waila / Hwyla");
        if (!WailaCompatibility.getRegistered())
        {
            throw new RuntimeException("Please register this handler using the provided method.");
        }
        if (!loaded)
        {
            registrar.registerBodyProvider(INSTANCE, TileEntityPlaceholder.class);
            registrar.registerBodyProvider(INSTANCE, TileEntityCompostBin.class);
            loaded = true;
        }
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
    {
        return tag;
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        TileEntity te = accessor.getWorld().getTileEntity(accessor.getPosition());
        ITooltipProvider[] providers = { ICompostBinCap.getCap(te) };
        TooltipUtils.doTooltip(providers, null, accessor.getPlayer(), ModConfig.zdebugging.debug, currenttip);
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

}