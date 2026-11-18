package com.notthatlonely.gencount.client;

import com.notthatlonely.gencount.client.platform.Platform;
import com.notthatlonely.gencount.core.DropCluster;
import com.notthatlonely.gencount.core.DropClusterer;
import com.notthatlonely.gencount.core.DropPoint;
import com.notthatlonely.gencount.core.GemFamily;
import com.notthatlonely.gencount.core.LabelBox;
import com.notthatlonely.gencount.core.LabelLayout;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class GenCountModule {

    private static final double MAX_DISTANCE = 48.0;
    /** How far above the drops the bottom edge of the label sits, in blocks. */
    private static final double LABEL_RISE = 0.55;

    private static final float NAMETAG_SCALE = 0.025F;
    private static final float MIN_SCALE = 0.6F;
    private static final float MAX_SCALE = 1.5F;
    private static final float MIN_DISTANCE = 0.5F;

    private static final int MARGIN = 2;
    private static final int LINE_GAP = 1;
    private static final int BACKGROUND = 0x90000000;

    private static final List<DropCluster> clusters = new ArrayList<>();
    private static final Matrix4f projectionBuffer = new Matrix4f();
    private static final Vector4f transformBuffer = new Vector4f();

    private static boolean enabled = true;

    private GenCountModule() {}

    public static void toggle() {
        enabled = !enabled;
        final Minecraft minecraft = Minecraft.getInstance();
        Platform.hooks()
                .showOverlayMessage(
                        minecraft,
                        Component.translatable(enabled ? "gencount.enabled" : "gencount.disabled"));
    }

    public static void onClientTick(final Minecraft minecraft) {
        clusters.clear();
        if (!enabled || minecraft.level == null) {
            return;
        }

        final Vec3 cameraPosition = Platform.hooks().getCamera(minecraft).position();
        final double maxDistanceSquared = MAX_DISTANCE * MAX_DISTANCE;
        final List<DropPoint> points = new ArrayList<>();

        for (final Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity itemEntity) || itemEntity.isRemoved()) {
                continue;
            }

            final ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) {
                continue;
            }

            final GemFamily family = GemMatcher.familyOf(stack.getItem());
            if (family == null) {
                continue;
            }

            final Vec3 position = itemEntity.position();
            if (position.distanceToSqr(cameraPosition) > maxDistanceSquared) {
                continue;
            }

            points.add(
                    new DropPoint(
                            position.x,
                            position.y,
                            position.z,
                            family,
                            stack.getCount(),
                            itemEntity));
        }

        if (!points.isEmpty()) {
            clusters.addAll(DropClusterer.cluster(points));
        }
    }

    public static void render(final GuiGraphicsExtractor graphics, final DeltaTracker deltaTracker) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (!enabled || minecraft.level == null || clusters.isEmpty()) {
            return;
        }

        final float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
        final Camera camera = Platform.hooks().getCamera(minecraft);
        final Font font = minecraft.font;
        final List<LabelBox> boxes = new ArrayList<>();

        for (final DropCluster cluster : clusters) {
            final Vec3 world = center(cluster, tickDelta).add(0.0, LABEL_RISE, 0.0);
            if (world.distanceTo(camera.position()) > MAX_DISTANCE) {
                continue;
            }

            final Vector3f screen = project(world, camera);
            if (screen == null) {
                continue;
            }

            final float scale = labelScale(minecraft, camera, screen.z);
            final List<GemFamily> families = cluster.families();
            final List<String> lines = new ArrayList<>(families.size());

            int contentWidth = 0;
            for (final GemFamily family : families) {
                final String line = cluster.line(family);
                lines.add(line);
                contentWidth = Math.max(contentWidth, font.width(Component.literal(line)));
            }

            final int contentHeight =
                    lines.size() * font.lineHeight + (lines.size() - 1) * LINE_GAP;
            final float halfWidth = (contentWidth + MARGIN * 2F) * scale * 0.5F;
            final float halfHeight = (contentHeight + MARGIN * 2F) * scale * 0.5F;

            // Anchor the BOTTOM of the label on the projected point so it floats above the drops.
            boxes.add(
                    new LabelBox(
                            screen.x,
                            screen.y - halfHeight,
                            halfWidth,
                            halfHeight,
                            new Label(families, lines, contentWidth, contentHeight, scale)));
        }

        if (boxes.isEmpty()) {
            return;
        }

        // Labels of different spots never sit on top of each other.
        LabelLayout.separate(boxes);

        graphics.nextStratum();
        for (final LabelBox box : boxes) {
            draw(graphics, font, box);
        }
    }

    private static void draw(
            final GuiGraphicsExtractor graphics, final Font font, final LabelBox box) {
        final Label label = (Label) box.data;

        graphics.pose().pushMatrix();
        graphics.pose().translate(box.centerX, box.centerY);
        graphics.pose().scale(label.scale, label.scale);
        graphics.pose().translate(-label.width * 0.5F, -label.height * 0.5F);

        graphics.fill(
                -MARGIN, -MARGIN, label.width + MARGIN, label.height + MARGIN, BACKGROUND);

        for (int i = 0; i < label.lines.size(); i++) {
            graphics.centeredText(
                    font,
                    Component.literal(label.lines.get(i)),
                    label.width / 2,
                    i * (font.lineHeight + LINE_GAP),
                    label.families.get(i).argb());
        }

        graphics.pose().popMatrix();
    }

    private static Vec3 center(final DropCluster cluster, final float tickDelta) {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;

        final List<DropPoint> points = cluster.points();
        for (final DropPoint point : points) {
            final Vec3 position = ((ItemEntity) point.ref).getPosition(tickDelta);
            x += position.x;
            y += position.y;
            z += position.z;
        }

        final int size = points.size();
        return new Vec3(x / size, y / size, z / size);
    }

    private static float labelScale(
            final Minecraft minecraft, final Camera camera, final float distance) {
        final float guiHeight = minecraft.getWindow().getGuiScaledHeight();
        final float halfFov = (float) Math.tan(Math.toRadians(camera.getFov()) * 0.5);
        final float depth = Math.max(distance, MIN_DISTANCE);
        final float pixelsPerBlock = guiHeight / (2.0F * depth * halfFov);

        return Mth.clamp(NAMETAG_SCALE * 1.6F * pixelsPerBlock, MIN_SCALE, MAX_SCALE);
    }

    private static Vector3f project(final Vec3 position, final Camera camera) {
        final Minecraft minecraft = Minecraft.getInstance();
        final Matrix4f viewProjection = camera.getViewRotationProjectionMatrix(projectionBuffer);
        final Vec3 cameraPosition = camera.position();

        transformBuffer
                .set(
                        (float) (position.x - cameraPosition.x),
                        (float) (position.y - cameraPosition.y),
                        (float) (position.z - cameraPosition.z),
                        1.0F)
                .mul(viewProjection);

        if (transformBuffer.w <= 0.0F) {
            return null;
        }

        final float ndcX = transformBuffer.x / transformBuffer.w;
        final float ndcY = transformBuffer.y / transformBuffer.w;

        return new Vector3f(
                (ndcX + 1.0F) * 0.5F * minecraft.getWindow().getGuiScaledWidth(),
                (1.0F - ndcY) * 0.5F * minecraft.getWindow().getGuiScaledHeight(),
                transformBuffer.w);
    }

    private record Label(
            List<GemFamily> families, List<String> lines, int width, int height, float scale) {}
}
