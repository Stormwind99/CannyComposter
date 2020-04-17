package com.wumple.cannycomposter;

import java.util.HashMap;

import com.electronwill.nightconfig.core.Config;
import com.wumple.util.config.ConfigUtil;
import com.wumple.util.config.MatchingConfig;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class CompostablesConfig extends MatchingConfig<Integer>
{
	CompostablesConfig()
	{
		super(new HashMap<String, Integer>(), 0);
	}
	
	// ------------------------------------------------------------------------
	// Translation from Config to CompostablesConfig
	
	public void copyConfigSet(Config config)
	{
		ConfigUtil.handleConfigSet(config, this::registerCompostables, map);
	}
	
	/// Add compostable value only if a config entry doesn't already exist for it
	protected void registerCompostable(Config c, Integer i, Item item)
	{
		c.add(item.getRegistryName().toString(), i);
	}

	/// Add compostable value only if a config entry doesn't already exist for it
	protected void registerCompostable(Config c, Integer i, String name)
	{
		c.add(name, i);
	}
	
	/// Add default compostable values only if a config entry was not already added for them
	/// That behavior allows overriding these defaults via config file
	protected void registerCompostables(Config c)
	{
		int f0 = 30;
		int f1 = 50;
		int f2 = 65;
		int f3 = 85;
		int f4 = 100;

		// Minecraft default compostables
		registerCompostable(c, f0, Items.JUNGLE_LEAVES);
		registerCompostable(c, f0, Items.OAK_LEAVES);
		registerCompostable(c, f0, Items.SPRUCE_LEAVES);
		registerCompostable(c, f0, Items.DARK_OAK_LEAVES);
		registerCompostable(c, f0, Items.ACACIA_LEAVES);
		registerCompostable(c, f0, Items.BIRCH_LEAVES);
		registerCompostable(c, f0, Items.OAK_SAPLING);
		registerCompostable(c, f0, Items.SPRUCE_SAPLING);
		registerCompostable(c, f0, Items.BIRCH_SAPLING);
		registerCompostable(c, f0, Items.JUNGLE_SAPLING);
		registerCompostable(c, f0, Items.ACACIA_SAPLING);
		registerCompostable(c, f0, Items.DARK_OAK_SAPLING);
		registerCompostable(c, f0, Items.BEETROOT_SEEDS);
		registerCompostable(c, f0, Items.DRIED_KELP);
		registerCompostable(c, f0, Items.GRASS);
		registerCompostable(c, f0, Items.KELP);
		registerCompostable(c, f0, Items.MELON_SEEDS);
		registerCompostable(c, f0, Items.PUMPKIN_SEEDS);
		registerCompostable(c, f0, Items.SEAGRASS);
		registerCompostable(c, f0, Items.SWEET_BERRIES);
		registerCompostable(c, f0, Items.WHEAT_SEEDS);
		registerCompostable(c, f1, Items.DRIED_KELP_BLOCK);
		registerCompostable(c, f1, Items.TALL_GRASS);
		registerCompostable(c, f1, Items.CACTUS);
		registerCompostable(c, f1, Items.SUGAR_CANE);
		registerCompostable(c, f1, Items.VINE);
		registerCompostable(c, f1, Items.MELON_SLICE);
		registerCompostable(c, f2, Items.SEA_PICKLE);
		registerCompostable(c, f2, Items.LILY_PAD);
		registerCompostable(c, f2, Items.PUMPKIN);
		registerCompostable(c, f2, Items.CARVED_PUMPKIN);
		registerCompostable(c, f2, Items.MELON);
		registerCompostable(c, f2, Items.APPLE);
		registerCompostable(c, f2, Items.BEETROOT);
		registerCompostable(c, f2, Items.CARROT);
		registerCompostable(c, f2, Items.COCOA_BEANS);
		registerCompostable(c, f2, Items.POTATO);
		registerCompostable(c, f2, Items.WHEAT);
		registerCompostable(c, f2, Items.BROWN_MUSHROOM);
		registerCompostable(c, f2, Items.RED_MUSHROOM);
		registerCompostable(c, f2, Items.MUSHROOM_STEM);
		registerCompostable(c, f2, Items.DANDELION);
		registerCompostable(c, f2, Items.POPPY);
		registerCompostable(c, f2, Items.BLUE_ORCHID);
		registerCompostable(c, f2, Items.ALLIUM);
		registerCompostable(c, f2, Items.AZURE_BLUET);
		registerCompostable(c, f2, Items.RED_TULIP);
		registerCompostable(c, f2, Items.ORANGE_TULIP);
		registerCompostable(c, f2, Items.WHITE_TULIP);
		registerCompostable(c, f2, Items.PINK_TULIP);
		registerCompostable(c, f2, Items.OXEYE_DAISY);
		registerCompostable(c, f2, Items.CORNFLOWER);
		registerCompostable(c, f2, Items.LILY_OF_THE_VALLEY);
		registerCompostable(c, f2, Items.WITHER_ROSE);
		registerCompostable(c, f2, Items.FERN);
		registerCompostable(c, f2, Items.SUNFLOWER);
		registerCompostable(c, f2, Items.LILAC);
		registerCompostable(c, f2, Items.ROSE_BUSH);
		registerCompostable(c, f2, Items.PEONY);
		registerCompostable(c, f2, Items.LARGE_FERN);
		registerCompostable(c, f3, Items.HAY_BLOCK);
		registerCompostable(c, f3, Items.BROWN_MUSHROOM_BLOCK);
		registerCompostable(c, f3, Items.RED_MUSHROOM_BLOCK);
		registerCompostable(c, f3, Items.BREAD);
		registerCompostable(c, f3, Items.BAKED_POTATO);
		registerCompostable(c, f3, Items.COOKIE);
		registerCompostable(c, f4, Items.CAKE);
		registerCompostable(c, f4, Items.PUMPKIN_PIE);

		// additional vanilla MC compostables
		registerCompostable(c, f0, Items.ROTTEN_FLESH);
		registerCompostable(c, f0, Items.COOKED_PORKCHOP);
		registerCompostable(c, f0, Items.PORKCHOP);
		registerCompostable(c, f0, Items.GRASS_BLOCK);
		registerCompostable(c, f0, Items.SPONGE);
		registerCompostable(c, f0, Items.WET_SPONGE);
		registerCompostable(c, f0, Items.COBWEB);
		registerCompostable(c, f0, Items.DEAD_BUSH);
		registerCompostable(c, f0, Items.SLIME_BALL);
		registerCompostable(c, f0, Items.SLIME_BLOCK);
		registerCompostable(c, f0, Items.BONE);
		registerCompostable(c, f0, Items.BONE_BLOCK);
		registerCompostable(c, f0, Items.TURTLE_EGG);
		registerCompostable(c, f0, Items.STICK);
		registerCompostable(c, f0, Items.EGG);
		registerCompostable(c, f0, Items.PAPER);
		registerCompostable(c, f0, Items.COD);
		registerCompostable(c, f0, Items.SALMON);
		registerCompostable(c, f0, Items.TROPICAL_FISH);
		registerCompostable(c, f0, Items.PUFFERFISH);
		registerCompostable(c, f0, Items.COOKED_COD);
		registerCompostable(c, f0, Items.COOKED_SALMON);
		registerCompostable(c, f0, Items.INK_SAC);
		registerCompostable(c, f1, Items.SUGAR);
		registerCompostable(c, f0, Items.BEEF);
		registerCompostable(c, f0, Items.COOKED_BEEF);
		registerCompostable(c, f0, Items.CHICKEN);
		registerCompostable(c, f0, Items.COOKED_CHICKEN);
		registerCompostable(c, f0, Items.NETHER_WART);
		registerCompostable(c, f0, Items.SPIDER_EYE);
		registerCompostable(c, f0, Items.FERMENTED_SPIDER_EYE);
		registerCompostable(c, f0, Items.CARROT_ON_A_STICK);
		registerCompostable(c, f0, Items.RABBIT);
		registerCompostable(c, f0, Items.COOKED_RABBIT);
		registerCompostable(c, f0, Items.RABBIT_FOOT);
		registerCompostable(c, f0, Items.COOKED_COD);
		registerCompostable(c, f0, Items.COOKED_MUTTON);
		registerCompostable(c, f0, Items.COOKED_SALMON);
		registerCompostable(c, f0, Items.MUTTON);
		registerCompostable(c, f0, Items.RABBIT_STEW);
		registerCompostable(c, f0, Items.MUSHROOM_STEW);
		registerCompostable(c, f0, Items.SUSPICIOUS_STEW);
		registerCompostable(c, f0, Items.CHORUS_FLOWER);
		registerCompostable(c, f0, Items.CHORUS_FRUIT);
		registerCompostable(c, f0, Items.POPPED_CHORUS_FRUIT);
		registerCompostable(c, f0, Items.BEETROOT_SOUP);
		
		// MAYBE wooden items
		// MAYBE buckets like milk bucket but give empty bucket back
		
		// compostable tags - to make support easier for modders
		registerCompostable(c, f0, "#forge:compostables_level1");
		registerCompostable(c, f1, "#forge:compostables_level2");
		registerCompostable(c, f2, "#forge:compostables_level3");
		registerCompostable(c, f3, "#forge:compostables_level4");
		registerCompostable(c, f4, "#forge:compostables_level5");
		// tag "forge:compostables_all" just merges the above tags for reference
		
		// MAYBE more tags - but evaluation order could cause unintended values
		//    if tag hit before #forge:compostables_level1 for example
		// #forge:bones
		// #forge:crops
		// #forge:eggs
		// #forge:feathers
		// #forge:mushrooms
		// #forge:seeds
		// #forge:slimeballs
		// #forge:string
		
		// Food Funk
		registerCompostable(c, f0, "foodfunk:rotten_food");
		registerCompostable(c, f0, "rotted_item");
		registerCompostable(c, f0, "biodegradable_item");
		// TODO "spoiled_milk" but give back bucket
	}

}
