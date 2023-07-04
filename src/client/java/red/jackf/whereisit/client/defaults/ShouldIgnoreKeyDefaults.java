package red.jackf.whereisit.client.defaults;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import red.jackf.whereisit.client.api.ShouldIgnoreKey;

public class ShouldIgnoreKeyDefaults {
    public static void setup() {
        // Creative mode search bar and anvil bar
        ShouldIgnoreKey.EVENT.register(() -> {
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
                return creativeScreen.searchBox.canConsumeInput();
            } else if (screen instanceof AnvilScreen anvilScreen) {
                return anvilScreen.name.canConsumeInput();
            } else if (screen instanceof RecipeUpdateListener listener) {
                var recipeBook = listener.getRecipeBookComponent();
                return recipeBook.searchBox != null && recipeBook.searchBox.canConsumeInput();
            }
            return false;
        });
    }
}
