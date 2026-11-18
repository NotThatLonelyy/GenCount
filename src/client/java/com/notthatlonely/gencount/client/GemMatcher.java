package com.notthatlonely.gencount.client;

import com.notthatlonely.gencount.core.GemFamily;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Maps a dropped item to the family it is counted under (or null if it is not tracked). */
public final class GemMatcher {

    private GemMatcher() {}

    public static GemFamily familyOf(final Item item) {
        if (item == Items.IRON_INGOT || item == Items.RAW_IRON || item == Items.IRON_NUGGET) {
            return GemFamily.IRON;
        }
        if (item == Items.GOLD_INGOT || item == Items.RAW_GOLD || item == Items.GOLD_NUGGET) {
            return GemFamily.GOLD;
        }
        if (item == Items.DIAMOND) {
            return GemFamily.DIAMOND;
        }
        if (item == Items.EMERALD) {
            return GemFamily.EMERALD;
        }
        return null;
    }
}
