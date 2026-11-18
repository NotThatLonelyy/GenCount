package com.notthatlonely.gencount.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Pushes overlapping labels apart so every label stays fully visible. */
public final class LabelLayout {
    private static final int MAX_PASSES = 32;
    private static final float GAP = 1.0F;

    private LabelLayout() {}

    public static void separate(List<LabelBox> boxes) {
        if (boxes.size() < 2) {
            return;
        }

        List<LabelBox> sorted = new ArrayList<LabelBox>(boxes);
        Collections.sort(
                sorted,
                new Comparator<LabelBox>() {
                    @Override
                    public int compare(LabelBox a, LabelBox b) {
                        int byY = Float.compare(a.centerY, b.centerY);
                        return byY != 0 ? byY : Float.compare(a.centerX, b.centerX);
                    }
                });

        for (int pass = 0; pass < MAX_PASSES; pass++) {
            boolean moved = false;

            for (int i = 0; i < sorted.size(); i++) {
                LabelBox a = sorted.get(i);
                for (int j = i + 1; j < sorted.size(); j++) {
                    LabelBox b = sorted.get(j);

                    float overlapX = a.halfWidth + b.halfWidth - Math.abs(a.centerX - b.centerX);
                    float overlapY = a.halfHeight + b.halfHeight - Math.abs(a.centerY - b.centerY);
                    if (overlapX <= 0F || overlapY <= 0F) {
                        continue;
                    }

                    if (overlapX < overlapY * 0.5F) {
                        b.centerX += (a.centerX <= b.centerX ? 1F : -1F) * (overlapX + GAP);
                    } else {
                        b.centerY += (a.centerY <= b.centerY ? 1F : -1F) * (overlapY + GAP);
                    }
                    moved = true;
                }
            }

            if (!moved) {
                break;
            }
        }
    }
}
