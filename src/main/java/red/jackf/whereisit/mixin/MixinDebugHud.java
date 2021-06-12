package red.jackf.whereisit.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.whereisit.client.RenderUtils;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public class MixinDebugHud {
    @Inject(at = @At("RETURN"), method = "getLeftText")
    protected void whereisit$getLeftText(CallbackInfoReturnable<List<String>> info) {
        info.getReturnValue().add("[Where Is It] Rendering " + RenderUtils.FOUND_ITEM_POSITIONS.size());
    }
}
