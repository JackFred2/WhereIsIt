package red.jackf.whereisit.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.WhereIsItClient;
import red.jackf.whereisit.client.RenderUtils;
import red.jackf.whereisit.compat.REIHandler;

// Where REI/non-HandledScreen features are processed

@Environment(EnvType.CLIENT)
@Mixin(Screen.class)
public abstract class MixinScreen {
    @Inject(method = "keyPressed", at = @At("TAIL"))
    private void whereisit$handleModdedKeys(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (WhereIsItClient.FIND_ITEMS.matchesKey(keyCode, scanCode)) {
            if (WhereIsIt.REILoaded) {
                double gameScale = (double) MinecraftClient.getInstance().getWindow().getScaledWidth() / (double) MinecraftClient.getInstance().getWindow().getWidth();
                double mouseX = MinecraftClient.getInstance().mouse.getX() * gameScale;
                double mouseY = MinecraftClient.getInstance().mouse.getY() * gameScale;

                ItemStack itemToFind = REIHandler.findREIItems(mouseX, mouseY);

                if (itemToFind != null) {
                    WhereIsItClient.searchForItem(itemToFind.getItem(), Screen.hasShiftDown(), itemToFind.getNbt());
                    //cir.setReturnValue(true);
                } else {
                    RenderUtils.clearSearch();
                }
            }
        }
    }
}
