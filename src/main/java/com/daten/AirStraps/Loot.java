package com.daten.AirStraps;

import com.daten.AirStraps.AirStraps;
import com.daten.AirStraps.init.StrapItems;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = AirStraps.MODID)
public class Loot
{
    private static final @Nonnull LootCondition[] NO_CONDITIONS = new LootCondition[0];

    @SubscribeEvent
    public static void onLootTableLoad(@Nonnull LootTableLoadEvent evt)
    {
        LootPool lp = new LootPool(new LootEntry[0], NO_CONDITIONS, new RandomValueRange(1, 3), new RandomValueRange(0, 0), AirStraps.NAME);
        LootTable looTable = evt.getTable();
        LootEntry basicStrap = new LootEntryItem(StrapItems.BASIC_STRAP, 20, 0, new LootFunction[0], new LootCondition[0], "airstraps:basic_strap");

        if (evt.getName().equals(LootTableList.CHESTS_NETHER_BRIDGE)) {
            lp.addEntry(basicStrap);
        }
        looTable.addPool(lp);
    }
}
