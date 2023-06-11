package red.jackf.whereisit.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DefaultGrabber {
    @Nullable
    public static ItemStack grab(Screen screen, int mouseX, int mouseY) {
        // normal item
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            if (containerScreen.hoveredSlot != null) {
                var stack = containerScreen.hoveredSlot.getItem();
                if (!stack.isEmpty()) return stack;
            }

            if (containerScreen instanceof RecipeUpdateListener recipeBookHolder) {
                var book = recipeBookHolder.getRecipeBookComponent();
                if (book.ghostRecipe.getRecipe() != null) {
                    for (var i = 0; i < book.ghostRecipe.size(); i++) {
                        var ingredient = book.ghostRecipe.get(i);
                        var x = ingredient.getX() + containerScreen.leftPos;
                        var y = ingredient.getY() + containerScreen.topPos;
                        if (x <= mouseX && mouseX < x + 16 && y <= mouseY && mouseY < y + 16) {
                            // TODO: support tags
                            return ingredient.getItem();
                        }
                    }
                }
            }
        }
        return null;
    }
}
