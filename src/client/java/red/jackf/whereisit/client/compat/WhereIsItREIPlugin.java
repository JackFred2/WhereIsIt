package red.jackf.whereisit.client.compat;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.*;
import red.jackf.whereisit.client.WhereIsItClient;
import red.jackf.whereisit.client.api.SearchRequestPopulator;

import java.util.function.Consumer;

/**
 * Internal usages: <br />
 * - getting a tag ID from a recipe slot
 */
public class WhereIsItREIPlugin implements REIClientPlugin {
    public WhereIsItREIPlugin() {
        super();
        WhereIsItClient.LOGGER.info("Enabling REI Support");

        SearchRequestPopulator.EVENT.register((request, screen, mouseX, mouseY) -> {
            if (REIRuntime.getInstance().isOverlayVisible()) {
                if (getFromOverlay(request)) return;
            }

            if (screen instanceof DisplayScreen) {
                getFromRecipeScreen(request, screen, mouseX, mouseY);
            }
        });
    }

    private static boolean getFromOverlay(SearchRequest request) {
        var overlayOpt = REIRuntime.getInstance().getOverlay();
        if (overlayOpt.isEmpty()) return true;
        var overlay = overlayOpt.get();
        var hoveredEntry = overlay.getEntryList().getFocusedStack();
        if (!hoveredEntry.isEmpty()) {
            parseEntryStack(request, hoveredEntry);
            return true;
        }
        var favourites = overlay.getFavoritesList();
        if (favourites.isPresent()) {
            var hoveredFavourite = favourites.get().getFocusedStack();
            if (!hoveredFavourite.isEmpty()) {
                parseEntryStack(request, hoveredFavourite);
                return true;
            }
        }
        return false;
    }

    // TODO: Support the tag screen; may have to look into TagTreeWidget
    private static void getFromRecipeScreen(SearchRequest request, Screen screen, int mouseX, int mouseY) {
        for (var widget : Widgets.walk(screen.children(), g ->
                g instanceof Slot slot && slot.isMouseOver(mouseX, mouseY)
        )) {
            var slot = (Slot) widget;
            if (slot instanceof EntryWidget entryWidget && entryWidget.tagMatch != null) {
                var key = TagKey.create(Registries.ITEM, entryWidget.tagMatch);
                if (BuiltInRegistries.ITEM.getTag(key).isPresent()) {
                    request.add(new TagCriterion(key));
                }
            } else {
                var criteria = new AnyOfCriterion();
                for (EntryStack<?> entry : slot.getEntries()) {
                    parseEntryStack(criteria, entry);
                }
                request.add(criteria.compact());
            }
            return;
        }
    }

    /**
     * Parses an REI EntryStack, then passes 0 or more Criterion to <code>consumer</code>.
     */
    private static void parseEntryStack(Consumer<Criterion> consumer, EntryStack<?> entryStack) {
        var value = entryStack.getValue();
        if (value instanceof ItemStack stack) {
            consumer.accept(new ItemCriterion(stack.getItem()));
        } else if (value instanceof FluidStack fluidStack) {
            consumer.accept(new FluidCriterion(fluidStack.getFluid()));
        }
    }
}
