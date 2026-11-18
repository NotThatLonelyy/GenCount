package com.notthatlonely.gencount.core;

/** One dropped stack of a tracked family. {@code ref} is the game entity, kept for later use. */
public final class DropPoint {
    public final double x;
    public final double y;
    public final double z;
    public final GemFamily family;
    public final int count;
    public final Object ref;

    public DropPoint(double x, double y, double z, GemFamily family, int count, Object ref) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.family = family;
        this.count = count;
        this.ref = ref;
    }
}
