package com.daten.AirStraps.init;

import com.daten.AirStraps.AirStraps;
import com.daten.AirStraps.items.ItemBasicStrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, AirStraps.MODID);

    public static final DeferredHolder<Item, ItemBasicStrap> BASIC_STRAP = ITEMS.register("basic_strap",
            () -> new ItemBasicStrap(2.0F, 65));

    public static final DeferredHolder<Item, ItemBasicStrap> IRON_STRAP = ITEMS.register("iron_strap",
            () -> new ItemBasicStrap(4.0F, 128));

    public static final DeferredHolder<Item, ItemBasicStrap> GOLD_STRAP = ITEMS.register("gold_strap",
            () -> new ItemBasicStrap(6.0F, 256));

    public static final DeferredHolder<Item, ItemBasicStrap> DIAMOND_STRAP = ITEMS.register("diamond_strap",
            () -> new ItemBasicStrap(8.0F, 512));

    public static final DeferredHolder<Item, ItemBasicStrap> INVINCIBLE_STRAP = ITEMS.register("invincible_strap",
            () -> new ItemBasicStrap(10.0F, -1));
}
