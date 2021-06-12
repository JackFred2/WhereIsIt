package red.jackf.whereisit.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.whereisit.WhereIsItClient;

// Where HandledScreen features are present

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen {

    @Accessor(value = "focusedSlot")
    protected abstract Slot whereisit$getFocusedSlot();

    @Inject(method = "keyPressed", at = @At("TAIL"))
    private void whereisit$handleModdedKeys(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (WhereIsItClient.FIND_ITEMS.matchesKey(keyCode, scanCode)) {
            if (whereisit$getFocusedSlot() != null && whereisit$getFocusedSlot().hasStack()) {
                WhereIsItClient.searchForItem(whereisit$getFocusedSlot().getStack().getItem(), Screen.hasShiftDown(), whereisit$getFocusedSlot().getStack().getTag());
                //cir.setReturnValue(true);
            }
        }
    }
}
