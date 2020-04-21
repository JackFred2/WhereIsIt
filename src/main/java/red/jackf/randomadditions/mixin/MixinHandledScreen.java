package red.jackf.randomadditions.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.randomadditions.RandomAdditionsClient;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {

    @Shadow
    protected Slot focusedSlot;

    @Inject(method= "keyPressed", at=@At("TAIL"))
    private void handleModdedKeys(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (RandomAdditionsClient.FIND_ITEMS.matchesKey(keyCode, scanCode)) {
            if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
                // Try find
                System.out.println(this.focusedSlot.getStack().getItem());
            }
        }
    }
}
