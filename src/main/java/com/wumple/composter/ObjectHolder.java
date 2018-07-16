package com.wumple.composter;

import com.wumple.composter.bin.BlockCompostBin;
import com.wumple.composter.bin.ComposterGuiHandler;
import com.wumple.composter.bin.TileEntityCompostBin;
import com.wumple.util.RegistrationHelpers;
import com.wumple.util.TypeIdentifier;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@GameRegistry.ObjectHolder("composter")
public class ObjectHolder {
    
    @GameRegistry.ObjectHolder("composter:compost_bin")
    public static /*final*/ Block compost_bin = null;
    
	//@GameRegistry.ObjectHolder("composter:compost_bin_item")
    public static /*final*/ Item compost_bin_item = null;

	//@GameRegistry.ObjectHolder("composter:compost")
    public static /*final*/ Item compost = null;

    // ----------------------------------------------------------------------
    // Ore Dictionary

    protected final static String[] composters = {"composter"};
    protected final static String[] fertilizers = {"fertilizer"};
    protected final static String BONEMEAL = "minecraft:dye@15";
    
    // ----------------------------------------------------------------------
    // Events

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class RegistrationHandler
    {

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event)
        {
            final IForgeRegistry<Item> registry = event.getRegistry();

            compost = RegistrationHelpers.regHelperOre(registry, new ItemCompost(), fertilizers);
            compost_bin_item = RegistrationHelpers.registerItemBlockOre(registry, compost_bin, composters);
            
            RegistrationHelpers.registerOreNames(TypeIdentifier.build(BONEMEAL).create(1), fertilizers);
            
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
        	RegistrationHelpers.registerRender(compost_bin, compost_bin_item);
        }
        
        public static void registerGuiHandlers()
        {
        	 NetworkRegistry.INSTANCE.registerGuiHandler(Composter.instance, new ComposterGuiHandler());
        }
	}
}
