package com.wumple.composter.capability;

import java.util.ArrayList;
import java.util.List;

import com.wumple.composter.config.ConfigHandler;
import com.wumple.composter.config.ModConfig;
import com.wumple.util.adapter.EntityThing;
import com.wumple.util.adapter.TileEntityThing;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class CompostBinHandler
{
    public static CompostBinHandler INSTANCE = new CompostBinHandler();

    public static CompostBinHandler getInstance()
    {
        return INSTANCE;
    }

    public CompostBinHandler()
    {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    protected boolean isDebugging()
    {
        return ModConfig.zdebugging.debug;
    }
    
    protected ICompostBinCap getCap(ICapabilityProvider provider)
    {
        return ICompostBinCap.getCap(provider);
    }
    
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
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
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
    public void onBlockBreak(BlockEvent.BreakEvent event)
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

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onDrawOverlay(final RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.gameSettings.showDebugInfo == true)
        {
            if (isDebugging())
            {
                addTileEntityDebug(e);
            }
        }
    }
    
    /*
     * Add TileEntity debug text to debug screen if looking at Block with a TileEntity
     */
    @SideOnly(Side.CLIENT)
    public void addTileEntityDebug(RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        
        // tile entity
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && mc.objectMouseOver.getBlockPos() != null)
        {
            BlockPos blockpos = (mc.objectMouseOver == null) ? null : mc.objectMouseOver.getBlockPos();
            TileEntity te = (blockpos == null) ? null : mc.world.getTileEntity(blockpos);
            ICompostBinCap cap = getCap(te);
            if (cap != null)
            {
                List<String> tips = new ArrayList<String>();
                cap.doTooltip(null, mc.player, true, tips);
                
                for (String tip : tips)
                {
                    e.getRight().add(tip);
                }
            }
        }
    }
}
