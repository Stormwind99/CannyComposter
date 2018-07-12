package com.wumple.composter;

import java.util.HashMap;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MOD_ID)
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModConfig
{
	@Name("Enable compost bonemeal")
	@Config.Comment("Compost triggers plant growth like bonemeal.")
    public static boolean enableCompostBonemeal = true;
	
	@Name("Compost strength")
	@Config.Comment("The probability that compost will succeed relative to bonemeal.")
	@RangeDouble(min=0.0, max=1.0)
    public static double compostBonemealStrength = 0.5F;
	
	@Name("Compost item")
	@Config.Comment("The item that the compost bin generates")
	public static String compostItem = "composter:compost";
	
	@Name("Decomposed items needed")
	@Config.Comment("The number of decomposed items needed to make one compost unit")
	@RangeInt(min=1)
	public static int decomposeNeeded = 8;
	
    @Name("Items")
    @Config.Comment("Set compost amounts for items.")
    public static Items items = new Items();

    public static class Items
    {
        @Name("Compost amount")
        @Config.Comment("Compost amount, -1 means item doesn't compost")
        @RangeInt(min = -1)
        public HashMap<String, Integer> amount = new HashMap<String, Integer>();
    }
    
    @Name("Debugging")
    @Config.Comment("Debugging options")
    public static Debugging zdebugging = new Debugging();

    public static class Debugging
    {
        @Name("Debug mode")
        @Config.Comment("Enable debug features on this menu, display extra debug info.")
        public boolean debug = false;
    }
    
    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    private static class EventHandler
    { 
        /**
         * Inject the new values and save to the config file when the config has been changed from the GUI.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event)
	{
            if (event.getModID().equals(Reference.MOD_ID))
	    {
                ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
