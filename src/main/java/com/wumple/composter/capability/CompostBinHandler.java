package com.wumple.composter.capability;

import com.wumple.composter.config.ConfigHandler;
import com.wumple.util.adapter.EntityThing;
import com.wumple.util.adapter.TileEntityThing;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class CompostBinHandler
{
    /**
     * Attach the capability to relevant items.
     *
     * @param event
     *            The event
     */
    @SubscribeEvent
    public static void attachCapabilitiesTileEntity(AttachCapabilitiesEvent<TileEntity> event)
    {
        TileEntity entity = event.getObject();

        if (ConfigHandler.composters.doesIt(entity))
        {
            CompostBinCapProvider provider = CompostBinCapProvider.createProvider(new TileEntityThing(entity));
            event.addCapability(CompostBinCap.ID, provider);
        }
    }

    @SubscribeEvent
    public static void attachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event)
    {
        Entity entity = event.getObject();

        if (ConfigHandler.composters.doesIt(entity))
        {
            CompostBinCapProvider provider = CompostBinCapProvider.createProvider(new EntityThing(entity));
            event.addCapability(CompostBinCap.ID, provider);
        }
    }
    
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getEntityPlayer().isSpectator()) { return; }
            
        World worldIn = event.getWorld();
        BlockPos pos = event.getPos();
        
        ICompostBinCap cap = ICompostBinCap.getCap(worldIn, pos);

        if (cap != null)
        {
            cap.onRightBlockClicked(event);
        }
    }
    
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        World worldIn = event.getWorld();
        
        if (worldIn.isRemote)
        {
            return;
        }
        
        BlockPos pos = event.getPos();
        ICompostBinCap cap = ICompostBinCap.getCap(worldIn, pos);
        
        if (cap != null)
        {
            cap.onBlockBreak(worldIn, pos);
        }
    }

}
