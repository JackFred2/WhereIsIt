package red.jackf.whereisit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import red.jackf.whereisit.Searcher;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.mixin.AccessorDrawableHelper;
import red.jackf.whereisit.mixin.AccessorHandledScreen;

import java.util.*;

@Environment(EnvType.CLIENT)
public abstract class RenderUtils {
    public static final Map<BlockPos, PositionData> FOUND_ITEM_POSITIONS = new HashMap<>();
    /**
     * Hook for changing a {@link PositionData} before rendering, e.g. for changing colour/text.
     */
    public static final Event<RenderLocation> RENDER_LOCATION_EVENT = EventFactory.createArrayBacked(RenderLocation.class, (context, simpleRendering, positionData) -> {
    }, callbacks -> (context, simpleRendering, positionData) -> {
        for (final RenderLocation callback : callbacks) {
            callback.renderLocation(context, simpleRendering, positionData);
        }
    });

    private static final List<BlockPos> toRemove = new ArrayList<>();
    @Nullable
    public static Item lastSearchedItem = null;
    @Nullable
    public static NbtCompound lastSearchedTag = null;
    public static boolean lastSearchedMatchNbt = false;
    private static long lastSearchTime = 0L;

    public static void setLastSearch(Item item, boolean matchNbt, NbtCompound nbt) {
        lastSearchedItem = item;
        lastSearchedMatchNbt = matchNbt;
        lastSearchedTag = nbt;
    }

    // clear the slot and in-world highlight
    public static void clearSearch() {
        FOUND_ITEM_POSITIONS.clear();
        clearSlotSearch();
    }

    // clear the inventory slot highlight
    public static void clearSlotSearch() {
        lastSearchedItem = null;
        lastSearchedMatchNbt = false;
        lastSearchedTag = null;
        lastSearchTime = -1L;
    }

    public static void renderTexts(WorldRenderContext context, Boolean simpleRendering) {
        if (FOUND_ITEM_POSITIONS.size() == 0) return;
        context.world().getProfiler().swap("whereisit_text");

        for (var entry : FOUND_ITEM_POSITIONS.entrySet()) {
            var data = entry.getValue();
            var basePos = Vec3d.of(data.pos);

            if (!context.world().getBlockState(data.pos.up()).isOpaque()) {
                basePos = basePos.add(0, 1d, 0);
            }

            var i = data.getAllText().size() - 1;
            for (Text text : data.getAllText()) {
                var pos = basePos.add(0, i * 0.3d * (WhereIsIt.CONFIG.getTextSizeModifier() / 100f), 0);
                drawTextWithBackground(context, pos, text, 64, true);
                i--;
            }
        }
    }

    public static void renderHighlights(WorldRenderContext context, Boolean simpleRendering) {
        if (lastSearchTime == -1 && lastSearchedItem != null) { // first tick after search
            lastSearchTime = context.world().getTime();
        } else if (context.world().getTime() >= lastSearchTime + WhereIsIt.CONFIG.getFadeoutTime()) { // remove the search highlight after a given time
            clearSlotSearch();
        }

        if (FOUND_ITEM_POSITIONS.size() == 0) return;
        context.world().getProfiler().swap("whereisit_highlights");

        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();

        MatrixStack stack = RenderSystem.getModelViewStack();
        stack.push();
        stack.scale(0.998f, 0.998f, 0.998f); // fixes z fighting by pulling all faces slightly closer to the camera

        stack.multiply(new Quaternion(Vec3f.POSITIVE_X, camera.getPitch(), true));
        stack.multiply(new Quaternion(Vec3f.POSITIVE_Y, camera.getYaw() + 180f, true));

        RenderSystem.applyModelViewMatrix();

        for (Map.Entry<BlockPos, PositionData> entry : FOUND_ITEM_POSITIONS.entrySet()) {
            PositionData positionData = entry.getValue();

            RENDER_LOCATION_EVENT.invoker().renderLocation(context, simpleRendering, positionData);

            long timeDiff = context.world().getTime() - positionData.time;
            float a = ((WhereIsIt.CONFIG.getFadeoutTime() - timeDiff) / (float) WhereIsIt.CONFIG.getFadeoutTime()) * 0.6f;

            Vec3d finalPos = cameraPos.subtract(positionData.pos.getX(), positionData.pos.getY(), positionData.pos.getZ()).negate();
            if (finalPos.lengthSquared() > 4096) { // if it's more than 64 blocks away, scale it so distant ones are still visible
                finalPos = finalPos.normalize().multiply(64);
            }

            RenderSystem.disableDepthTest();

            // Bright boxes, in front of terrain but blocked by it
            if (!simpleRendering) {
                RenderSystem.enableDepthTest();

                buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

                drawShape(buffer, positionData.shape,
                    finalPos.x,
                    finalPos.y,
                    finalPos.z,
                    positionData.r,
                    positionData.g,
                    positionData.b,
                    a);

                tessellator.draw();

                RenderSystem.disableDepthTest();
            }

            RenderSystem.depthFunc(GL11.GL_ALWAYS);

            // Translucent boxes, behind terrain but always visible
            buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

            drawShape(buffer, positionData.shape,
                finalPos.x,
                finalPos.y,
                finalPos.z,
                positionData.r,
                positionData.g,
                positionData.b,
                simpleRendering ? a : a * 0.8f);

            tessellator.draw();

            RenderSystem.depthFunc(GL11.GL_LEQUAL);

            if (timeDiff >= WhereIsIt.CONFIG.getFadeoutTime()) {
                toRemove.add(entry.getKey());
            }
        }

        stack.pop();
        RenderSystem.applyModelViewMatrix();

        Iterator<BlockPos> iter = toRemove.listIterator();
        while (iter.hasNext()) {
            FOUND_ITEM_POSITIONS.remove(iter.next());
            iter.remove();
        }
    }

    /**
     * Draws a hologram of a VoxelShape with a specified colour and position.
     *
     * @param buffer {@link BufferBuilder} to create vertices for.
     * @param shape  {@link VoxelShape} to draw.
     * @param x      X coordinate offset.
     * @param y      Y coordinate offset.
     * @param z      Z coordinate offset.
     * @param r      Red colour component.
     * @param g      Green colour component.
     * @param b      Blue colour component.
     * @param a      Alpha colour component.
     */
    private static void drawShape(BufferBuilder buffer, VoxelShape shape, double x, double y, double z, float r, float g, float b, float a) {
        shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {

            double lowX = x1 + x;
            double lowY = y1 + y;
            double lowZ = z1 + z;
            double highX = x2 + x;
            double highY = y2 + y;
            double highZ = z2 + z;

            //bottom / -y
            buffer.vertex(lowX, lowY, lowZ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY, highZ).color(r, g, b, a).next();
            buffer.vertex(lowX, lowY, highZ).color(r, g, b, a).next();

            buffer.vertex(lowX, lowY, lowZ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY, lowZ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY, highZ).color(r, g, b, a).next();

            //top / +y
            buffer.vertex(lowX, highY, lowZ).color(r, g, b, a).next();
            buffer.vertex(lowX, highY, highZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();

            buffer.vertex(lowX, highY, lowZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, lowZ).color(r, g, b, a).next();

            //west / -x
            buffer.vertex(lowX, lowY, lowZ).color(r, g, b, a).next();
            buffer.vertex(lowX, lowY, highZ).color(r, g, b, a).next();
            buffer.vertex(lowX, highY, highZ).color(r, g, b, a).next();

            buffer.vertex(lowX, lowY, lowZ).color(r, g, b, a).next();
            buffer.vertex(lowX, highY, highZ).color(r, g, b, a).next();
            buffer.vertex(lowX, highY, lowZ).color(r, g, b, a).next();

            //east / +x
            buffer.vertex(highX, lowY, lowZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY, highZ).color(r, g, b, a).next();

            buffer.vertex(highX, lowY, lowZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, lowZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();

            //west / -x
            buffer.vertex(lowX, lowY, highZ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY, highZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();

            buffer.vertex(lowX, lowY, highZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();
            buffer.vertex(lowX, highY, highZ).color(r, g, b, a).next();

            //west / -x
            buffer.vertex(lowX, lowY, lowZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, lowZ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY, lowZ).color(r, g, b, a).next();

            buffer.vertex(lowX, lowY, lowZ).color(r, g, b, a).next();
            buffer.vertex(lowX, highY, lowZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, lowZ).color(r, g, b, a).next();
        });
    }

    /**
     * Draws a highlight over slots that contain the last searched item.
     */
    public static void drawSlotWithLastSearchedItem(MatrixStack matrixStack, HandledScreen<?> screen) {
        if (WhereIsIt.CONFIG.disableSlotHighlight() || lastSearchedItem == null || lastSearchTime == -1L) return;
        int time = (int) ((System.currentTimeMillis() / 15) % 360);
        screen.getScreenHandler().slots.forEach(slot -> {
            ItemStack stack = slot.getStack();
            if (slot.hasStack() && Searcher.areStacksEqual(stack.getItem(), stack.getNbt(), lastSearchedItem, lastSearchedTag, lastSearchedMatchNbt)) {
                int colour;
                if (WhereIsIt.CONFIG.isRainbowMode()) {
                    Vec3f colourRaw = RenderUtils.hueToColour(time + 5 * slot.id);
                    Vec3i colourRGB = new Vec3i(colourRaw.getX() * 255, colourRaw.getY() * 255, colourRaw.getZ() * 255);
                    colour = (((128 << 8) + colourRGB.getX() << 8) + colourRGB.getY() << 8) + colourRGB.getZ();
                } else {
                    colour = 0x80FFFF00;
                }

                int x = slot.x + ((AccessorHandledScreen) screen).whereisit$getX();
                int y = slot.y + ((AccessorHandledScreen) screen).whereisit$getY();
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);
                ((AccessorDrawableHelper) screen).whereisit$fillGradient(matrixStack, x - 2, y - 2, x, y + 18, colour, colour);
                ((AccessorDrawableHelper) screen).whereisit$fillGradient(matrixStack, x + 16, y - 2, x + 18, y + 18, colour, colour);

                ((AccessorDrawableHelper) screen).whereisit$fillGradient(matrixStack, x, y - 2, x + 16, y, colour, colour);
                ((AccessorDrawableHelper) screen).whereisit$fillGradient(matrixStack, x, y + 16, x + 16, y + 18, colour, colour);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.enableDepthTest();
            }
        });
    }

    /**
     * Draws text at a specified location in the world, at the same scale as a nameplate.
     * Call in {@link net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents#AFTER_ENTITIES} for correct occlusion with terrain.
     *
     * @param context     The {@link WorldRenderContext} given by the render event hook.
     * @param pos         The position in-world to render text at.
     * @param text        The {@link Text} object to draw.
     * @param maxDistance The maximum distance, after which the text will not be rendered.
     * @param seeThrough  If the text should be darkened when behind terrain.
     */
    public static void drawTextWithBackground(WorldRenderContext context, Vec3d pos, Text text, int maxDistance, boolean seeThrough) {
        var dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        var finalPos = pos.subtract(context.camera().getPos()).add(0.5, 0.5, 0.5);
        float textScale = WhereIsIt.CONFIG.getTextSizeModifier() / 100f;
        if (finalPos.lengthSquared() <= maxDistance * maxDistance) {
            var matrices = context.matrixStack();
            matrices.push();
            matrices.translate(finalPos.x, finalPos.y, finalPos.z);
            matrices.multiply(dispatcher.getRotation());
            matrices.scale(-0.025F * textScale, -0.025F * textScale, 0.025F * textScale);
            var matrix4f = matrices.peek().getPositionMatrix();
            int backgroundColour = (int) (MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;
            var textRenderer = MinecraftClient.getInstance().textRenderer;
            float xOffset = (float) (-textRenderer.getWidth(text) / 2);
            textRenderer.draw(text, xOffset, 0, 0x20FFFFFF, false, matrix4f, context.consumers(), true, backgroundColour, 0x00F000F0); // background
            textRenderer.draw(text, xOffset, 0, 0xFFFFFFFF, false, matrix4f, context.consumers(), seeThrough, 0, 0x00F000F0); // text
            matrices.pop();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public static Vec3f hueToColour(float hue) {
        hue = ((hue % 360) + 360) % 360;
        float factor = 1 - Math.abs(MathHelper.floorMod(hue / 60f, 2) - 1);

        switch ((int) (hue / 60)) {
            case 0:
                return new Vec3f(1, factor, 0);
            case 1:
                return new Vec3f(factor, 1, 0);
            case 2:
                return new Vec3f(0, 1, factor);
            case 3:
                return new Vec3f(0, factor, 1);
            case 4:
                return new Vec3f(factor, 0, 1);
            case 5:
                return new Vec3f(1, 0, factor);
        }

        throw new RuntimeException("Exhausted switch statement?");
    }

    @FunctionalInterface
    public interface RenderLocation {
        void renderLocation(WorldRenderContext context, Boolean simpleRendering, PositionData positionData);
    }
}
