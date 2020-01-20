package com.wumple.cannycomposter;

import com.wumple.cannycomposter.things.CannyComposterBlock;
import com.wumple.cannycomposter.things.CompostItem;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Reference.MOD_ID)
public class MyObjectHolder
{
	// ----------------------------------------------------------------------
	// Blocks, Items, etc.

	//@ObjectHolder("minecraft:composter")
	public static /*final*/ Block blockComposter = null;

	//@ObjectHolder("minecraft:composter")
	public static /*final*/ BlockItem itemComposter = null;
	
	//@ObjectHolder("cannycomposter:compost")
	public static /*final*/ CompostItem itemCompost = null;

	// ----------------------------------------------------------------------
	// Events

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistrationHandler
	{
		@SubscribeEvent
		public static void registerBlocks(RegistryEvent.Register<Block> event)
		{
			final IForgeRegistry<Block> registry = event.getRegistry();

			blockComposter = new CannyComposterBlock();
			blockComposter.setRegistryName("minecraft:composter");
			registry.register(blockComposter);
		}

		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event)
		{
			final IForgeRegistry<Item> registry = event.getRegistry();

			Item.Properties properties = new Item.Properties();

			itemComposter = new BlockItem(blockComposter, properties);
			itemComposter.setRegistryName("minecraft:composter");
			registry.register(itemComposter);
			
			itemCompost = new CompostItem(properties);
			itemCompost.setRegistryName("cannycomposter:compost");
			registry.register(itemCompost);
		}
	}
}