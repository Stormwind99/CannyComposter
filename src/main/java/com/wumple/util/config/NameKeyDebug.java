package com.wumple.util.config;

import java.util.ArrayList;

import com.wumple.cannycomposter.ConfigManager;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class NameKeyDebug
{
	public static boolean isDebugging()
	{
		return ConfigManager.Debugging.debug.get();
	}
	
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onItemTooltip(ItemTooltipEvent event)
    {
        if (isDebugging())
        {
            ItemStack itemStack = event.getItemStack();
            ArrayList<String> nameKeys = NameKeys.getNameKeys(itemStack);

            if (nameKeys != null)
            {
                for (String nameKey : nameKeys)
                {
                    event.getToolTip().add(new TranslationTextComponent("misc.wumpleutil.tooltip.advanced.namekey.itemstack", nameKey));
                }
            }
        }
    }

    /*
     * Draw debug screen extras
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    @OnlyIn(Dist.CLIENT)
    static public void onDrawOverlay(final RenderGameOverlayEvent.Text e)
    {
        if (isDebugging())
        {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameSettings.showDebugInfo == true)
            {
                e.getRight().add("");
                e.getRight().add(I18n.format("misc.wumpleutil.debug.namekey.title"));
                addEntityDebug(e);
                addTileEntityDebug(e);
                addBlockDebug(e);
            }
        }
    }
    
    /*
     * Add Entity debug text to debug screen if looking at Entity
     */
    
    @OnlyIn(Dist.CLIENT)
    public static void addEntityDebug(RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY)
        {
        	final EntityRayTraceResult rayTraceResult = (EntityRayTraceResult) mc.objectMouseOver;
        	Entity entity = rayTraceResult.getEntity();
        	
            ArrayList<String> nameKeys = NameKeys.getNameKeys(entity);

            if (nameKeys != null)
            {
                for (String nameKey : nameKeys)
                {
                    e.getRight().add(I18n.format("misc.wumpleutil.debug.namekey.entity", nameKey));
                }
            }
        }
    }
    
    /*
     * Add TileEntity debug text to debug screen if looking at Block with a TileEntity
     */
    @OnlyIn(Dist.CLIENT)
    public static void addTileEntityDebug(RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK)
        {
            final BlockRayTraceResult rayTraceResult = (BlockRayTraceResult) mc.objectMouseOver;
            BlockPos blockpos = rayTraceResult.getPos();

            TileEntity te = (blockpos == null) ? null : mc.world.getTileEntity(blockpos);
           
            ArrayList<String> nameKeys = NameKeys.getNameKeys(te);

            if (nameKeys != null)
            {
                for (String nameKey : nameKeys)
                {
                    e.getRight().add(I18n.format("misc.wumpleutil.debug.namekey.tileentity", nameKey));
                }
            }
        }
    }
    
    /*
     * Add TileEntity debug text to debug screen if looking at Block
     */
    @OnlyIn(Dist.CLIENT)
    public static void addBlockDebug(RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK)
        {
            final BlockRayTraceResult rayTraceResult = (BlockRayTraceResult) mc.objectMouseOver;
            BlockPos blockpos = rayTraceResult.getPos();

            Block block = (blockpos == null) ? null : mc.world.getBlockState(blockpos).getBlock();
           
            ArrayList<String> nameKeys = NameKeys.getNameKeys(block);

            if (nameKeys != null)
            {
                for (String nameKey : nameKeys)
                {
                    e.getRight().add(I18n.format("misc.wumpleutil.debug.namekey.block", nameKey));
                }
            }
        }
    }

}