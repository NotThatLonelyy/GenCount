package com.notthatlonely.gencount.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Drops that lie close together. One cluster = one label, whatever mix of families it holds. */
public final class DropCluster {
    private final List<DropPoint> points = new ArrayList<DropPoint>();
    private final int[] totals = new int[GemFamily.VALUES.length];

    void add(DropPoint point) {
        points.add(point);
        totals[point.family.ordinal()] += point.count;
    }

    public List<DropPoint> points() {
        return Collections.unmodifiableList(points);
    }

    public int total(GemFamily family) {
        return totals[family.ordinal()];
    }

    /** Families present in this cluster, always in the same order (iron, gold, diamond, emerald). */
    public List<GemFamily> families() {
        List<GemFamily> result = new ArrayList<GemFamily>(4);
        for (GemFamily family : GemFamily.VALUES) {
            if (totals[family.ordinal()] > 0) {
                result.add(family);
            }
        }
        return result;
    }

    public String line(GemFamily family) {
        return family.label() + " x" + total(family);
    }
}
