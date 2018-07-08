package com.wumple.composter;

import com.wumple.util.RegistrationHelpers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder("composter")
public class ObjectHolder {
    
    @GameRegistry.ObjectHolder("composter:compost_bin")
    public static /*final*/ Block compost_bin = null;
    
	//@GameRegistry.ObjectHolder("compost:compost_bin_item")
    public static /*final*/ Item compost_bin_item = null;

	//@GameRegistry.ObjectHolder("compost:compost")
    public static /*final*/ Item compost = null;

    // ----------------------------------------------------------------------
    // Events

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class RegistrationHandler
    {

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event)
        {
            final IForgeRegistry<Item> registry = event.getRegistry();

            compost = RegistrationHelpers.regHelper(registry, new ItemCompost());
            compost_bin_item = RegistrationHelpers.registerItemBlock(registry, compost_bin);
            
            registerTileEntities();
        }    
        
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event)
        {
            final IForgeRegistry<Block> registry = event.getRegistry();

            compost_bin = RegistrationHelpers.regHelper(registry, new BlockCompostBin());
        } 
        
        public static void registerTileEntities()
        {
        	RegistrationHelpers.registerTileEntity(TileEntityCompostBin.class, "composter:compost_bin");
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void registerRenders(ModelRegistryEvent event)
        {
        	RegistrationHelpers.registerRender(compost);

            // TODO ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCompostBin.class, new TileEntityCompostRenderer());
        }
	}
}
