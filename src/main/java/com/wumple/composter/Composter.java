package com.wumple.composter;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, dependencies = Reference.DEPENDENCIES, updateJSON=Reference.UPDATEJSON)
public class Composter
{
    @Mod.Instance(Reference.MOD_ID)
    public static Composter instance;
    
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) 
    {
    	com.wumple.composter.config.ObjectHolder.RegistrationHandler.registerGuiHandlers();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        // add any missing default config properties
        ConfigHandler.init();
    }
}
