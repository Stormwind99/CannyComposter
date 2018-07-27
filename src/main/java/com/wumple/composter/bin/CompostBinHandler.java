package com.wumple.composter.bin;

import com.wumple.util.adapter.EntityThing;
import com.wumple.util.adapter.TileEntityThing;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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

        //if (ConfigHandler.preserving.doesIt(entity))
        if (entity instanceof TileEntityCompostBin)
        {
            CompostBinCapProvider provider = CompostBinCapProvider.createProvider(new TileEntityThing(entity));
            event.addCapability(CompostBinCap.ID, provider);
        }
    }

    @SubscribeEvent
    public static void attachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event)
    {
        Entity entity = event.getObject();

        if (entity == null) // if (entity instanceof TileEntityCompostBin2)
        {
            CompostBinCapProvider provider = CompostBinCapProvider.createProvider(new EntityThing(entity));
            event.addCapability(CompostBinCap.ID, provider);
        }
    }

}
