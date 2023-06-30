package red.jackf.whereisit.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;
import red.jackf.whereisit.api.SearchResult;

import java.util.Collection;
import java.util.Collections;

public class ResultRenderer {
    // had a nice shader going but alas, https://github.com/IrisShaders/Iris/blob/1.19.4/docs/development/compatibility/core-shaders.md
    // they're right btw don't put this on iris
    @SuppressWarnings("unused")
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
                    .createCompositeState(false));

    private static Collection<SearchResult> results = Collections.emptyList();
    // if -1, update next refresh
    private static long lastSetTime = 0;
    private static final long LIFESPAN = 10 * SharedConstants.TICKS_PER_SECOND;

    private static float progress = 1f;

    @SuppressWarnings("resource")
    public static void setup() {
        WorldRenderEvents.END.register(context -> {
            if (results.isEmpty()) return;
            if (lastSetTime == -1) lastSetTime = context.world().getGameTime();

            progress = (context.world().getGameTime() + context.tickDelta() - lastSetTime) / LIFESPAN;

            if (context.world() == null || progress > 1f) {
                results = Collections.emptyList();
                return;
            }

            renderLocations(context);
        });
    }

    private static void renderLocations(WorldRenderContext context) {
        var camera = context.camera();

        var pose = new PoseStack();
        pose.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        pose.mulPose(Axis.YP.rotationDegrees(camera.getYRot() - 180f));
        var projected = camera.getPosition();
        pose.translate(-projected.x, -projected.y, -projected.z);

        // from 100% to 50%
        var alpha = 1 - (progress / 2f);
        var colour = Mth.hsvToRgb(progress, 1f, 1f);
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

        results.forEach(result -> renderIndividual(
                result,
                builder,
                pose,
                scale
        ));

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
