package com.wumple.composter.bin;

import com.wumple.composter.Composter;
import com.wumple.util.adapter.EntityThing;
import com.wumple.util.adapter.TileEntityThing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
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
    
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        World worldIn = event.getWorld();
        BlockPos pos = event.getPos();
        EntityPlayer playerIn = event.getEntityPlayer();
        TileEntity tileentity = worldIn.getTileEntity(pos);
        ICompostBinCap iinventory = ICompostBinCap.getCap(tileentity);

        if (iinventory != null)
        {
            if (worldIn.getBlockState(pos.up()).doesSideBlockChestOpening(worldIn, pos.up(), EnumFacing.DOWN))
            {
                event.setCancellationResult(EnumActionResult.FAIL);
                event.setCanceled(true);
            }
            else if (worldIn.isRemote)
            {
                event.setCanceled(true);
            }
            else
            {
                playerIn.openGui(Composter.instance, ComposterGuiHandler.compostBinGuiID, worldIn, pos.getX(), pos.getY(), pos.getZ());
                event.setCanceled(true);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
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
        TileEntity tileentity = worldIn.getTileEntity(pos);
        ICompostBinCap iinventory = ICompostBinCap.getCap(tileentity);
        
        if (iinventory != null)
        {
            InventoryHelper.dropInventoryItems(worldIn, pos, iinventory);
            worldIn.updateComparatorOutputLevel(pos, worldIn.getBlockState(pos).getBlock());
        }
    }

}
