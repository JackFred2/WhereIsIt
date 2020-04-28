package red.jackf.whereisit.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.whereisit.compat.REIHandler;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.WhereIsItClient;

// Where REI/non-HandledScreen features are processed

@Environment(EnvType.CLIENT)
@Mixin(Screen.class)
public abstract class MixinScreen {
    @Inject(method= "keyPressed", at=@At("TAIL"))
    private void handleModdedKeys(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
         if (WhereIsItClient.FIND_ITEMS.matchesKey(keyCode, scanCode)) {
             if (WhereIsIt.REILoaded) {
                 double gameScale = (double) MinecraftClient.getInstance().getWindow().getScaledWidth() / (double) MinecraftClient.getInstance().getWindow().getWidth();
                 double mouseX = MinecraftClient.getInstance().mouse.getX() * gameScale;
                 double mouseY = MinecraftClient.getInstance().mouse.getY() * gameScale;

                 Item itemToFind = REIHandler.findREIItems(mouseX, mouseY);

                 if (itemToFind != null) {
                     WhereIsItClient.sendItemFindPacket(itemToFind);
                     //cir.setReturnValue(true);
                 }
             }
         }
    }
}
