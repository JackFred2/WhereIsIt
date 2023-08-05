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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.client.WhereIsItClient;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.util.*;

@SuppressWarnings("resource") // i really don't want to call ClientLevel#close() thanks
public class WorldRendering {
    // had a nice shader going but alas, https://github.com/IrisShaders/Iris/blob/1.19.4/docs/development/compatibility/core-shaders.md
    // they're right btw don't put this on iris
    /*
    public static final RenderType BLOCK_HIGHLIGHT = RenderType.create("whereisit_block_highlight",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256 * 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.CULL)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));*/

    private static final Map<BlockPos, SearchResult> results = new HashMap<>();
    private static final Map<BlockPos, SearchResult> namedResults = new HashMap<>();

    private static final List<Pair<Vec3, Component>> scheduledLabels = new ArrayList<>();

    private static float progress = 1f;

    public static void setup() {
        // schedule highlight label renders
        WorldRenderEvents.START.register(context -> {
            if (!WhereIsItConfig.INSTANCE.getConfig().getClient().showContainerNamesInResults) return;
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

            progress = Mth.clamp((context.world()
                    .getGameTime() + context.tickDelta() - WhereIsItClient.lastSearchTime) / WhereIsItConfig.INSTANCE.getConfig()
                    .getClient().fadeoutTimeTicks, 0f, 1f);

            if (context.world() == null || progress > 1f) {
                return;
            }

            renderBoxes(context);
        });
    }

    public static void scheduleLabel(Vec3 pos, Component name) {
        scheduledLabels.add(Pair.of(pos, name));
    }

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

    public static void renderLabel(Vec3 pos, Component name, PoseStack pose, Camera camera, MultiBufferSource consumers) {
        pose.pushPose();

        pos = pos.subtract(camera.getPosition());

        pose.translate(pos.x, pos.y, pos.z);
        pose.mulPose(camera.rotation());
        var factor = 0.025f * WhereIsItConfig.INSTANCE.getConfig().getClient().containerNameLabelScale;
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

    private static void renderBoxes(WorldRenderContext context) {
        var camera = context.camera();

        var pose = new PoseStack();
        pose.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        pose.mulPose(Axis.YP.rotationDegrees(camera.getYRot() - 180f));
        var projected = camera.getPosition();
        pose.translate(-projected.x, -projected.y, -projected.z);

        // from 100% to 50%
        var alpha = 1 - (progress / 2f);
        var colour = WhereIsItClient.getColour(((context.world().getGameTime() + context.tickDelta()) % 80) / 80);
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
                    result.pos(),
                    builder,
                    pose,
                    scale
            );

            for (BlockPos otherPos : result.otherPositions()) {
                renderBox(
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

    private static void renderBox(BlockPos pos, VertexConsumer consumer, PoseStack pose, float scale) {
        pose.pushPose();
        pose.translate(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
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

    public static void clearResults() {
        results.clear();
        namedResults.clear();
    }

    public static Map<BlockPos, SearchResult> getResults() {
        return results;
    }

    public static Map<BlockPos, SearchResult> getNamedResults() {
        return namedResults;
    }
}
