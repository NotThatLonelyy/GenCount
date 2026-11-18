package com.notthatlonely.gencount.client.platform;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** The few calls whose names differ between Minecraft 26.x versions. */
public interface PlatformHooks {

    Camera getCamera(Minecraft minecraft);

    void showOverlayMessage(Minecraft minecraft, Component message);
}
