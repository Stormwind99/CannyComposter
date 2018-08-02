package com.wumple.composter;

import com.wumple.composter.bin.BlockCompostBin;
import com.wumple.composter.bin.TileEntityCompostBin;
import com.wumple.composter.capability.container.ComposterGuiHandler;
import com.wumple.composter.compost.ItemCompost;
import com.wumple.util.misc.RegistrationHelpers;
import com.wumple.util.misc.TypeIdentifier;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

@GameRegistry.ObjectHolder("composter")
public class ObjectHolder
{

    @GameRegistry.ObjectHolder("composter:compost_bin")
    public static /* final */ Block compost_bin = null;

    // @GameRegistry.ObjectHolder("composter:compost_bin_item")
    public static /* final */ Item compost_bin_item = null;

    // @GameRegistry.ObjectHolder("composter:compost")
    public static /* final */ Item compost = null;

    // ----------------------------------------------------------------------
    // Ore Dictionary

    public static class Ids
    {
        protected final static String[] composters = { "composter" };
        protected final static String[] fertilizers = { "fertilizer" };
        protected final static String BONEMEAL = "minecraft:dye@15";
        protected final static String BONEMEALOREDICT = "bonemeal";
        public static final String listAllSeed = "listAllseed";
        public static final String listAllFlower = "listAllflower";
        public static final String listAllMelon = "listAllmelon";
        public static final String listAllMushroom="listAllmushroom";
    }

    // ----------------------------------------------------------------------
    // Events

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class RegistrationHandler
    {

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event)
        {
            final IForgeRegistry<Item> registry = event.getRegistry();

            compost = RegistrationHelpers.regHelperOre(registry, new ItemCompost(), Ids.fertilizers);
            compost_bin_item = RegistrationHelpers.registerItemBlockOre(registry, compost_bin, Ids.composters);

            registerMoreOreNames();
            
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
            RegistrationHelpers.registerTileEntity(TileEntityCompostBin.class, BlockCompostBin.ID);
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
        
        public static void registerMoreOreNames()
        {
            RegistrationHelpers.registerOreNames(TypeIdentifier.build(Ids.BONEMEAL).create(1), Ids.fertilizers);
            OreDictionary.registerOre(Ids.BONEMEALOREDICT, TypeIdentifier.build(Ids.BONEMEAL).create(1));
            OreDictionary.registerOre(Ids.listAllSeed, Items.WHEAT_SEEDS);
            OreDictionary.registerOre(Ids.listAllSeed, Items.BEETROOT_SEEDS);
            OreDictionary.registerOre(Ids.listAllSeed, Items.PUMPKIN_SEEDS);
            OreDictionary.registerOre(Ids.listAllSeed, Items.MELON_SEEDS);
            OreDictionary.registerOre(Ids.listAllMelon, Blocks.MELON_BLOCK);
            OreDictionary.registerOre(Ids.listAllMelon, Blocks.PUMPKIN);
            OreDictionary.registerOre(Ids.listAllMelon, TypeIdentifier.build("minecraft:melon_block").create(1));
            OreDictionary.registerOre(Ids.listAllMelon, TypeIdentifier.build("minecraft:pumpkin").create(1));
            OreDictionary.registerOre(Ids.listAllFlower, new ItemStack(Blocks.RED_FLOWER, 1, OreDictionary.WILDCARD_VALUE));
            OreDictionary.registerOre(Ids.listAllFlower, new ItemStack(Blocks.YELLOW_FLOWER, 1, OreDictionary.WILDCARD_VALUE));
            OreDictionary.registerOre(Ids.listAllFlower, Blocks.CHORUS_FLOWER);
            OreDictionary.registerOre(Ids.listAllMushroom, Blocks.RED_MUSHROOM);
            OreDictionary.registerOre(Ids.listAllMushroom, Blocks.BROWN_MUSHROOM);
            OreDictionary.registerOre(Ids.listAllMushroom, Blocks.RED_MUSHROOM_BLOCK);
            OreDictionary.registerOre(Ids.listAllMushroom, Blocks.BROWN_MUSHROOM_BLOCK);
            
            /*
            // For MC 1.13
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:dandelion").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:poppy").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:blue_orchid").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:azure_bluet").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:red_tulip").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:orange_tulip").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:white_tulip").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:pink_tulip").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:oxeye_daisy").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:sunflower").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:lilac").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:rose_bush").create(1) );
            OreDictionary.registerOre(Ids.listAllFlower, TypeIdentifier.build("minecraft:peony").create(1) );
            */
        }
    }
}
