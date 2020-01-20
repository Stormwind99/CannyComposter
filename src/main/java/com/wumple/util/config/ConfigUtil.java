package com.wumple.util.config;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigUtil
{
	// ------------------------------------------------------------------------
	// copy values from a Config object into a Map for actual usage
	
	public static <V> void handlePairConfig(Config config, Map<String, V> map)
	{
		config.entrySet().forEach((e) -> {
			map.put(e.getKey(), e.getValue());
		});
	}

	public static void handlePairConfig(Config config, Consumer<Config.Entry> func)
	{
		config.entrySet().forEach((e) -> {
			func.accept(e);
		});
	}

	public static void handlePairConfig(Config config, BiConsumer<String, String> func)
	{
		handlePairConfig(config, (e) -> {
			func.accept(e.getKey(), e.getValue());
		});
	}

	public static void handlePairConfig(Config config, Pattern pattern, BiConsumer<Config.Entry, Matcher> func)
	{
		handlePairConfig(config, (e) -> {
			Matcher m = pattern.matcher(e.getKey());
			if (m.matches())
			{
				func.accept(e, m);
			}
		});
	}

	// ------------------------------------------------------------------------
	// Search a map containing overridable config-like values
	
	/// Search a map for values, but fail if found with falseValue
	public static <V> boolean containsNotFalseValue(Map<String, V> map, String key, V falseValue)
	{
		V value = map.get(key);

		if (value != null)
		{
			if (value != falseValue)
			{
				return true;
			}
		}

		return false;
	}
	
	/// Search a map for values, but return falseValue if not found
	public static <V> V getOrFalseValue(Map<String, V> map, String key, V falseValue)
	{
		V value = map.get(key);

		if (value != null)
		{
			return value;
		}

		return falseValue;
	}

	// ------------------------------------------------------------------------

	/// shortcut to build a config value for a key/value set
	public static ForgeConfigSpec.ConfigValue<Config> buildSet(ForgeConfigSpec.Builder builder, String name, String comment)
	{
		// Example from https://github.com/gigaherz/Survivalist/blob/1.14/src/main/java/gigaherz/survivalist/ConfigManager.java#L131 
		ForgeConfigSpec.ConfigValue<Config> value = builder
				.comment(comment)
				.define(Arrays.asList(name),
						() -> Config.of(TomlFormat.instance()), 
						x -> true, 
						Config.class);

		return value;
	}

	/// make r process config set c, clear the mirrored map, and read c into the mirrored map
	public static void handleConfigSet(Config c, Consumer<Config> r, Map<String,?> map)
	{
		r.accept(c);
		map.clear();
		ConfigUtil.handlePairConfig(c, map);
	}
}
