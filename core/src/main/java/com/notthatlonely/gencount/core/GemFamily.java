package com.notthatlonely.gencount.core;

/** The four tracked drop families and the color each one is drawn in. */
public enum GemFamily {
    IRON("Iron", 0xC0C0C0), // silver
    GOLD("Gold", 0xFFE03A), // yellow
    DIAMOND("Diamond", 0x55FFFF), // cyan
    EMERALD("Emerald", 0x55FF55); // green

    public static final GemFamily[] VALUES = values();

    private final String label;
    private final int rgb;

    GemFamily(String label, int rgb) {
        this.label = label;
        this.rgb = rgb;
    }

    public String label() {
        return label;
    }

    public int rgb() {
        return rgb;
    }

    public int argb() {
        return 0xFF000000 | rgb;
    }
}
