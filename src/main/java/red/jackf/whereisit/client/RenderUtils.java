package red.jackf.whereisit.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.lwjgl.opengl.GL11;
import red.jackf.whereisit.Searcher;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.WhereIsItClient;
import red.jackf.whereisit.mixin.AccessorDrawableHelper;
import red.jackf.whereisit.mixin.AccessorHandledScreen;
import red.jackf.whereisit.mixin.MixinHandledScreen;

import java.util.*;

public abstract class RenderUtils {
    public static final Map<VoxelShape, List<Box>> CACHED_SHAPES = new HashMap<>();
    public static final Map<BlockPos, FoundItemPos> FOUND_ITEM_POSITIONS = new HashMap<>();
    private static final List<BlockPos> toRemove = new ArrayList<>();

    public static void renderOutlines(WorldRenderContext context, Boolean simpleRendering) {
        if (FOUND_ITEM_POSITIONS.size() == 0) return;
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

        for (Map.Entry<BlockPos, FoundItemPos> entry : FOUND_ITEM_POSITIONS.entrySet()) {
            FoundItemPos positionData = entry.getValue();
            long timeDiff = context.world().getTime() - positionData.time;
            float a = (WhereIsIt.CONFIG.getFadeoutTime() - timeDiff) / (float) WhereIsIt.CONFIG.getFadeoutTime();

            Vec3d finalPos = cameraPos.subtract(positionData.pos.getX(), positionData.pos.getY(), positionData.pos.getZ()).negate();
            if (finalPos.lengthSquared() > 4096) { // if it's more than 64 blocks away, scale it so distant ones are still visible
                finalPos = finalPos.normalize().multiply(64);
            }

            // Bright boxes, in front of terrain but blocked by it
            if (!simpleRendering) {
                RenderSystem.enableDepthTest();

                GlStateManager.color4f(positionData.r, positionData.g, positionData.b, a);

                drawShape(tessellator, buffer, positionData.shape,
                    finalPos.x,
                    finalPos.y,
                    finalPos.z);

                // Translucent boxes, behind terrain but always visible

                RenderSystem.disableDepthTest();
            }

            float forcedAlpha = simpleRendering ? a : a * 0.5f;

            GlStateManager.color4f(positionData.r, positionData.g, positionData.b, forcedAlpha);

            drawShape(tessellator, buffer, positionData.shape,
                finalPos.x,
                finalPos.y,
                finalPos.z);

            if (timeDiff >= WhereIsIt.CONFIG.getFadeoutTime()) {
                toRemove.add(entry.getKey());
            }
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.popMatrix();

        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);

        Iterator<BlockPos> iter = toRemove.listIterator();
        while (iter.hasNext()) {
            FOUND_ITEM_POSITIONS.remove(iter.next());
            iter.remove();
        }

        if (FOUND_ITEM_POSITIONS.size() == 0) {
            WhereIsItClient.clearLastItem();
        }
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

    public static void renderLastSlot(MatrixStack matrixStack, MinecraftClient minecraftClient, Screen screen, int mouseX, int mouseY, float tickDelta) {
        if (WhereIsIt.CONFIG.disableSlotHighlight()) return;
        if (screen instanceof HandledScreen) {
            HandledScreen<?> handledScreen = (HandledScreen<?>) screen;
            handledScreen.getScreenHandler().slots.forEach(slot -> {
                ItemStack stack = slot.getStack();
                if (Searcher.areStacksEqual(stack.getItem(), stack.getTag(), WhereIsItClient.getLastSearchedItem(), WhereIsItClient.getLastSearchedTag(), WhereIsItClient.lastSearchIgnoreNbt())) {
                    int x = slot.x + ((AccessorHandledScreen) screen).getX();
                    int y = slot.y + ((AccessorHandledScreen) screen).getY();
                    final int colour = 0x80FFFF00;
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
    }
}
