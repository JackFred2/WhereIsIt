package red.jackf.whereisit.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;

public class RenderingObjects {
    @Nullable
    private static ShaderInstance BLOCK_HIGHLIGHT_SHADER;
    public static RenderStateShard.ShaderStateShard BLOCK_HIGHLIGHT_SHADER_SHARD = new RenderStateShard.ShaderStateShard(RenderingObjects::getBlockHighlightShader);

    // thank u Modular Routers https://github.com/desht/ModularRouters/blob/MC1.19.2-master/src/main/java/me/desht/modularrouters/client/render/ModRenderTypes.java#L42
    public static final RenderType BLOCK_HIGHLIGHT = RenderType.create("whereisit_block_highlight",
            DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256 * 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(BLOCK_HIGHLIGHT_SHADER_SHARD)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.CULL)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    private RenderingObjects() {}

    public static void setup() {
        CoreShaderRegistrationCallback.EVENT.register(context ->
            context.register(WhereIsIt.id("block_highlight"), DefaultVertexFormat.POSITION, shaderInstance ->
                BLOCK_HIGHLIGHT_SHADER = shaderInstance
            )
        );
    }

    public static ShaderInstance getBlockHighlightShader() {
        return BLOCK_HIGHLIGHT_SHADER;
    }
}
