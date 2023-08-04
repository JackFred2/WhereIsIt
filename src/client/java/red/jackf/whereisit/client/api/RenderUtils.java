package red.jackf.whereisit.client.api;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import red.jackf.whereisit.client.render.WorldRendering;

import java.util.Set;

/**
 * Utilities for working with Where Is It's rendering
 */
@SuppressWarnings("unused")
public interface RenderUtils {
    /**
     * @return Get all positions currently being rendered by Where Is It.
     */
    static Set<BlockPos> getCurrentlyRendered() {
        return WorldRendering.getResults().keySet();
    }

    /**
     * @return Get all positions currently being rendered by Where Is It that specifically have a label. Used by Chest
     * Tracker to skip label rendering when results are shown for the same position.
     */
    static Set<BlockPos> getCurrentlyRenderedWithNames() {
        return WorldRendering.getNamedResults().keySet();
    }

    /**
     * Schedule a label to be rendered here on the next frame. Register here in order to have correct layering for labels.
     * This should be called every frame before {@link net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents#BEFORE_BLOCK_OUTLINE}.
     * @param pos Position to render the label at
     * @param name Label to render at said position
     */
    static void scheduleLabelRender(Vec3 pos, Component name) {
        WorldRendering.scheduleLabel(pos, name);
    }
}
