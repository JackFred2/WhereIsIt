package red.jackf.whereisit.compat;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.impl.client.gui.screen.AbstractDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.screen.CompositeDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.screen.DefaultDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.item.ItemStack;
import red.jackf.whereisit.WhereIsIt;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
public class REIHandler {
    private REIHandler() {
    }

    public static ItemStack findREIItems(double mouseX, double mouseY) {
        Optional<ScreenOverlay> overlayOptional = REIRuntime.getInstance().getOverlay();

        try {
            if (overlayOptional.isPresent()) {
                ScreenOverlay overlay = overlayOptional.get();

                // Big List
                EntryStack<?> mainListFocused = overlay.getEntryList().getFocusedStack();
                if (!mainListFocused.isEmpty() && mainListFocused.getValueType() == ItemStack.class)
                    return (ItemStack) mainListFocused.getValue();

                // Favourites
                if (overlay.getFavoritesList().isPresent()) {
                    EntryStack<?> favouritesFocused = overlay.getFavoritesList().get().getFocusedStack();
                    if (!favouritesFocused.isEmpty() && favouritesFocused.getValueType() == ItemStack.class)
                        return (ItemStack) favouritesFocused.getValue();
                }

                var client = MinecraftClient.getInstance();

                if (client.currentScreen instanceof AbstractDisplayViewingScreen) {
                    var hoveredEntryWidget = client.currentScreen.children().stream()
                        .filter(element -> element instanceof EntryWidget)
                        .filter(element -> ((EntryWidget) element).containsMouse(mouseX, mouseY))
                        .findFirst();
                    if (hoveredEntryWidget.isPresent()) {
                        var entries = ((EntryWidget) hoveredEntryWidget.get()).getEntries();
                        if (entries.size() > 0 && entries.get(0).getType() == VanillaEntryTypes.ITEM) return entries.get(0).castValue();
                    }
                }

            }
        } catch (Exception ex) {
            WhereIsIt.error("Error searching REI for item to search: ");
            WhereIsIt.error(ex);
        }
        return null;
    }
}
