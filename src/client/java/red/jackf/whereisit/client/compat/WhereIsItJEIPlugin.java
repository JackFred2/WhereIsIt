package red.jackf.whereisit.client.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.FluidCriterion;
import red.jackf.whereisit.api.criteria.ItemCriterion;
import red.jackf.whereisit.client.WhereIsItClient;
import red.jackf.whereisit.client.api.SearchRequestPopulator;

public final class WhereIsItJEIPlugin implements IModPlugin {
    private boolean setup = false;
    private IJeiRuntime runtime = null;

    @Override
    @NotNull
    public ResourceLocation getPluginUid() {
        return WhereIsIt.id("stack_grabber");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (!setup) {
            WhereIsItClient.LOGGER.info("Enabling JEI Support");
            SearchRequestPopulator.EVENT.register((request, screen, mouseX, mouseY) -> {
                if (runtime != null) {
                    var ingredientsStack = parseIngredient(request, runtime.getIngredientListOverlay()::getIngredientUnderMouse);
                    if (ingredientsStack) return;

                    var bookmarkStack = parseIngredient(request, runtime.getBookmarkOverlay()::getIngredientUnderMouse);
                    if (bookmarkStack) return;

                    getRecipeStack(request, runtime.getRecipesGui());
                }
            });
            setup = true;
        }
        this.runtime = jeiRuntime;
    }

    private void getRecipeStack(SearchRequest request, IRecipesGui recipe) {
        var stack = recipe.getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
        if (stack.isPresent()) {
            request.add(new ItemCriterion(stack.get().getItem()));
            return;
        }
        var fluid = recipe.getIngredientUnderMouse(FabricTypes.FLUID_STACK);
        fluid.ifPresent(fluidIngredient -> request.add(new FluidCriterion(fluidIngredient.getFluid())));
    }

    private interface OverlayGetter {
        <I> I get(IIngredientType<I> type);
    }

    private boolean parseIngredient(SearchRequest request, OverlayGetter getter) {
        var stack = getter.get(VanillaTypes.ITEM_STACK);
        if (stack != null) {
            request.add(new ItemCriterion(stack.getItem()));
            return true;
        }
        var fluidIngredient = getter.get(FabricTypes.FLUID_STACK);
        if (fluidIngredient != null) {
            request.add(new FluidCriterion(fluidIngredient.getFluid()));
            return true;
        }
        return false;
    }

    @Override
    public void onRuntimeUnavailable() {
        this.runtime = null;
    }
}
