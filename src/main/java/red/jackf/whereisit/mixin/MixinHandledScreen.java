package red.jackf.whereisit.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookGhostSlots;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.whereisit.WhereIsItClient;
import red.jackf.whereisit.client.RenderUtils;

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
                WhereIsItClient.searchForItem(whereisit$getFocusedSlot().getStack().getItem(), Screen.hasShiftDown(), whereisit$getFocusedSlot().getStack().getNbt());
                //cir.setReturnValue(true);
            } else if (this instanceof RecipeBookProvider provider) {
                int screenX = ((AccessorHandledScreen) this).whereisit$getX();
                int screenY = ((AccessorHandledScreen) this).whereisit$getY();

                double gameScale = (double) MinecraftClient.getInstance().getWindow().getScaledWidth() / (double) MinecraftClient.getInstance().getWindow().getWidth();
                double mouseX = MinecraftClient.getInstance().mouse.getX() * gameScale;
                double mouseY = MinecraftClient.getInstance().mouse.getY() * gameScale;

                var ghostSlots = ((AccessorRecipeBookWidget) provider.getRecipeBookWidget()).whereisit$getGhostSlots();
                for (int i = 0; i < ghostSlots.getSlotCount(); i++) {
                    RecipeBookGhostSlots.GhostInputSlot ghostInputSlot = ghostSlots.getSlot(i);
                    int slotLeft = ghostInputSlot.getX() + screenX;
                    int slotTop = ghostInputSlot.getY() + screenY;
                    if (mouseX > slotLeft && mouseY > slotTop && mouseX <= slotLeft + 16 && mouseY <= slotTop + 16) {
                        WhereIsItClient.searchForItem(ghostInputSlot.getCurrentItemStack().getItem(), Screen.hasShiftDown(), ghostInputSlot.getCurrentItemStack().getNbt());
                        break;
                    }
                }
            }
        }
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"))
    private void whereisit$drawSlotHighlights(MatrixStack stack, int x, int y, CallbackInfo ci) {
        RenderUtils.drawSlotWithLastSearchedItem(stack, (HandledScreen<?>) (Object) this);
    }
}
