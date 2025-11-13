package com.daten.AirStraps;

import com.daten.AirStraps.init.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(AirStraps.MODID)
public class AirStraps
{
    public static final String MODID = "airstraps";
    public static final String NAME = "Air Straps";
    public static final String VERSION = "1.0";

    private static final Logger LOGGER = LogUtils.getLogger();

    public AirStraps(IEventBus modEventBus)
    {
        // Register items
        ModItems.ITEMS.register(modEventBus);

        LOGGER.info("Air Straps mod initialized");
        LOGGER.info("AIR STRAPS >> {}", Blocks.DIRT.getName());
    }
}
