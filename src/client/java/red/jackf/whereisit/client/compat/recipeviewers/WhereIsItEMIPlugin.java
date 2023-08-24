package red.jackf.whereisit.client.compat.recipeviewers;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.builtin.AnyOfCriterion;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.builtin.FluidCriterion;
import red.jackf.whereisit.api.criteria.builtin.ItemTagCriterion;
import red.jackf.whereisit.client.WhereIsItClient;
import red.jackf.whereisit.client.api.SearchRequestPopulator;
import red.jackf.whereisit.client.api.ShouldIgnoreKey;
import red.jackf.whereisit.client.compat.CompatUtils;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Populates a request from the overlays or recipe screens. <br />
 *
 * Internal usages: <br />
 * - filtering ingredients by tag or not, and stacks by item or fluids <br />
 * - getting the hovered item in a recipe screen, and if it's the result
 */
@SuppressWarnings("UnstableApiUsage")
public class WhereIsItEMIPlugin implements EmiPlugin {
    private static boolean hasErrored = false;

    static {
        WhereIsItClient.LOGGER.info("Hooking into EMI");
        SearchRequestPopulator.EVENT.register((request, screen, mouseX, mouseY) -> {
            if (!WhereIsItConfig.INSTANCE.getConfig().getClient().compatibility.emiSupport) return;
            if (hasErrored) return;
            try {
                populate(request, screen, mouseX, mouseY);
            } catch (Exception ex) {
                CompatUtils.LOGGER.error("Error in REI handler, disabling", ex);
                hasErrored = true;
            }
        });
        ShouldIgnoreKey.EVENT.register(EmiApi::isSearchFocused);
    }

    // TODO fix adding ingredients from both this and the vanilla handler, as #getHoveredStack() also looks in GUIs.
    //      not too important though
    // TODO support the recipe tree screen
    private static void populate(SearchRequest request, Screen screen, int mouseX, int mouseY) {
        // look on overlay
        var ingredient = EmiApi.getHoveredStack(mouseX, mouseY, false).getStack();
        //            we want favourites, but not the crafting history or craftables, so easier to just class check
        var context = ingredient.getClass() == EmiFavorite.class ? SearchRequestPopulator.Context.FAVOURITE : SearchRequestPopulator.Context.overlay();

        // nothing from overlay, look for recipe screen
        if (ingredient.isEmpty() && screen instanceof RecipeScreen recipeScreen) {
            ingredient = recipeScreen.getHoveredStack();
            context = SearchRequestPopulator.Context.RECIPE;
        }
        if (ingredient.isEmpty()) return;

        if (ingredient instanceof EmiFavorite favorite) ingredient = favorite.getStack();
        if (ingredient instanceof TagEmiIngredient tag && tag.key.registry() == Registries.ITEM) {
            //noinspection unchecked
            request.accept(new ItemTagCriterion((TagKey<Item>) tag.key));
        } else {
            List<Criterion> criterion = new ArrayList<>();
            for (EmiStack emiStack : ingredient.getEmiStacks())
                getCriterion(criterion::add, emiStack, context);
            if (!criterion.isEmpty())
                request.accept(new AnyOfCriterion(criterion).compact());
        }
    }

    private static void getCriterion(Consumer<Criterion> consumer, EmiStack emiStack, SearchRequestPopulator.Context context) {
        if (emiStack instanceof ItemEmiStack itemStack) {
            SearchRequestPopulator.addItemStack(consumer, itemStack.getItemStack(), context);
        } else if (emiStack instanceof FluidEmiStack fluidStack) {
            consumer.accept(new FluidCriterion((Fluid) fluidStack.getKey()));
        }
    }

    @Override
    public void register(EmiRegistry registry) {

    }
}
