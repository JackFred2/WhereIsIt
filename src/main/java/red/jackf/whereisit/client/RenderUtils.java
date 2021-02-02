package red.jackf.whereisit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import red.jackf.whereisit.FoundType;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.WhereIsItClient;

import java.util.ArrayList;
import java.util.List;

import static red.jackf.whereisit.WhereIsItClient.optimizedDrawShapeOutline;

public abstract class RenderUtils {
    public static final RenderPhase.Transparency RENDER_TRANSPARENCY = new RenderPhase.Transparency("wii_translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);
    private static final List<WhereIsItClient.FoundItemPos> outlinesToRemove = new ArrayList<>();
    private static final RenderLayer RENDER_LAYER = RenderLayer.of("wii_blockoutline",
        VertexFormats.POSITION_COLOR,
        1, 256,
        RenderLayer.MultiPhaseParameters.builder()
            .lineWidth(DynamicLineWidth.get())
            .depthTest(new RenderPhase.DepthTest("pass", 519))
            .transparency(RenderUtils.RENDER_TRANSPARENCY)
            .build(false)
    );

    public static void renderOutlines(MatrixStack matrices, VertexConsumerProvider.Immediate vertices, Camera camera, long startTime) {
        Vec3d cameraPos = camera.getPos();
        RenderSystem.disableDepthTest();
        matrices.push();

        float r = ((WhereIsIt.CONFIG.getColour() >> 16) & 0xff) / 255f;
        float g = ((WhereIsIt.CONFIG.getColour() >> 8) & 0xff) / 255f;
        float b = ((WhereIsIt.CONFIG.getColour()) & 0xff) / 255f;

        float rAlt = ((WhereIsIt.CONFIG.getAlternateColour() >> 16) & 0xff) / 255f;
        float gAlt = ((WhereIsIt.CONFIG.getAlternateColour() >> 8) & 0xff) / 255f;
        float bAlt = ((WhereIsIt.CONFIG.getAlternateColour()) & 0xff) / 255f;

        for (
            WhereIsItClient.FoundItemPos foundPos : WhereIsItClient.FOUND_ITEM_POSITIONS) {
            long timeDiff = startTime - foundPos.time;
            if (foundPos.type == FoundType.FOUND_DEEP) {
                optimizedDrawShapeOutline(matrices,
                    vertices.getBuffer(RENDER_LAYER),
                    foundPos.shape,
                    foundPos.pos.getX() - cameraPos.x,
                    foundPos.pos.getY() - cameraPos.y,
                    foundPos.pos.getZ() - cameraPos.z,
                    rAlt,
                    gAlt,
                    bAlt,
                    (WhereIsIt.CONFIG.getFadeoutTime() - timeDiff) / (float) WhereIsIt.CONFIG.getFadeoutTime()
                );
            } else {
                optimizedDrawShapeOutline(matrices,
                    vertices.getBuffer(RENDER_LAYER),
                    foundPos.shape,
                    foundPos.pos.getX() - cameraPos.x,
                    foundPos.pos.getY() - cameraPos.y,
                    foundPos.pos.getZ() - cameraPos.z,
                    r,
                    g,
                    b,
                    (WhereIsIt.CONFIG.getFadeoutTime() - timeDiff) / (float) WhereIsIt.CONFIG.getFadeoutTime()
                );
            }
            if (timeDiff >= WhereIsIt.CONFIG.getFadeoutTime()) {
                outlinesToRemove.add(foundPos);
            }
        }

        vertices.draw(RENDER_LAYER);
        matrices.pop();
        RenderSystem.enableDepthTest();

        for (WhereIsItClient.FoundItemPos pos : outlinesToRemove)
            WhereIsItClient.FOUND_ITEM_POSITIONS.remove(pos);

        outlinesToRemove.clear();
    }
}
