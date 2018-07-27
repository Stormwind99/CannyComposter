package com.wumple.composter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wumple.composter.capability.CompostBinCap;
import com.wumple.composter.config.ConfigHandler;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, dependencies = Reference.DEPENDENCIES, updateJSON = Reference.UPDATEJSON, certificateFingerprint=Reference.FINGERPRINT)
public class Composter
{
    @Mod.Instance(Reference.MOD_ID)
    public static Composter instance;
    
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        
        CompostBinCap.register();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) 
    {
    	com.wumple.composter.ObjectHolder.RegistrationHandler.registerGuiHandlers();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        // add any missing default config properties
        ConfigHandler.init();
    }

    @EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event)
    {
        if (logger == null)
        {
            logger = LogManager.getLogger(Reference.MOD_ID);
        }
        if (logger != null)
        {
            logger.warn("Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with. This version will NOT be supported by the author!");
            logger.warn("Expected " + event.getExpectedFingerprint() + " found " + event.getFingerprints().toString());
        }
    }
}
