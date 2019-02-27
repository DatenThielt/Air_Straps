package com.daten.AirStraps.init;

import com.daten.AirStraps.items.ItemBasicStrap;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class RegistryHandler {

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {

        final Item[] items = {

                new ItemBasicStrap( "itemStrap", "basic_strap", 2.0F, 65),
                new ItemBasicStrap( "itemStrapIron","iron_strap", 4.0F, 128),
                new ItemBasicStrap( "itemStrapGold","gold_strap", 6.0F, 256),
                new ItemBasicStrap( "itemStrapDiamond","diamond_strap", 8.0F, 512),
                new ItemBasicStrap( "itemStrapInvincible","invincible_strap", 10.0F, -1),
        };

        event.getRegistry().registerAll(items);

    }

}
