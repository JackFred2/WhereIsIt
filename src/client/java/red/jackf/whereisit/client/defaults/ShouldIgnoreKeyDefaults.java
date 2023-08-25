package red.jackf.whereisit.client.defaults;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import red.jackf.whereisit.client.api.ShouldIgnoreKey;

public class ShouldIgnoreKeyDefaults {
    static void setup() {
        // Creative mode search bar and anvil bar
        ShouldIgnoreKey.EVENT.register(() -> {
            var screen = Minecraft.getInstance().screen;
            if (screen == null) return false;
            if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
                return creativeScreen.searchBox.canConsumeInput();
            } else if (screen instanceof RecipeUpdateListener listener) {
                var recipeBook = listener.getRecipeBookComponent();
                return recipeBook.searchBox != null && recipeBook.searchBox.canConsumeInput();
            } else if (screen.getFocused() instanceof EditBox editBox) {
                return editBox.canConsumeInput();
            }
            return false;
        });
    }
}
