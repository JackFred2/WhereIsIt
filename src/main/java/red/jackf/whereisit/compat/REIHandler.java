package red.jackf.whereisit.compat;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
public class REIHandler {
    private REIHandler() {
    }

    public static ItemStack findREIItems(double mouseX, double mouseY) {
        Optional<ScreenOverlay> overlayOptional = REIRuntime.getInstance().getOverlay();

        if (overlayOptional.isPresent()) {
            ScreenOverlay overlay = overlayOptional.get();

            // Big List
            EntryStack<?> mainListFocused = overlay.getEntryList().getFocusedStack();
            if (!mainListFocused.isEmpty() && mainListFocused.getValueType() == ItemStack.class) return (ItemStack) mainListFocused.getValue();

            // Favourites
            if (overlay.getFavoritesList().isPresent()) {
                EntryStack<?> favouritesFocused = overlay.getFavoritesList().get().getFocusedStack();
                if (!favouritesFocused.isEmpty() && favouritesFocused.getValueType() == ItemStack.class) return (ItemStack) favouritesFocused.getValue();
            }

            /*if (MinecraftClient.getInstance().currentScreen instanceof RecipeViewingScreen
                || MinecraftClient.getInstance().currentScreen instanceof VillagerRecipeViewingScreen) {
                item = tryFindItem((MinecraftClient.getInstance().currentScreen).children(), mouseX, mouseY);
                return item;
            }*/

        }
        return null;
    }

    /*@Nullable
    private static ItemStack tryFindItem(@Nullable List<? extends Element> elements, double mouseX, double mouseY) {
        if (elements == null) return null;
        for (Element element : elements) {
            if (element instanceof Widget) {
                Widget widget = (Widget) element;
                if (widget instanceof EntryWidget && widget.containsMouse(mouseX, mouseY)) {
                    EntryWidget entryWidget = (EntryWidget) widget;
                    for (EntryStack entryStack : entryWidget.getEntries()) {
                        if (entryStack instanceof ItemEntryStack) {
                            return entryStack.getItemStack();
                        }
                    }
                }
            }
        }
        return null;
    }*/
}
