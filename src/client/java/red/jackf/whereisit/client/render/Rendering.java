package red.jackf.whereisit.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.util.*;

@SuppressWarnings("resource") // i really don't want to call ClientLevel#close() thanks
public class Rendering {
    private static final Map<BlockPos, SearchResult> results = new HashMap<>();
    private static final Map<BlockPos, SearchResult> namedResults = new HashMap<>();

    private static final List<Pair<Vec3, Component>> scheduledLabels = new ArrayList<>();

    private static long ticksSinceSearch = 0;
    @Nullable
    private static SearchRequest lastRequest = null;

    public static void setup() {
        // schedule highlight label renders
        WorldRenderEvents.START.register(context -> {
            if (!WhereIsItConfig.INSTANCE.instance().getClient().showContainerNamesInResults) return;
            for (SearchResult value : namedResults.values())
                scheduleLabel(value.pos().getCenter().add(value.nameOffset()), value.name());
        });

        // render scheduled labels
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hit) -> {
            if (scheduledLabels.isEmpty()) return true;

            renderLabels(context);
            scheduledLabels.clear();
            return true;
        });

        // label boxes
        WorldRenderEvents.END.register(context -> {
            if (results.isEmpty()) return;

            var progress = Mth.clamp((getTicksSinceSearch() + context.tickDelta()) / WhereIsItConfig.INSTANCE.instance()
                    .getCommon().fadeoutTimeTicks, 0f, 1f);

            if (context.world() == null || progress > 1f) {
                return;
            }

            renderBoxes(context, progress);
        });
    }

    // smooth scaling for the cube highlights
    private static float easingFunc(float progress) {
        var power = 32f;
        return (float) ((1 - Math.pow(progress, power)) * (1 - Math.pow(1 - progress, power)) * (1 - (progress / 4f)));
    }

    public static void addResults(Collection<SearchResult> newResults) {
        for (SearchResult result : newResults) {
            // TODO: when names are added, prioritise the one with a name
            results.put(result.pos(), result);
            if (result.name() != null) namedResults.put(result.pos(), result);
        }
    }

    public static void setLastRequest(@Nullable SearchRequest request) {
        lastRequest = request;
    }

    public static void clearResults() {
        lastRequest = null;
        results.clear();
        namedResults.clear();
    }

    public static Map<BlockPos, SearchResult> getResults() {
        return results;
    }

    public static Map<BlockPos, SearchResult> getNamedResults() {
        return namedResults;
    }

    private static float getBaseProgress(long ticks, float delta) {
        float base = ticks + delta;
        base *= WhereIsItConfig.INSTANCE.instance().getClient().highlightTimeFactor;
        return (base % 80) / 80;
    }

    //////////////////////
    // SCREEN RENDERING //
    //////////////////////

    // render a highlight behind an item
    public static void renderSlotHighlight(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            var time = getBaseProgress(getTicksSinceSearch(), tickDelta);
            for (Slot slot : containerScreen.getMenu().slots) {
                if (slot.isActive() && slot.hasItem() && lastRequest != null &&
                        SearchRequest.check(slot.getItem(), lastRequest)) {
                    var x = slot.x + containerScreen.leftPos;
                    var y = slot.y + containerScreen.topPos;
                    var progress = time;
                    progress += (slot.x / 256f) * WhereIsItConfig.INSTANCE.instance().getClient().slotHighlightXFactor;
                    progress -= ((mouseX + mouseY) / 1280f) * WhereIsItConfig.INSTANCE.instance().getClient().slotHighlightMouseFactor;
                    var colour = CurrentGradientHolder.getColour(progress);
                    graphics.fill(x, y, x + 16, y + 16, colour);
                }
            }
        }
    }

    ////////////////////
    // TEXT RENDERING //
    ////////////////////

    // schedule a label to be rendered; should be called before BEFORE_BLOCK_OUTLINE every frame
    public static void scheduleLabel(Vec3 pos, Component name) {
        scheduledLabels.add(Pair.of(pos, name));
    }

    // does the rendering of labels at BEFORE_BLOCK_OUTLINE
    @SuppressWarnings("DataFlowIssue")
    private static void renderLabels(WorldRenderContext context) {
        scheduledLabels.stream().sorted(Comparator.comparingDouble(pair ->
                // sort by furthest to camera inwards
                -context.camera().rotation().transformInverse(pair.getFirst()
                        //.subtract(context.camera().getPosition())
                        .toVector3f()).z
        )).forEach(pair ->
                renderLabel(
                        pair.getFirst(),
                        pair.getSecond(),
                        context.matrixStack(),
                        context.camera(),
                        context.consumers()));
    }

    // render an individual label
    public static void renderLabel(Vec3 pos, Component name, PoseStack pose, Camera camera, MultiBufferSource consumers) {
        pose.pushPose();

        pos = pos.subtract(camera.getPosition());

        pose.translate(pos.x, pos.y, pos.z);
        pose.mulPose(camera.rotation());
        var factor = 0.025f * WhereIsItConfig.INSTANCE.instance().getClient().containerNameLabelScale;
        pose.scale(-factor, -factor, factor);
        var matrix4f = pose.last().pose();
        var width = Minecraft.getInstance().font.width(name);
        float x = (float) -width / 2;

        var bgBuffer = consumers.getBuffer(RenderType.textBackgroundSeeThrough());
        var bgColour = ((int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255F)) << 24;
        bgBuffer.vertex(matrix4f, x - 1, -1f, 0).color(bgColour).uv2(LightTexture.FULL_BRIGHT).endVertex();
        bgBuffer.vertex(matrix4f, x - 1, 10f, 0).color(bgColour).uv2(LightTexture.FULL_BRIGHT).endVertex();
        bgBuffer.vertex(matrix4f, x + width, 10f, 0).color(bgColour).uv2(LightTexture.FULL_BRIGHT).endVertex();
        bgBuffer.vertex(matrix4f, x + width, -1f, 0).color(bgColour).uv2(LightTexture.FULL_BRIGHT).endVertex();

        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        Minecraft.getInstance().font.drawInBatch(name, x, 0, 0x20_FFFFFF, false,
                matrix4f, consumers, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
        Minecraft.getInstance().font.drawInBatch(name, x, 0, 0xFF_FFFFFF, false,
                matrix4f, consumers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableDepthTest();


        pose.popPose();
    }

    /////////////////////
    // WORLD RENDERING //
    /////////////////////

    // render all boxes at END
    private static void renderBoxes(WorldRenderContext context, float progress) {
        var camera = context.camera();

        var pose = new PoseStack();
        pose.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        pose.mulPose(Axis.YP.rotationDegrees(camera.getYRot() - 180f));

        // from 100% to 50%
        var alpha = 1 - (progress / 2f);
        var colour = CurrentGradientHolder.getColour(getBaseProgress(getTicksSinceSearch(), context.tickDelta()));
        var scale = easingFunc(progress);

        var tesselator = Tesselator.getInstance();
        var builder = tesselator.getBuilder();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.defaultColor(
                FastColor.ARGB32.red(colour),
                FastColor.ARGB32.green(colour),
                FastColor.ARGB32.blue(colour),
                (int) (alpha * 255)
        );

        for (SearchResult result : results.values()) {
            renderBox(
                    camera.getPosition(),
                    result.pos(),
                    builder,
                    pose,
                    scale
            );

            for (BlockPos otherPos : result.otherPositions()) {
                renderBox(
                        camera.getPosition(),
                        otherPos,
                        builder,
                        pose,
                        scale
                );
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        RenderSystem.enableBlend();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        tesselator.end();

        builder.unsetDefaultColor();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();

    }

    // render an individual box
    private static void renderBox(Vec3 cameraPos, BlockPos pos, VertexConsumer consumer, PoseStack pose, float scale) {
        pose.pushPose();
        // done here to fix floating point issues (e.g. at world border)
        final double xOffset = pos.getX() + (0.5 - cameraPos.x);
        final double yOffset = pos.getY() + (0.5 - cameraPos.y);
        final double zOffset = pos.getZ() + (0.5 - cameraPos.z);
        pose.translate(xOffset, yOffset, zOffset);
        pose.scale(scale * 0.5f, scale * 0.5f, scale * 0.5f);
        var resultMatrix = pose.last().pose();

        // -Z
        consumer.vertex(resultMatrix, -1, -1, -1).endVertex();
        consumer.vertex(resultMatrix, -1, 1, -1).endVertex();
        consumer.vertex(resultMatrix, 1, 1, -1).endVertex();
        consumer.vertex(resultMatrix, 1, -1, -1).endVertex();

        // +Z
        consumer.vertex(resultMatrix, -1, -1, 1).endVertex();
        consumer.vertex(resultMatrix, 1, -1, 1).endVertex();
        consumer.vertex(resultMatrix, 1, 1, 1).endVertex();
        consumer.vertex(resultMatrix, -1, 1, 1).endVertex();

        // -Y
        consumer.vertex(resultMatrix, -1, -1, -1).endVertex();
        consumer.vertex(resultMatrix, 1, -1, -1).endVertex();
        consumer.vertex(resultMatrix, 1, -1, 1).endVertex();
        consumer.vertex(resultMatrix, -1, -1, 1).endVertex();

        // +Y
        consumer.vertex(resultMatrix, -1, 1, -1).endVertex();
        consumer.vertex(resultMatrix, -1, 1, 1).endVertex();
        consumer.vertex(resultMatrix, 1, 1, 1).endVertex();
        consumer.vertex(resultMatrix, 1, 1, -1).endVertex();

        // -X
        consumer.vertex(resultMatrix, -1, -1, -1).endVertex();
        consumer.vertex(resultMatrix, -1, -1, 1).endVertex();
        consumer.vertex(resultMatrix, -1, 1, 1).endVertex();
        consumer.vertex(resultMatrix, -1, 1, -1).endVertex();

        // +X
        consumer.vertex(resultMatrix, 1, -1, -1).endVertex();
        consumer.vertex(resultMatrix, 1, 1, -1).endVertex();
        consumer.vertex(resultMatrix, 1, 1, 1).endVertex();
        consumer.vertex(resultMatrix, 1, -1, 1).endVertex();

        pose.popPose();
    }

    public static long getTicksSinceSearch() {
        return ticksSinceSearch;
    }

    public static void incrementTicksSinceSearch() {
        ticksSinceSearch++;
    }

    public static void resetSearchTime() {
        ticksSinceSearch = 0;
    }
}
