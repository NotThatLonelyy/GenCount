package com.notthatlonely.gencount.core;

/** A label's screen rectangle (center + half size), movable by {@link LabelLayout}. */
public final class LabelBox {
    public float centerX;
    public float centerY;
    public final float halfWidth;
    public final float halfHeight;
    public final Object data;

    public LabelBox(float centerX, float centerY, float halfWidth, float halfHeight, Object data) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.data = data;
    }
}
