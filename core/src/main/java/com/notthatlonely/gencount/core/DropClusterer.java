package com.notthatlonely.gencount.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Groups drops that are near each other, so items at the same spot share a single label. */
public final class DropClusterer {
    public static final double LINK_HORIZONTAL = 1.0;
    public static final double LINK_VERTICAL = 1.5;

    private DropClusterer() {}

    public static List<DropCluster> cluster(List<DropPoint> points) {
        int size = points.size();
        int[] parent = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }

        for (int i = 0; i < size; i++) {
            DropPoint a = points.get(i);
            for (int j = i + 1; j < size; j++) {
                DropPoint b = points.get(j);
                if (Math.abs(a.x - b.x) <= LINK_HORIZONTAL
                        && Math.abs(a.z - b.z) <= LINK_HORIZONTAL
                        && Math.abs(a.y - b.y) <= LINK_VERTICAL) {
                    union(parent, i, j);
                }
            }
        }

        Map<Integer, DropCluster> groups = new LinkedHashMap<Integer, DropCluster>();
        for (int i = 0; i < size; i++) {
            int root = find(parent, i);
            DropCluster cluster = groups.get(root);
            if (cluster == null) {
                cluster = new DropCluster();
                groups.put(root, cluster);
            }
            cluster.add(points.get(i));
        }
        return new ArrayList<DropCluster>(groups.values());
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    private static void union(int[] parent, int a, int b) {
        int ra = find(parent, a);
        int rb = find(parent, b);
        if (ra != rb) {
            parent[rb] = ra;
        }
    }
}
