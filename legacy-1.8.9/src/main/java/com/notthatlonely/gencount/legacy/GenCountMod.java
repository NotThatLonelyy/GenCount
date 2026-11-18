package com.notthatlonely.gencount.legacy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
        modid = GenCountMod.MODID,
        name = "GenCount",
        version = GenCountMod.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8.9]")
public class GenCountMod {
    public static final String MODID = "gencount";
    /** Keep in sync with "version" in ../gradle.properties. */
    public static final String VERSION = "1.0.0";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(GenCountHandler.TOGGLE);
        MinecraftForge.EVENT_BUS.register(new GenCountHandler());
    }
}
