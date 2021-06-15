package red.jackf.whereisit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.lwjgl.opengl.GL11;
import red.jackf.whereisit.Searcher;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.WhereIsItClient;
import red.jackf.whereisit.mixin.AccessorDrawableHelper;
import red.jackf.whereisit.mixin.AccessorHandledScreen;

import java.util.*;

@Environment(EnvType.CLIENT)
public abstract class RenderUtils {
    public static final Map<BlockPos, PositionData> FOUND_ITEM_POSITIONS = new HashMap<>();
    private static final List<BlockPos> toRemove = new ArrayList<>();

    public static final Event<RenderLocation> RENDER_LOCATION_EVENT = EventFactory.createArrayBacked(RenderLocation.class, (context, simpleRendering, positionData) -> {}, callbacks -> (context, simpleRendering, positionData) -> {
        for (final RenderLocation callback : callbacks) {
            callback.renderLocation(context, simpleRendering, positionData);
        }
    });

    public static void renderOutlines(WorldRenderContext context, Boolean simpleRendering) {
        if (FOUND_ITEM_POSITIONS.size() == 0) return;
        context.world().getProfiler().swap("whereisit");
        
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

        if (FabricLoader.getInstance().isModLoaded("canvas")) { // canvas compat
            stack.multiply(new Quaternion(Vec3f.POSITIVE_X, camera.getPitch(), true));
            stack.multiply(new Quaternion(Vec3f.POSITIVE_Y, camera.getYaw() + 180f, true));
        }

        RenderSystem.applyModelViewMatrix();

        for (Map.Entry<BlockPos, PositionData> entry : FOUND_ITEM_POSITIONS.entrySet()) {
            PositionData positionData = entry.getValue();

            RENDER_LOCATION_EVENT.invoker().renderLocation(context, simpleRendering, positionData);
            
            long timeDiff = context.world().getTime() - positionData.time;
            float a = ((WhereIsIt.CONFIG.getFadeoutTime() - timeDiff) / (float) WhereIsIt.CONFIG.getFadeoutTime());

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

        if (FOUND_ITEM_POSITIONS.size() == 0) {
            WhereIsItClient.clearLastItem();
        }
    }

    private static void drawShape(BufferBuilder buffer, VoxelShape shape, double x, double y, double z, float r, float g, float b, float a) {
       shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {

           double lowX = x1 + x;
           double lowY = y1 + y;
           double lowZ = z1 + z;
           double highX = x2 + x;
           double highY = y2 + y;
           double highZ = z2 + z;

            //bottom / -y
            buffer.vertex(lowX , lowY , lowZ ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY , highZ).color(r, g, b, a).next();
            buffer.vertex(lowX , lowY , highZ).color(r, g, b, a).next();

            buffer.vertex(lowX , lowY , lowZ ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY , lowZ ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY , highZ).color(r, g, b, a).next();

            //top / +y
            buffer.vertex(lowX , highY, lowZ ).color(r, g, b, a).next();
            buffer.vertex(lowX , highY, highZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();

            buffer.vertex(lowX , highY, lowZ ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, lowZ ).color(r, g, b, a).next();

            //west / -x
            buffer.vertex(lowX , lowY , lowZ ).color(r, g, b, a).next();
            buffer.vertex(lowX , lowY , highZ).color(r, g, b, a).next();
            buffer.vertex(lowX , highY, highZ).color(r, g, b, a).next();

            buffer.vertex(lowX , lowY , lowZ ).color(r, g, b, a).next();
            buffer.vertex(lowX , highY, highZ).color(r, g, b, a).next();
            buffer.vertex(lowX , highY, lowZ ).color(r, g, b, a).next();

            //east / +x
            buffer.vertex(highX, lowY , lowZ ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY , highZ).color(r, g, b, a).next();

            buffer.vertex(highX, lowY , lowZ ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, lowZ ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();

            //west / -x
            buffer.vertex(lowX , lowY , highZ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY , highZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();

            buffer.vertex(lowX , lowY , highZ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, highZ).color(r, g, b, a).next();
            buffer.vertex(lowX , highY, highZ).color(r, g, b, a).next();

            //west / -x
            buffer.vertex(lowX , lowY , lowZ ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, lowZ ).color(r, g, b, a).next();
            buffer.vertex(highX, lowY , lowZ ).color(r, g, b, a).next();

            buffer.vertex(lowX , lowY , lowZ ).color(r, g, b, a).next();
            buffer.vertex(lowX , highY, lowZ ).color(r, g, b, a).next();
            buffer.vertex(highX, highY, lowZ ).color(r, g, b, a).next();
        });
    }

    public static void renderLastSlot(MatrixStack matrixStack, MinecraftClient minecraftClient, Screen screen, int mouseX, int mouseY, float tickDelta) {
        if (WhereIsIt.CONFIG.disableSlotHighlight()) return;
        if (screen instanceof HandledScreen<?> handledScreen) {
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

    @FunctionalInterface
    public interface RenderLocation {
        void renderLocation(WorldRenderContext context, Boolean simpleRendering, PositionData positionData);
    }

    // hue between 0 and 360
    public static Vec3f hueToColour(float hue) {
        hue = hue % 360;
        float factor = 1 - Math.abs(MathHelper.floorMod(hue / 60f, 2) - 1);

        switch ((int) (hue / 60)) {
            case 0 : return new Vec3f(1, factor,0);
            case 1 : return new Vec3f(factor, 1,0);
            case 2 : return new Vec3f(0,1, factor);
            case 3 : return new Vec3f(0,factor, 1);
            case 4 : return new Vec3f(factor,0, 1);
            case 5 : return new Vec3f(1,0, factor);
        }

        throw new RuntimeException("Exhausted switch statement?");
    }
}
