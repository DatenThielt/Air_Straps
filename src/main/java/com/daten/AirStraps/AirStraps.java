package com.daten.AirStraps;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = AirStraps.MODID, name = AirStraps.NAME, version = AirStraps.VERSION)
public class AirStraps
{
    public static final String MODID = "airstraps";
    public static final String NAME = "Air Straps";
    public static final String VERSION = "1.0";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("AIR STRAPS AIR STRAPS AIR STRAPS >> {}", Blocks.DIRT.getRegistryName());
    }
}
