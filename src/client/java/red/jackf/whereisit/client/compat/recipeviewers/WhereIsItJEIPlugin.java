package red.jackf.whereisit.client.compat.recipeviewers;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.FluidCriterion;
import red.jackf.whereisit.api.criteria.ItemCriterion;
import red.jackf.whereisit.client.WhereIsItClient;
import red.jackf.whereisit.client.api.SearchRequestPopulator;
import red.jackf.whereisit.config.WhereIsItConfig;

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
        if (FabricLoader.getInstance().isModLoaded("emi")) return; // JEMI
        if (!setup) {
            WhereIsItClient.LOGGER.info("Hooking into JEI");
            SearchRequestPopulator.EVENT.register((request, screen, mouseX, mouseY) -> {
                if (!WhereIsItConfig.INSTANCE.getConfig().getClient().compatibility.jeiSupport) return;
                if (runtime != null) {
                    var ingredientsStack = parseIngredient(request, runtime.getIngredientListOverlay()::getIngredientUnderMouse, SearchRequestPopulator.Context.overlay());
                    if (ingredientsStack) return;

                    var bookmarkStack = parseIngredient(request, runtime.getBookmarkOverlay()::getIngredientUnderMouse, SearchRequestPopulator.Context.FAVOURITE);
                    if (bookmarkStack) return;

                    getRecipeStack(request, runtime.getRecipesGui());
                }
            });
            setup = true;
        }
        this.runtime = jeiRuntime;
    }

    // TODO support multiple ingredients/tags
    private void getRecipeStack(SearchRequest request, IRecipesGui recipe) {
        var stack = recipe.getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
        if (stack.isPresent()) {
            request.accept(new ItemCriterion(stack.get().getItem()));
            return;
        }
        var fluid = recipe.getIngredientUnderMouse(FabricTypes.FLUID_STACK);
        fluid.ifPresent(fluidIngredient -> request.accept(new FluidCriterion(fluidIngredient.getFluid())));
    }

    private interface OverlayGetter {
        <I> I get(IIngredientType<I> type);
    }

    private boolean parseIngredient(SearchRequest request, OverlayGetter getter, SearchRequestPopulator.Context context) {
        var stack = getter.get(VanillaTypes.ITEM_STACK);
        if (stack != null) {
            SearchRequestPopulator.addItemStack(request, stack, context);
            return true;
        }
        var fluidIngredient = getter.get(FabricTypes.FLUID_STACK);
        if (fluidIngredient != null) {
            request.accept(new FluidCriterion(fluidIngredient.getFluid()));
            return true;
        }
        return false;
    }

    @Override
    public void onRuntimeUnavailable() {
        this.runtime = null;
    }
}
