package red.jackf.whereisit.client.compat;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.*;
import red.jackf.whereisit.client.WhereIsItClient;
import red.jackf.whereisit.client.api.SearchRequestPopulator;

import java.util.List;
import java.util.Objects;

/**
 * Populates a request from the overlays or recipe screens. <br />
 *
 * Internal usages: <br />
 * - filtering ingredients by tag or not, and stacks by item or fluids <br />
 * - getting the hovered item in a recipe screen
 */
@SuppressWarnings("UnstableApiUsage")
public class WhereIsItEMICompat implements EmiPlugin {
    static {
        WhereIsItClient.LOGGER.info("Enabling EMI Support");
        SearchRequestPopulator.EVENT.register(WhereIsItEMICompat::populate);
    }

    // TODO support the recipe tree screen
    private static void populate(SearchRequest request, Screen screen, int mouseX, int mouseY) {
        var ingredient = EmiApi.getHoveredStack(mouseX, mouseY, true).getStack();
        if (ingredient.isEmpty() && screen instanceof RecipeScreen recipeScreen)
            ingredient = recipeScreen.getHoveredStack();
        if (ingredient.isEmpty()) return;

        if (ingredient instanceof TagEmiIngredient tagIngredient && tagIngredient.key.registry() == Registries.ITEM) {
            //noinspection unchecked
            request.add(new TagCriterion((TagKey<Item>) tagIngredient.key));
        } else {
            List<Criterion> criterion = ingredient.getEmiStacks().stream()
                    .map(WhereIsItEMICompat::getCriterion)
                    .filter(Objects::nonNull)
                    .toList();
            request.add(new AnyOfCriterion(criterion).compact());
        }
    }

    private static Criterion getCriterion(EmiStack emiStack) {
        if (emiStack instanceof ItemEmiStack itemStack) {
            return new ItemCriterion(itemStack.getItemStack().getItem());
        } else if (emiStack instanceof FluidEmiStack fluidStack) {
            return new FluidCriterion((Fluid) fluidStack.getKey());
        } else {
            return null;
        }
    }

    @Override
    public void register(EmiRegistry registry) {

    }
}
