package com.notthatlonely.gencount.legacy;

import com.notthatlonely.gencount.core.GemFamily;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

/**
 * 1.8.9 has no raw ore or iron nuggets, so ore blocks (which drop themselves here) count
 * together with ingots and gold nuggets.
 */
public final class GemMatcher {

    private GemMatcher() {}

    public static GemFamily familyOf(Item item) {
        if (item == null) {
            return null;
        }
        if (item == Items.iron_ingot || item == Item.getItemFromBlock(Blocks.iron_ore)) {
            return GemFamily.IRON;
        }
        if (item == Items.gold_ingot
                || item == Items.gold_nugget
                || item == Item.getItemFromBlock(Blocks.gold_ore)) {
            return GemFamily.GOLD;
        }
        if (item == Items.diamond) {
            return GemFamily.DIAMOND;
        }
        if (item == Items.emerald) {
            return GemFamily.EMERALD;
        }
        return null;
    }
}
