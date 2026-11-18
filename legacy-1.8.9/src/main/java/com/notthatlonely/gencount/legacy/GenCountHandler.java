package com.notthatlonely.gencount.legacy;

import com.notthatlonely.gencount.core.DropCluster;
import com.notthatlonely.gencount.core.DropClusterer;
import com.notthatlonely.gencount.core.DropPoint;
import com.notthatlonely.gencount.core.GemFamily;
import com.notthatlonely.gencount.core.LabelBox;
import com.notthatlonely.gencount.core.LabelLayout;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class GenCountHandler {

    public static final KeyBinding TOGGLE =
            new KeyBinding("key.gencount.toggle", Keyboard.KEY_G, "key.categories.misc");

    private static final double MAX_DISTANCE = 48.0;
    private static final double LABEL_RISE = 0.55;
    /** Fixed label size: the same on screen at any distance. Very small on purpose. */
    private static final float LABEL_SCALE = 0.5F;
    /** Text opacity: 60%. There is no background box. */
    private static final float TEXT_OPACITY = 0.6F;

    private static final int MARGIN = 1;
    private static final int LINE_GAP = 1;

    private final List<DropCluster> clusters = new ArrayList<DropCluster>();
    private final List<Projected> projected = new ArrayList<Projected>();

    private final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
    private final IntBuffer viewport = BufferUtils.createIntBuffer(16);
    private final FloatBuffer screen = BufferUtils.createFloatBuffer(3);

    private boolean enabled = true;

    /** Scan the world once per tick and group the tracked drops. */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        while (TOGGLE.isPressed()) {
            enabled = !enabled;
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText(
                                "\u00a7eGenCount: " + (enabled ? "\u00a7aON" : "\u00a7cOFF")));
            }
        }

        clusters.clear();
        if (!enabled || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        double maxSq = MAX_DISTANCE * MAX_DISTANCE;
        List<DropPoint> points = new ArrayList<DropPoint>();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityItem) || entity.isDead) {
                continue;
            }

            ItemStack stack = ((EntityItem) entity).getEntityItem();
            if (stack == null) {
                continue;
            }

            GemFamily family = GemMatcher.familyOf(stack.getItem());
            if (family == null || entity.getDistanceSqToEntity(mc.thePlayer) > maxSq) {
                continue;
            }

            // No see-through-walls: skip drops that are hidden behind blocks.
            if (!mc.thePlayer.canEntityBeSeen(entity)) {
                continue;
            }

            points.add(
                    new DropPoint(
                            entity.posX, entity.posY, entity.posZ, family, stack.stackSize, entity));
        }

        if (!points.isEmpty()) {
            clusters.addAll(DropClusterer.cluster(points));
        }
    }

    /** The 3D camera matrices only exist here, so project every label to the screen now. */
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        projected.clear();
        if (!enabled || clusters.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        Entity view = mc.getRenderViewEntity();
        if (view == null) {
            return;
        }

        float pt = event.partialTicks;
        double viewX = lerp(view.lastTickPosX, view.posX, pt);
        double viewY = lerp(view.lastTickPosY, view.posY, pt);
        double viewZ = lerp(view.lastTickPosZ, view.posZ, pt);

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

        int scaleFactor = new ScaledResolution(mc).getScaleFactor();

        for (DropCluster cluster : clusters) {
            double x = 0;
            double y = 0;
            double z = 0;
            List<DropPoint> points = cluster.points();
            for (DropPoint point : points) {
                Entity entity = (Entity) point.ref;
                x += lerp(entity.lastTickPosX, entity.posX, pt);
                y += lerp(entity.lastTickPosY, entity.posY, pt);
                z += lerp(entity.lastTickPosZ, entity.posZ, pt);
            }
            int n = points.size();

            double dx = x / n - viewX;
            double dy = y / n + LABEL_RISE - viewY;
            double dz = z / n - viewZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > MAX_DISTANCE) {
                continue;
            }

            screen.clear();
            if (!GLU.gluProject((float) dx, (float) dy, (float) dz, modelView, projection, viewport, screen)) {
                continue;
            }

            float depth = screen.get(2);
            if (depth < 0F || depth > 1F) {
                continue; // behind the camera
            }

            projected.add(
                    new Projected(
                            cluster,
                            screen.get(0) / scaleFactor,
                            (mc.displayHeight - screen.get(1)) / scaleFactor,
                            (float) distance));
        }
    }

    /** Draw the labels in 2D, with the same overlap handling as the 26.x versions. */
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || projected.isEmpty()) {
            return;
        }

        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        List<LabelBox> boxes = new ArrayList<LabelBox>();

        for (Projected p : projected) {
            float scale = LABEL_SCALE;
            List<GemFamily> families = p.cluster.families();
            List<String> lines = new ArrayList<String>();

            int width = 0;
            for (GemFamily family : families) {
                String line = p.cluster.line(family);
                lines.add(line);
                width = Math.max(width, font.getStringWidth(line));
            }
            int height = lines.size() * font.FONT_HEIGHT + (lines.size() - 1) * LINE_GAP;

            float halfWidth = (width + MARGIN * 2F) * scale * 0.5F;
            float halfHeight = (height + MARGIN * 2F) * scale * 0.5F;

            // Bottom of the label rests on the projected point, i.e. above the drops
            boxes.add(
                    new LabelBox(
                            p.x, p.y - halfHeight, halfWidth, halfHeight,
                            new Label(families, lines, width, height, scale)));
        }

        // Different spots never cover each other
        LabelLayout.separate(boxes);

        // Needed so the 60% text opacity actually blends (no background box draws it for us)
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        for (LabelBox box : boxes) {
            Label label = (Label) box.data;

            GlStateManager.pushMatrix();
            GlStateManager.translate(box.centerX, box.centerY, 0F);
            GlStateManager.scale(label.scale, label.scale, 1F);
            GlStateManager.translate(-label.width * 0.5F, -label.height * 0.5F, 0F);

            for (int i = 0; i < label.lines.size(); i++) {
                String line = label.lines.get(i);
                float x = (label.width - font.getStringWidth(line)) * 0.5F;
                float y = i * (font.FONT_HEIGHT + LINE_GAP);
                font.drawStringWithShadow(line, x, y, label.families.get(i).argb(TEXT_OPACITY));
            }

            GlStateManager.popMatrix();
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private static double lerp(double previous, double current, float partial) {
        return previous + (current - previous) * partial;
    }

    private static final class Projected {
        final DropCluster cluster;
        final float x;
        final float y;
        final float distance;

        Projected(DropCluster cluster, float x, float y, float distance) {
            this.cluster = cluster;
            this.x = x;
            this.y = y;
            this.distance = distance;
        }
    }

    private static final class Label {
        final List<GemFamily> families;
        final List<String> lines;
        final int width;
        final int height;
        final float scale;

        Label(List<GemFamily> families, List<String> lines, int width, int height, float scale) {
            this.families = families;
            this.lines = lines;
            this.width = width;
            this.height = height;
            this.scale = scale;
        }
    }
}
