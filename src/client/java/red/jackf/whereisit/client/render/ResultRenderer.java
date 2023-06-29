package red.jackf.whereisit.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import red.jackf.whereisit.api.SearchResult;

import java.util.Collection;
import java.util.Collections;

public class ResultRenderer {
    // thank u Modular Routers https://github.com/desht/ModularRouters/blob/MC1.19.2-master/src/main/java/me/desht/modularrouters/client/render/ModRenderTypes.java#L42
    public static final RenderType BLOCK_HIGHLIGHT = RenderType.create("whereisit_block_highlight",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256 * 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.CULL)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    private static Collection<SearchResult> results = Collections.emptyList();
    // if -1, update next refresh
    private static long lastSetTime = 0;
    private static final long LIFESPAN = 10 * SharedConstants.TICKS_PER_SECOND;

    private static float progress = 1f;

    @SuppressWarnings("resource")
    public static void setup() {
        WorldRenderEvents.LAST.register(context -> {
            if (results.isEmpty()) return;
            if (lastSetTime == -1) lastSetTime = context.world().getGameTime();

            progress = (context.world().getGameTime() + context.tickDelta() - lastSetTime) / LIFESPAN;

            if (context.world() == null || progress > 1f) {
                results = Collections.emptyList();
                return;
            }

            renderLocations(context.matrixStack(), context.camera());
        });
    }

    private static void renderLocations(PoseStack pose, Camera camera) {
        var buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        pose.pushPose();
        var projected = camera.getPosition();
        pose.translate(-projected.x, -projected.y, -projected.z);

        // from 80% to 25%
        var alpha = (int) ((0.8 - (progress / 25f)) * 255);
        var colour = Mth.hsvToRgb(progress, 1f, 1f);
        var scale = easingFunc(progress);

        results.forEach(result -> renderIndividual(
                result,
                buffer,
                pose,
                FastColor.ARGB32.red(colour),
                FastColor.ARGB32.green(colour),
                FastColor.ARGB32.blue(colour),
                alpha,
                scale
        ));

        pose.popPose();

    }

    private static void renderIndividual(SearchResult searchResult, MultiBufferSource.BufferSource buffer, PoseStack pose, int r, int g, int b, int alpha, float scale) {
        pose.pushPose();
        pose.translate(searchResult.pos().getX() + 0.5f, searchResult.pos().getY() + 0.5f, searchResult.pos()
                .getZ() + 0.5f);
        pose.scale(scale * 0.5f, scale * 0.5f, scale * 0.5f);
        var resultMatrix = pose.last().pose();

        var consumer = buffer.getBuffer(BLOCK_HIGHLIGHT);

        // -Z
        consumer.vertex(resultMatrix, -1, -1, -1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, -1, 1, -1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, 1, 1, -1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, 1, -1, -1).color(r, g, b, alpha).endVertex();

        // +Z
        consumer.vertex(resultMatrix, -1, -1, 1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, 1, -1, 1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, 1, 1, 1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, -1, 1, 1).color(r, g, b, alpha).endVertex();

        // -Y
        consumer.vertex(resultMatrix, -1, -1, -1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, 1, -1, -1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, 1, -1, 1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, -1, -1, 1).color(r, g, b, alpha).endVertex();

        // +Y
        consumer.vertex(resultMatrix, -1, 1, -1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, -1, 1, 1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, 1, 1, 1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, 1, 1, -1).color(r, g, b, alpha).endVertex();

        // -X
        consumer.vertex(resultMatrix, -1, -1, -1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, -1, -1, 1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, -1, 1, 1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, -1, 1, -1).color(r, g, b, alpha).endVertex();

        // +X
        consumer.vertex(resultMatrix, 1, -1, -1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, 1, 1, -1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, 1, 1, 1).color(r, g, b, alpha).endVertex();
        consumer.vertex(resultMatrix, 1, -1, 1).color(r, g, b, alpha).endVertex();

        RenderSystem.disableDepthTest();

        buffer.endBatch(BLOCK_HIGHLIGHT);

        pose.popPose();
    }

    private static float easingFunc(float progress) {
        progress = Mth.clamp(progress, 0, 1);
        var power = 32f;
        return (float) ((1 - Math.pow(progress, power)) * (1 - Math.pow(1 - progress, power)) * (1 - (progress / 4f)));
    }

    public static void setResults(Collection<SearchResult> newResults) {
        results = newResults;
        lastSetTime = -1;
    }
}
