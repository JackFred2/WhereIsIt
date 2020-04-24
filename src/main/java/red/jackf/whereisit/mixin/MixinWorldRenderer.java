package red.jackf.whereisit.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ShapeContext;
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
import red.jackf.whereisit.WhereIsItClient;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static red.jackf.whereisit.WhereIsItClient.FOUND_ITEMS_LIFESPAN;
import static red.jackf.whereisit.WhereIsItClient.optimizedDrawShapeOutline;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    private final List<WhereIsItClient.FoundItemPos> wii_outlinesToRemove = new ArrayList<>();

    private static final RenderPhase.Transparency WII_Transparency = new RenderPhase.Transparency("wii_translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);

    private static final RenderLayer WII_RenderLayer = RenderLayer.of("wii_blockoutline",
            VertexFormats.POSITION_COLOR,
            1, 256,
            RenderLayer.MultiPhaseParameters.builder()
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
                    .depthTest(new RenderPhase.DepthTest("pass", 519))
                    .transparency(WII_Transparency)
                    .build(false)
            );

    //@Shadow
    //private static void drawShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {}


    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Shadow private ClientWorld world;

    @Inject(method= "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",
            at=@At(value = "TAIL"))
    public void renderFoundItemOverlay(MatrixStack matrices,
                                       float tickDelta,
                                       long limitTime,
                                       boolean renderBlockOutline,
                                       Camera camera,
                                       GameRenderer gameRenderer,
                                       LightmapTextureManager lightmapTextureManager,
                                       Matrix4f matrix4f,
                                       CallbackInfo ci) {
        this.world.getProfiler().swap("wii_founditems");
        Vec3d cameraPos = camera.getPos();
        VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();
        RenderSystem.disableDepthTest();
        matrices.push();

        for (WhereIsItClient.FoundItemPos foundPos : WhereIsItClient.FOUND_ITEM_POSITIONS) {
            long timeDiff = this.world.getTime() - foundPos.time;
            /*drawShapeOutline(matrices,
                    immediate.getBuffer(WII_RenderLayer),
                    //VoxelShapes.fullCube(),
                    foundPos.shape,
                    foundPos.pos.getX() - cameraPos.x,
                    foundPos.pos.getY() - cameraPos.y,
                    foundPos.pos.getZ() - cameraPos.z,
                    0.0f,
                    1.0f,
                    0.0f,
                    (FOUND_ITEMS_LIFESPAN - timeDiff) / (float) FOUND_ITEMS_LIFESPAN
            );*/
            optimizedDrawShapeOutline(matrices,
                    immediate.getBuffer(WII_RenderLayer),
                    foundPos.shape,
                    foundPos.pos.getX() - cameraPos.x,
                    foundPos.pos.getY() - cameraPos.y,
                    foundPos.pos.getZ() - cameraPos.z,
                    0.0f,
                    1.0f,
                    0.0f,
                    (FOUND_ITEMS_LIFESPAN - timeDiff) / (float) FOUND_ITEMS_LIFESPAN
            );
            if (timeDiff >= FOUND_ITEMS_LIFESPAN) {
                wii_outlinesToRemove.add(foundPos);
            }
        }

        immediate.draw(WII_RenderLayer);
        matrices.pop();
        RenderSystem.enableDepthTest();

        for (WhereIsItClient.FoundItemPos pos : wii_outlinesToRemove)
            WhereIsItClient.FOUND_ITEM_POSITIONS.remove(pos);

        wii_outlinesToRemove.clear();
    }
}
