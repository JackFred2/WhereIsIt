package red.jackf.whereisit.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import red.jackf.whereisit.api.SearchResult;

import java.util.Collection;
import java.util.Collections;

public class ResultRenderer {
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
        var alpha = (1.0f - (progress / 2f));
        var scale = easingFunc(progress);

        var consumer = buffer.getBuffer(RenderingObjects.BLOCK_HIGHLIGHT);

        results.forEach(result -> renderIndividual(
                result,
                consumer,
                pose,
                scale
        ));

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        buffer.endBatch(RenderingObjects.BLOCK_HIGHLIGHT);

        pose.popPose();

    }

    private static void renderIndividual(SearchResult searchResult, VertexConsumer consumer, PoseStack pose, float scale) {
        pose.pushPose();
        pose.translate(searchResult.pos().getX() + 0.5f, searchResult.pos().getY() + 0.5f, searchResult.pos()
                .getZ() + 0.5f);
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

        RenderSystem.disableDepthTest();

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
