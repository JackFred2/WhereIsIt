package red.jackf.whereisit.client.defaults;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.builtin.AnyOfCriterion;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.client.api.events.SearchRequestPopulator;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Populates a request from the vanilla GUIs/recipe book
 */
public class SearchRequestPopulatorDefaults {
    static void setup() {
        SearchRequestPopulator.EVENT.register(SearchRequestPopulatorDefaults::populate);
    }

    private static void populate(SearchRequest request, Screen screen, int mouseX, int mouseY) {
        // normal item
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            // inventory slots
            if (containerScreen.hoveredSlot != null) {
                var stack = containerScreen.hoveredSlot.getItem();
                if (!stack.isEmpty()) {
                    SearchRequestPopulator.addItemStack(request, stack, SearchRequestPopulator.Context.inventory());
                }
            }

            // grab from recipe book highlights
            if (WhereIsItConfig.INSTANCE.instance().getClient().compatibility.recipeBookSupport && containerScreen instanceof AbstractRecipeBookScreen<?> recipeBookScreen) {
                RecipeBookComponent<?> book = recipeBookScreen.recipeBookComponent;
                if (!book.ghostSlots.ingredients.isEmpty() && book.ghostSlots.ingredients.containsKey(recipeBookScreen.hoveredSlot)) {
                    GhostSlots.GhostSlot hovered = book.ghostSlots.ingredients.get(recipeBookScreen.hoveredSlot);

                    List<Criterion> criteria = new ArrayList<>();
                    for (ItemStack stack : hovered.items()) {
                        SearchRequestPopulator.addItemStack(criteria::add, stack, SearchRequestPopulator.Context.RECIPE);
                    }
                    request.accept(new AnyOfCriterion(criteria).compact());
                }
            }
        }
    }
}
