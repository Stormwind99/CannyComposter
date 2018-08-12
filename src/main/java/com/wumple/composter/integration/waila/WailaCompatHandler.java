package com.wumple.composter.integration.waila;

import net.minecraftforge.fml.common.Loader;

public class WailaCompatHandler
{
    public static void register()
    {
        if (Loader.isModLoaded("waila"))
        {
            WailaCompatibility.register();
        }
    }
}
