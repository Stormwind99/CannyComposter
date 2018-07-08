package com.wumple.composter;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Name;
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
    public static double compostBonemealStrength = 0.5F;
    
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
