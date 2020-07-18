package red.jackf.whereisit.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
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
    protected abstract Slot wii_getFocusedSlot();

    @Inject(method = "keyPressed", at = @At("TAIL"))
    private void handleModdedKeys(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (WhereIsItClient.FIND_ITEMS.matchesKey(keyCode, scanCode)) {
            if (wii_getFocusedSlot() != null && wii_getFocusedSlot().hasStack()) {
                WhereIsItClient.sendItemFindPacket(wii_getFocusedSlot().getStack().getItem(), Screen.hasShiftDown(), wii_getFocusedSlot().getStack().getTag());
                //cir.setReturnValue(true);
            }
        }
    }
}
