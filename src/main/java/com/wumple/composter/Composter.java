package com.wumple.composter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wumple.composter.capability.CompostBinCap;
import com.wumple.composter.config.ConfigHandler;
import com.wumple.composter.integration.theoneprobe.TOPCompatibility;
import com.wumple.composter.integration.waila.WailaCompatibility;
import com.wumple.util.mod.ModBase;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, dependencies = Reference.DEPENDENCIES, updateJSON = Reference.UPDATEJSON, certificateFingerprint = Reference.FINGERPRINT)
public class Composter extends ModBase
{
    @Mod.Instance(Reference.MOD_ID)
    public static Composter instance;

    @EventHandler
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        CompostBinCap.register();
        TOPCompatibility.register();
        WailaCompatibility.register();
    }

    @EventHandler
    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        ObjectHolder.RegistrationHandler.registerGuiHandlers();
    }

    @EventHandler
    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
        // add any missing default config properties
        ConfigHandler.init();
    }

    @EventHandler
    @Override
    public void onFingerprintViolation(FMLFingerprintViolationEvent event)
    {
        super.onFingerprintViolation(event);
    }
    
    @Override
    public Logger getLoggerFromManager()
    {
        return LogManager.getLogger(Reference.MOD_ID);
    }
}
