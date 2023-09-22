package red.jackf.whereisit.client.compat.recipeviewers;

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
import red.jackf.whereisit.api.criteria.builtin.AnyOfCriterion;
import red.jackf.whereisit.api.criteria.builtin.FluidCriterion;
import red.jackf.whereisit.api.criteria.builtin.ItemTagCriterion;
import red.jackf.whereisit.client.WhereIsItClient;
import red.jackf.whereisit.client.api.events.SearchRequestPopulator;
import red.jackf.whereisit.client.api.events.ShouldIgnoreKey;
import red.jackf.whereisit.client.compat.CompatUtils;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.util.function.Consumer;

/**
 * Internal usages: <br />
 * - getting a tag ID from a recipe slot
 */
public class WhereIsItREIPlugin implements REIClientPlugin {
    private boolean hasErrored = false;
    public WhereIsItREIPlugin() {
        super();
        WhereIsItClient.LOGGER.info("Hooking into REI");

        SearchRequestPopulator.EVENT.register((request, screen, mouseX, mouseY) -> {
            if (!WhereIsItConfig.INSTANCE.getConfig().getClient().compatibility.reiSupport) return;
            if (hasErrored) return;
            try {
                if (REIRuntime.getInstance().isOverlayVisible()) {
                    if (getFromOverlay(request)) return;
                }

                if (screen instanceof DisplayScreen) {
                    getFromRecipeScreen(request, screen, mouseX, mouseY);
                }
            } catch (Exception ex) {
                CompatUtils.LOGGER.error("Error in REI handler, disabling", ex);
                hasErrored = true;
            }
        });

        ShouldIgnoreKey.EVENT.register(() -> {
            var textField = REIRuntime.getInstance().getSearchTextField();
            return textField != null && textField.isFocused();
        });
    }

    private static boolean getFromOverlay(SearchRequest request) {
        var overlayOpt = REIRuntime.getInstance().getOverlay();
        if (overlayOpt.isEmpty()) return true;
        var overlay = overlayOpt.get();
        var hoveredEntry = overlay.getEntryList().getFocusedStack();
        if (!hoveredEntry.isEmpty()) {
            parseEntryStack(request, hoveredEntry, SearchRequestPopulator.Context.overlay());
            return true;
        }
        var favourites = overlay.getFavoritesList();
        if (favourites.isPresent()) {
            var hoveredFavourite = favourites.get().getFocusedStack();
            if (!hoveredFavourite.isEmpty()) {
                parseEntryStack(request, hoveredFavourite, SearchRequestPopulator.Context.FAVOURITE);
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
            // TODO support fluid tags
            if (slot instanceof EntryWidget entryWidget && entryWidget.tagMatch != null) {
                var key = TagKey.create(Registries.ITEM, entryWidget.tagMatch);
                if (BuiltInRegistries.ITEM.getTag(key).isPresent()) {
                    request.accept(new ItemTagCriterion(key));
                }
            } else {
                var criteria = new AnyOfCriterion();
                for (EntryStack<?> entry : slot.getEntries()) {
                    parseEntryStack(criteria, entry, SearchRequestPopulator.Context.RECIPE);
                }
                request.accept(criteria.compact());
            }
            return;
        }
    }

    /**
     * Parses an REI EntryStack, then passes 0 or more Criterion to <code>consumer</code>.
     */
    private static void parseEntryStack(Consumer<Criterion> consumer, EntryStack<?> entryStack, SearchRequestPopulator.Context context) {
        var value = entryStack.getValue();
        if (value instanceof ItemStack stack) {
            SearchRequestPopulator.addItemStack(consumer, stack, context);
        } else if (value instanceof FluidStack fluidStack) {
            consumer.accept(new FluidCriterion(fluidStack.getFluid()));
        }
    }
}
