package red.jackf.whereisit;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.ItemEntryStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class REIHandler {
    private REIHandler() {};

    public static Item findREIItems(double mouseX, double mouseY) {
        // Big List
        Item item = tryFindItem(ContainerScreenOverlay.getEntryListWidget().children(), mouseX, mouseY);
        if (item != null) return item;

        // Favourites
        if (ContainerScreenOverlay.getFavoritesListWidget() != null) {
            item = tryFindItem(ContainerScreenOverlay.getFavoritesListWidget().children(), mouseX, mouseY);
            if (item != null) return item;
        }

        if (MinecraftClient.getInstance().currentScreen instanceof RecipeViewingScreen
         || MinecraftClient.getInstance().currentScreen instanceof VillagerRecipeViewingScreen) {
            item = tryFindItem((MinecraftClient.getInstance().currentScreen).children(), mouseX, mouseY);
            if (item != null) return item;
        }

        return null;
    }

    @Nullable
    private static Item tryFindItem(@Nullable List<? extends Element> elements, double mouseX, double mouseY) {
        if (elements == null) return null;
        for (Element element : elements) {
            if (element instanceof Widget) {
                Widget widget = (Widget) element;
                if (widget instanceof EntryWidget && widget.containsMouse(mouseX, mouseY)) {
                    EntryWidget entryWidget = (EntryWidget) widget;
                    for (EntryStack entryStack : entryWidget.getEntries()) {
                        if (entryStack instanceof ItemEntryStack) {
                            return entryStack.getItem();
                        }
                    }
                }
            }
        }
        return null;
    }
}
