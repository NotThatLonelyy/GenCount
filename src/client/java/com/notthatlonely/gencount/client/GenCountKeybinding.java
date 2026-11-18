package com.notthatlonely.gencount.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class GenCountKeybinding {

    public static final KeyMapping TOGGLE =
            new KeyMapping("key.gencount.toggle", InputConstants.KEY_G, KeyMapping.Category.MISC);

    private GenCountKeybinding() {}
}
