package red.jackf.randomadditions.mixin;

import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.randomadditions.RandomAdditionsClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Shadow
    private static void drawShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {

    }


    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Shadow private ClientWorld world;

    @Shadow @Final private MinecraftClient client;

    @Inject(method= "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V", at=@At("RETURN"))
    public void renderFoundItemOverlay(MatrixStack matrices,
                                       float tickDelta,
                                       long limitTime,
                                       boolean renderBlockOutline,
                                       Camera camera,
                                       GameRenderer gameRenderer,
                                       LightmapTextureManager lightmapTextureManager,
                                       Matrix4f matrix4f,
                                       CallbackInfo ci) {
        this.world.getProfiler().swap("rc_founditems");
        Vec3d cameraPos = camera.getPos();
        List<RandomAdditionsClient.FoundItemPos> toRemove = new ArrayList<>();
        Iterator<RandomAdditionsClient.FoundItemPos> iter = RandomAdditionsClient.FOUND_ITEM_POSITIONS.iterator();
        while (iter.hasNext()) {
            RandomAdditionsClient.FoundItemPos pos = iter.next();
            drawShapeOutline(matrices,
                    this.bufferBuilders.getEntityVertexConsumers().getBuffer(RenderLayer.getLines()),
                    this.world.getBlockState(pos.pos).getOutlineShape(this.world, pos.pos, ShapeContext.of(this.client.player)),
                    pos.pos.getX() - cameraPos.x,
                    pos.pos.getY() - cameraPos.y,
                    pos.pos.getZ() - cameraPos.z,
                    0.0f,
                    1.0f,
                    0.0f,
                    0.4f
                    );
            if (this.world.getTime() - pos.time > RandomAdditionsClient.FOUND_ITEMS_LIFESPAN) {
                toRemove.add(pos);
            }
        }
        for (RandomAdditionsClient.FoundItemPos pos : toRemove)
            RandomAdditionsClient.FOUND_ITEM_POSITIONS.remove(pos);
    }
}
