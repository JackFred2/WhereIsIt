package red.jackf.whereisit.client.defaults;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.world.item.crafting.Ingredient;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.builtin.AnyOfCriterion;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.builtin.ItemTagCriterion;
import red.jackf.whereisit.client.api.events.SearchRequestPopulator;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.util.ArrayList;

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
            if (WhereIsItConfig.INSTANCE.getConfig().getClient().compatibility.recipeBookSupport && containerScreen instanceof RecipeUpdateListener recipeBookHolder) {
                var book = recipeBookHolder.getRecipeBookComponent();
                if (book.ghostRecipe.getRecipe() != null) {
                    for (var i = 0; i < book.ghostRecipe.size(); i++) {
                        var ghost = book.ghostRecipe.get(i);
                        var x = ghost.getX() + containerScreen.leftPos;
                        var y = ghost.getY() + containerScreen.topPos;
                        // ingredient hovered
                        if (x <= mouseX && mouseX < x + 16 && y <= mouseY && mouseY < y + 16) {
                            var criteria = new ArrayList<Criterion>();
                            for (Ingredient.Value value : ghost.ingredient.values) {
                                if (value instanceof Ingredient.ItemValue item) {
                                    SearchRequestPopulator.addItemStack(criteria::add, item.item, SearchRequestPopulator.Context.RECIPE);
                                } else if (value instanceof Ingredient.TagValue tag) {
                                    criteria.add(new ItemTagCriterion(tag.tag));
                                }
                            }
                            request.accept(new AnyOfCriterion(criteria).compact());
                            break;
                        }
                    }
                }
            }
        }
    }
}
