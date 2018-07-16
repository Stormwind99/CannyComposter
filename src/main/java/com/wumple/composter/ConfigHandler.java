package com.wumple.composter;

import com.wumple.composter.bin.TileEntityCompostBin;
import com.wumple.composter.config.ModConfig;
import com.wumple.util.config.MatchingConfig;

import net.minecraft.init.Items;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

public class ConfigHandler
{
	public static final MatchingConfig<Integer> compostAmounts = new MatchingConfig<Integer>(ModConfig.items.amount, TileEntityCompostBin.NO_DECOMPOSE_TIME);
	
	public static void init()
	{
		compostAmounts.addDefaultProperty("minecraft:food", 125);
		compostAmounts.addDefaultProperty(Items.ROTTEN_FLESH, "minecraft:rotten_flesh", 100);
		compostAmounts.addDefaultProperty("composter:compost_bin", 125);
		compostAmounts.addDefaultProperty("minecraft:wooden_axe", 125);
		compostAmounts.addDefaultProperty("minecraft:wooden_pickaxe", 125);
		compostAmounts.addDefaultProperty("minecraft:wooden_sword", 125);
		compostAmounts.addDefaultProperty("minecraft:wooden_shovel", 125);
		compostAmounts.addDefaultProperty("minecraft:wooden_spade", 125);
		compostAmounts.addDefaultProperty("minecraft:wooden_hoe", 125);
		compostAmounts.addDefaultProperty("minecraft:wooden_button", 125);
		compostAmounts.addDefaultProperty("minecraft:wooden_pressure_plate", 125);
		compostAmounts.addDefaultProperty("minecraft:wooden_door", 125);
		compostAmounts.addDefaultProperty("minecraft:wool", 75);
		
		// Food Funk mod
		compostAmounts.addDefaultProperty("foodfunk:spoiled_milk", 100);
		compostAmounts.addDefaultProperty("foodfunk:rotten_food", 100);
		compostAmounts.addDefaultProperty("foodfunk:rotten_item", 100);
		
		// ore dict
        compostAmounts.addDefaultProperty("logWood", 125);
        compostAmounts.addDefaultProperty("plankWood", 125);
        compostAmounts.addDefaultProperty("slabWood", 125);
        compostAmounts.addDefaultProperty("stairWood", 125);
        compostAmounts.addDefaultProperty("fenceWood", 125);
        compostAmounts.addDefaultProperty("fenceGateWood", 125);
        compostAmounts.addDefaultProperty("stickWood", 125);
        compostAmounts.addDefaultProperty("treeSapling", 125);
        compostAmounts.addDefaultProperty("treeLeaves", 125);
        compostAmounts.addDefaultProperty("vine", 125);
        
        // crops
        compostAmounts.addDefaultProperty("cropWheat", 125);
        compostAmounts.addDefaultProperty("cropPotato", 125);
        compostAmounts.addDefaultProperty("cropCarrot", 125);
        compostAmounts.addDefaultProperty("cropNetherWart", 125);
        compostAmounts.addDefaultProperty("sugarcane", 125);
        compostAmounts.addDefaultProperty("blockCactus", 125);

        // misc materials
        compostAmounts.addDefaultProperty("dye", 125);
        compostAmounts.addDefaultProperty("paper", 125);

        // mob drops
        compostAmounts.addDefaultProperty("slimeball", 125);
        
        compostAmounts.addDefaultProperty("bone", 125);
        compostAmounts.addDefaultProperty("gunpowder", 125);
        compostAmounts.addDefaultProperty("string", 125);
        compostAmounts.addDefaultProperty("leather", 125);
        compostAmounts.addDefaultProperty("feather", 125);
        compostAmounts.addDefaultProperty("egg", 125);
        
        compostAmounts.addDefaultProperty("grass", 125);
             
        compostAmounts.addDefaultProperty("torch", 125);
        compostAmounts.addDefaultProperty("workbench", 125);
        compostAmounts.addDefaultProperty("blockSlime", 125);
        compostAmounts.addDefaultProperty("chestWood", 125);
        
        
        
        ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
	}
	
	
		/*

		// More ideas 
		 
        if (item instanceof ItemEgg)
        {
        	return 100;
        }
        
        if (item instanceof ItemBucketMilk)
        {
        	return 200;
        }
        
        if (item instanceof ItemSeeds)
        {
        	return 125;
        }
        
        if (item instanceof ItemPotion)
        {
        	return 200;
        }
        
        if (item instanceof ItemTool)
        {
        	ItemTool tool = (ItemTool)item;
        	if (tool.getToolMaterialName().equalsIgnoreCase("WOOD"))
        	{
        		// MAYBE vary amount on item damage, and damage item as it composts
        		return 300;
        	}
        }
     
        // MAYBE more items
        // ItemArmor material leather
        // ItemArrow
        // ItemBanner
        // ItemBed
        // ItemBoat
        // ItemBook
        // ItemCarrotOnAStick
        // ItemCloth
        // ItemCoal
        // ItemDoor if wood (how?)
        // ItemDye
        // MAPBASE: ItemEmptyMap
        // ItemFishingRod
        // ItemHoe if wood
        // ItemKnowledgeBook
        // ItemLead
        // ItemLilyPad
        // ItemMapBase
        // MAPBASE: ItemMap
        // ItemSaddle
        // ItemSeedFood (?)
        // ItemSign
        // ItemSkull
        // ItemSlab if wood
        // ItemSnow
        // ItemSnowball
        // ItemSoup
        // ItemSword if wood
        // TOOL: ItemAxe if wood
        // TOOL: ItemSpade if wood
        // TOOL: ItemPickaxe if wood
        // ItemWritableBook
        // ItemWrittenBook
        // ItemEnchantedBook
        // ItemBow
        // ItemHoe if wood

        // STICK
        // Stairs if wood
        // Plate if wood
        // Gear if wood
        
        public static final Item APPLE;
        public static final ItemBow BOW;
        public static final Item ARROW;
        public static final Item COAL;
        public static final Item WOODEN_SWORD;
        public static final Item WOODEN_SHOVEL;
        public static final Item WOODEN_PICKAXE;
        public static final Item WOODEN_AXE;
        public static final Item STICK;
        public static final Item BOWL;
        public static final Item MUSHROOM_STEW;
        public static final Item STRING;
        public static final Item FEATHER;
        public static final Item GUNPOWDER;
        public static final Item WOODEN_HOE;
        public static final Item WHEAT_SEEDS;
        public static final Item WHEAT;
        public static final Item BREAD;
        public static final ItemArmor LEATHER_HELMET;
        public static final ItemArmor LEATHER_CHESTPLATE;
        public static final ItemArmor LEATHER_LEGGINGS;
        public static final ItemArmor LEATHER_BOOTS;
        public static final Item PORKCHOP;
        public static final Item COOKED_PORKCHOP;
        public static final Item PAINTING;
        public static final Item GOLDEN_APPLE;
        public static final Item SIGN;
        public static final Item OAK_DOOR;
        public static final Item SPRUCE_DOOR;
        public static final Item BIRCH_DOOR;
        public static final Item JUNGLE_DOOR;
        public static final Item ACACIA_DOOR;
        public static final Item DARK_OAK_DOOR;
        public static final Item WATER_BUCKET;
        public static final Item SADDLE;
        public static final Item REDSTONE;
        public static final Item SNOWBALL;
        public static final Item BOAT;
        public static final Item SPRUCE_BOAT;
        public static final Item BIRCH_BOAT;
        public static final Item JUNGLE_BOAT;
        public static final Item ACACIA_BOAT;
        public static final Item DARK_OAK_BOAT;
        public static final Item LEATHER;
        public static final Item MILK_BUCKET;
        public static final Item CLAY_BALL;
        public static final Item REEDS;
        public static final Item PAPER;
        public static final Item BOOK;
        public static final Item SLIME_BALL;
        public static final Item CHEST_MINECART;
        public static final Item EGG;
        public static final ItemFishingRod FISHING_ROD;
        public static final Item GLOWSTONE_DUST;
        public static final Item FISH;
        public static final Item COOKED_FISH;
        public static final Item DYE;
        public static final Item BONE;
        public static final Item SUGAR;
        public static final Item CAKE;
        public static final Item BED;
        public static final Item COOKIE;
        public static final ItemMap FILLED_MAP;
        public static final Item MELON;
        public static final Item PUMPKIN_SEEDS;
        public static final Item MELON_SEEDS;
        public static final Item BEEF;
        public static final Item COOKED_BEEF;
        public static final Item CHICKEN;
        public static final Item COOKED_CHICKEN;
        public static final Item MUTTON;
        public static final Item COOKED_MUTTON;
        public static final Item RABBIT;
        public static final Item COOKED_RABBIT;
        public static final Item RABBIT_STEW;
        public static final Item RABBIT_FOOT;
        public static final Item RABBIT_HIDE;
        public static final Item ROTTEN_FLESH;
        public static final Item GHAST_TEAR;
        public static final Item NETHER_WART;
        public static final ItemPotion POTIONITEM;
        public static final ItemPotion SPLASH_POTION;
        public static final ItemPotion LINGERING_POTION;
        public static final Item GLASS_BOTTLE;
        public static final Item DRAGON_BREATH;
        public static final Item SPIDER_EYE;
        public static final Item FERMENTED_SPIDER_EYE;
        public static final Item BLAZE_POWDER;
        public static final Item MAGMA_CREAM;
        public static final Item SPECKLED_MELON;
        public static final Item SPAWN_EGG;
        public static final Item WRITABLE_BOOK;
        public static final Item WRITTEN_BOOK;
        public static final Item ITEM_FRAME;
        public static final Item CARROT;
        public static final Item POTATO;
        public static final Item BAKED_POTATO;
        public static final Item POISONOUS_POTATO;
        public static final ItemEmptyMap MAP;
        public static final Item GOLDEN_CARROT;
        public static final Item SKULL;
        public static final Item CARROT_ON_A_STICK;
        public static final Item PUMPKIN_PIE;
        public static final Item FIREWORKS;
        public static final Item FIREWORK_CHARGE;
        public static final Item ENCHANTED_BOOK;
        public static final ItemArmorStand ARMOR_STAND;
        public static final Item LEAD;
        public static final Item BANNER;
        public static final Item SHIELD;
        public static final Item CHORUS_FRUIT;
        public static final Item CHORUS_FRUIT_POPPED;
        public static final Item BEETROOT_SEEDS;
        public static final Item BEETROOT;
        public static final Item BEETROOT_SOUP;
        public static final Item SHULKER_SHELL;
        public static final Item KNOWLEDGE_BOOK;
        
         */
	
}
