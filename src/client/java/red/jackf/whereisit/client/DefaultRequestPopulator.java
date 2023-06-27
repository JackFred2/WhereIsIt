package red.jackf.whereisit.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.world.item.crafting.Ingredient;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.AnyOfCriterion;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.ItemCriterion;
import red.jackf.whereisit.api.criteria.TagCriterion;

import java.util.ArrayList;

public class DefaultRequestPopulator {
    public static void populate(SearchRequest request, Screen screen, int mouseX, int mouseY) {
        // normal item
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            // inventory slots
            if (containerScreen.hoveredSlot != null) {
                var stack = containerScreen.hoveredSlot.getItem();
                if (!stack.isEmpty()) {
                    request.add(new ItemCriterion(stack.getItem()));
                }
            }

            // grab from recipe book highlights
            if (containerScreen instanceof RecipeUpdateListener recipeBookHolder) {
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
                                    criteria.add(new ItemCriterion(item.item.getItem()));
                                } else if (value instanceof Ingredient.TagValue tag) {
                                    criteria.add(new TagCriterion(tag.tag));
                                }
                            }
                            request.add(new AnyOfCriterion(criteria).compact());
                            break;
                        }
                    }
                }
            }
        }
    }
}
