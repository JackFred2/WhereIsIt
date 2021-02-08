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
import red.jackf.whereisit.FoundType;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.WhereIsItClient;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class RenderUtils {
    private static final List<WhereIsItClient.FoundItemPos> toRemove = new ArrayList<>();

    public static void renderOutlines(WorldRenderContext context) {
        Vec3d cameraPos = context.camera().getPos();

        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.0f);
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.pushMatrix();
        RenderSystem.lineWidth(WhereIsIt.CONFIG.getLineWidth());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float r = ((WhereIsIt.CONFIG.getColour() >> 16) & 0xff) / 255f;
        float g = ((WhereIsIt.CONFIG.getColour() >> 8) & 0xff) / 255f;
        float b = ((WhereIsIt.CONFIG.getColour()) & 0xff) / 255f;

        float rAlt = ((WhereIsIt.CONFIG.getAlternateColour() >> 16) & 0xff) / 255f;
        float gAlt = ((WhereIsIt.CONFIG.getAlternateColour() >> 8) & 0xff) / 255f;
        float bAlt = ((WhereIsIt.CONFIG.getAlternateColour()) & 0xff) / 255f;

        for (WhereIsItClient.FoundItemPos pos : WhereIsItClient.FOUND_ITEM_POSITIONS) {
            long timeDiff = context.world().getTime() - pos.time;
            float a = (WhereIsIt.CONFIG.getFadeoutTime() - timeDiff) / (float) WhereIsIt.CONFIG.getFadeoutTime();

            if (pos.type == FoundType.FOUND) GlStateManager.color4f(r, g, b, a);
            else GlStateManager.color4f(rAlt, gAlt, bAlt, a);

            drawShape(tessellator, buffer, pos.shape,
                pos.pos.getX() - cameraPos.x,
                pos.pos.getY() - cameraPos.y,
                pos.pos.getZ() - cameraPos.z);

            RenderSystem.disableDepthTest();

            if (pos.type == FoundType.FOUND) GlStateManager.color4f(r, g, b, a * 0.5f);
            else GlStateManager.color4f(rAlt, gAlt, bAlt, a * 0.5f);

            drawShape(tessellator, buffer, pos.shape,
                pos.pos.getX() - cameraPos.x,
                pos.pos.getY() - cameraPos.y,
                pos.pos.getZ() - cameraPos.z);

            RenderSystem.enableDepthTest();

            if (timeDiff >= WhereIsIt.CONFIG.getFadeoutTime()) {
                toRemove.add(pos);
            }
        }

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.popMatrix();

        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);

        for (WhereIsItClient.FoundItemPos pos : toRemove)
            WhereIsItClient.FOUND_ITEM_POSITIONS.remove(pos);
    }

    private static void drawShape(Tessellator tessellator, BufferBuilder buffer, VoxelShape shape, double x, double y, double z) {
        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION);
        List<Box> edges = WhereIsItClient.CACHED_SHAPES.get(shape);
        if (edges == null) {
            //WhereIsIt.log("Adding new cached shape");
            List<Box> edgesList = new LinkedList<>();
            shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> edgesList.add(new Box(x1, y1, z1, x2, y2, z2)));
            edges = edgesList;
            WhereIsItClient.CACHED_SHAPES.put(shape, edgesList);
        }

        for (Box box : edges) {
            buffer.vertex(box.minX + x, box.minY + y, box.minZ + z).next();
            buffer.vertex(box.maxX + x, box.maxY + y, box.maxZ + z).next();
        }

        tessellator.draw();
    }
}
