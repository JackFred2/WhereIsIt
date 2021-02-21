package red.jackf.whereisit.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.lwjgl.opengl.GL11;
import red.jackf.whereisit.WhereIsIt;

import java.util.*;

public abstract class RenderUtils {
    public static final Map<VoxelShape, List<Box>> CACHED_SHAPES = new HashMap<>();
    public static final List<FoundItemPos> FOUND_ITEM_POSITIONS = new ArrayList<>();
    private static final List<FoundItemPos> toRemove = new ArrayList<>();

    public static void renderOutlines(WorldRenderContext context, Boolean simpleRendering) {
        context.world().getProfiler().swap("whereisit");
        Vec3d cameraPos = context.camera().getPos();

        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.0f);
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.pushMatrix();
        RenderSystem.lineWidth(WhereIsIt.CONFIG.getLineWidth());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (FoundItemPos pos : FOUND_ITEM_POSITIONS) {
            long timeDiff = context.world().getTime() - pos.time;
            float a = (WhereIsIt.CONFIG.getFadeoutTime() - timeDiff) / (float) WhereIsIt.CONFIG.getFadeoutTime();

            // Bright boxes, in front of terrain but blocked by it
            if (!simpleRendering) {
                RenderSystem.enableDepthTest();

                GlStateManager.color4f(pos.r, pos.g, pos.b, a);

                drawShape(tessellator, buffer, pos.shape,
                    pos.pos.getX() - cameraPos.x,
                    pos.pos.getY() - cameraPos.y,
                    pos.pos.getZ() - cameraPos.z);

                // Translucent boxes, behind terrain but always visible

                RenderSystem.disableDepthTest();
            }

            float forcedAlpha = simpleRendering ? a : a * 0.5f;

            GlStateManager.color4f(pos.r, pos.g, pos.b, forcedAlpha);

            drawShape(tessellator, buffer, pos.shape,
                pos.pos.getX() - cameraPos.x,
                pos.pos.getY() - cameraPos.y,
                pos.pos.getZ() - cameraPos.z);

            if (timeDiff >= WhereIsIt.CONFIG.getFadeoutTime()) {
                toRemove.add(pos);
            }
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.popMatrix();

        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);

        for (FoundItemPos pos : toRemove)
            FOUND_ITEM_POSITIONS.remove(pos);
    }

    private static void drawShape(Tessellator tessellator, BufferBuilder buffer, VoxelShape shape, double x, double y, double z) {
        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION);
        List<Box> edges = CACHED_SHAPES.get(shape);
        if (edges == null) {
            //WhereIsIt.log("Adding new cached shape");
            List<Box> edgesList = new LinkedList<>();
            shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> edgesList.add(new Box(x1, y1, z1, x2, y2, z2)));
            edges = edgesList;
            CACHED_SHAPES.put(shape, edgesList);
        }

        for (Box box : edges) {
            buffer.vertex(box.minX + x, box.minY + y, box.minZ + z).next();
            buffer.vertex(box.maxX + x, box.maxY + y, box.maxZ + z).next();
        }

        tessellator.draw();
    }
}
