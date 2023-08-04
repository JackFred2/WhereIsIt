package red.jackf.whereisit.client.api;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import red.jackf.whereisit.client.render.WorldRendering;

import java.util.Set;

@SuppressWarnings("unused")
public interface RenderUtils {
    static Set<BlockPos> getCurrentlyRendered() {
        return WorldRendering.getResults().keySet();
    }

    static Set<BlockPos> getCurrentlyRenderedWithNames() {
        return WorldRendering.getNamedResults().keySet();
    }

    static void renderName(Vec3 pos, Component name, WorldRenderContext context) {

    }
}
