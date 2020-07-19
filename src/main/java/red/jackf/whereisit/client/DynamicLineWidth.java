package red.jackf.whereisit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderPhase;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.mixin.AccessorRenderPhase;

import java.util.OptionalDouble;

@SuppressWarnings("ConstantConditions")
@Environment(EnvType.CLIENT)
public abstract class DynamicLineWidth {
    public static RenderPhase.LineWidth get() {
        RenderPhase.LineWidth layer = new RenderPhase.LineWidth(OptionalDouble.empty());
        ((AccessorRenderPhase) layer).setName("line_width_dynamic");
        ((AccessorRenderPhase) layer).setBeginAction(() -> RenderSystem.lineWidth(WhereIsIt.CONFIG.getLineWidth()));
        ((AccessorRenderPhase) layer).setEndAction(() -> RenderSystem.lineWidth(1.0f));
        return layer;
    }
}
