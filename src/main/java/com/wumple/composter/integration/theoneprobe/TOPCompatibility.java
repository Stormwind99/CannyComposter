package com.wumple.composter.integration.theoneprobe;

import net.minecraftforge.fml.common.event.FMLInterModComms;

public class TOPCompatibility
{
    private static boolean registered;

    public static void register()
    {
        if (registered) { return; }
        registered = true;
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "com.wumple.composter.integration.theoneprobe.TOPProvider");
    }
}