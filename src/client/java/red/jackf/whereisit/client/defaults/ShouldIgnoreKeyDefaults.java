package red.jackf.whereisit.client.defaults;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import red.jackf.whereisit.client.api.events.ShouldIgnoreKey;

public class ShouldIgnoreKeyDefaults {
    static void setup() {
        // Creative mode search bar and anvil bar
        ShouldIgnoreKey.EVENT.register(() -> {
            var screen = Minecraft.getInstance().screen;
            if (screen == null) return false;
            if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
                return creativeScreen.searchBox.canConsumeInput();
            } else if (screen instanceof AbstractRecipeBookScreen<?> recipeBookScreen) {
                var recipeBook = recipeBookScreen.recipeBookComponent;
                return recipeBook.searchBox != null && recipeBook.searchBox.canConsumeInput();
            } else if (screen.getFocused() instanceof EditBox editBox) {
                return editBox.canConsumeInput();
            }
            return false;
        });
    }
}
