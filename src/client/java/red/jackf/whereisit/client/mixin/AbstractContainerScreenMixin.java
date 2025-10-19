package red.jackf.whereisit.client.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.whereisit.client.render.Rendering;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Inject(
            method = "renderContents(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlightBack(Lnet/minecraft/client/gui/GuiGraphics;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterRenderSlotHighlightBack(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Rendering.renderSlotHighlight((AbstractContainerScreen<?>) (Object) this, guiGraphics, mouseX, mouseY, partialTick);
    }
}
