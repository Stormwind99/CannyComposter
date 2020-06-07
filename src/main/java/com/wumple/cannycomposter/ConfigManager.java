package com.wumple.cannycomposter;

import java.nio.file.Path;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.wumple.util.config.ConfigUtil;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

// See
// https://github.com/McJty/YouTubeModding14/blob/master/src/main/java/com/mcjty/mytutorial/Config.java
// https://wiki.mcjty.eu/modding/index.php?title=Tut14_Ep6

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigManager
{
	private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

	public static ForgeConfigSpec COMMON_CONFIG;
	public static ForgeConfigSpec CLIENT_CONFIG;

	public static final String CATEGORY_GENERAL = "General";
	public static final String CATEGORY_DEBUGGING = "Debugging";

	public static class General
	{
		public static ForgeConfigSpec.ConfigValue<Config> compostables;
		public static ForgeConfigSpec.ConfigValue<String> compost;
		public static ForgeConfigSpec.DoubleValue compostChance;
		public static ForgeConfigSpec.IntValue cookMinTime;
		public static ForgeConfigSpec.IntValue cookMaxTime;
		public static ForgeConfigSpec.BooleanValue cookSteam;

		private static void setupConfig()
		{
			COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);

			compostables = ConfigUtil.buildSet(COMMON_BUILDER, "compostables",
					"Compostable items and their compost values (0 - 100)");
			
			compostChance = COMMON_BUILDER.comment("Chance compost works").defineInRange("compostChance", 0.5D, 0.0D, 1.0D);

			compost = COMMON_BUILDER.comment("Compost item generated by composter").define("compost",
					"cannycomposter:compost");
			
			cookMinTime = COMMON_BUILDER.comment("Composter cooking minimum time").defineInRange("cookMinTime",
					10, 0, Integer.MAX_VALUE);

			cookMaxTime = COMMON_BUILDER.comment("Composter cooking max time").defineInRange("cookMaxTime",
					20, 0, Integer.MAX_VALUE);

			cookSteam = COMMON_BUILDER.comment("Does composter cooking make steam").define("cookSteam",
					true);
			
			COMMON_BUILDER.pop();
		}
	}

	public static class Debugging
	{
		public static ForgeConfigSpec.BooleanValue debug;

		private static void setupConfig()
		{
			// @Config.Comment("Debugging options")
			COMMON_BUILDER.comment("Debugging settings").push(CATEGORY_DEBUGGING);

			// @Name("Debug mode")
			debug = COMMON_BUILDER.comment("Enable general debug features, display extra debug info").define("debug",
					false);

			COMMON_BUILDER.pop();
		}
	}

	static
	{
		General.setupConfig();
		Debugging.setupConfig();

		COMMON_CONFIG = COMMON_BUILDER.build();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}

	public static void loadConfig(ForgeConfigSpec spec, Path path)
	{

		final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave()
				.writingMode(WritingMode.REPLACE).build();

		configData.load();
		spec.setConfig(configData);
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent)
	{
	}

	@SubscribeEvent
	public static void onReload(final ModConfig.Reloading configEvent)
	{
	}

	public static void register(final ModLoadingContext context)
	{
		context.registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
		context.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);

		loadConfig(ConfigManager.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve(Reference.MOD_ID + "-client.toml"));
		loadConfig(ConfigManager.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve(Reference.MOD_ID + "-common.toml"));
	}

	// ------------------------------------------------------------------------

	/**
	 * @param event
	 */
	@SubscribeEvent
	public static void modConfig(ModConfig.ModConfigEvent event)
	{
		ModConfig config = event.getConfig();
		if (config.getSpec() != COMMON_CONFIG)
			return;

		/*
		// MAYBE once got an concurrency exception in parent caller, try:
		Minecraft.getInstance().enqueue(new Runnable()
		{
			@Override
			public void run()
			{
				compostablesConfig.copyConfigSet(General.compostables.get());
			}
		});
		*/
		
		compostablesConfig.copyConfigSet(General.compostables.get());
	}

	// convert compostables Config to more usable CompostablesConfig
	public static final CompostablesConfig compostablesConfig = new CompostablesConfig();
}
