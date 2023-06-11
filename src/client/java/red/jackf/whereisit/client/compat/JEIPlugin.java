package red.jackf.whereisit.client.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.client.api.StackGrabber;

public final class JEIPlugin implements IModPlugin {
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
            StackGrabber.EVENT.register((screen, mouseX, mouseY) -> {
                if (runtime != null) {
                    var ingredientsStack = getOverlayStack(runtime.getIngredientListOverlay()::getIngredientUnderMouse);
                    if (ingredientsStack != null) return ingredientsStack;

                    var bookmarkStack = getOverlayStack(runtime.getBookmarkOverlay()::getIngredientUnderMouse);
                    if (bookmarkStack != null) return bookmarkStack;

                    return getRecipeStack(runtime.getRecipesGui());
                }
                return null;
            });
            setup = true;
        }
        this.runtime = jeiRuntime;
    }

    @Nullable
    private ItemStack getRecipeStack(IRecipesGui recipe) {
        var stack = recipe.getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
        if (stack.isPresent()) return stack.get();
        var fluid = recipe.getIngredientUnderMouse(FabricTypes.FLUID_STACK);
        if (fluid.isPresent()) {
            var bucket = fluid.get().getFluid().getBucket();
            if (bucket != Items.AIR) return new ItemStack(bucket);
        }
        return null;
    }

    private interface OverlayGetter {
        <I> I get(IIngredientType<I> type);
    }

    @Nullable
    private ItemStack getOverlayStack(OverlayGetter getter) {
        var stack = getter.get(VanillaTypes.ITEM_STACK);
        if (stack != null) return stack;
        var fluidIngredient = getter.get(FabricTypes.FLUID_STACK);
        if (fluidIngredient != null) {
            var bucketItem = fluidIngredient.getFluid().getBucket();
            if (bucketItem != Items.AIR) return new ItemStack(bucketItem);
        }
        return null;
    }

    @Override
    public void onRuntimeUnavailable() {
        this.runtime = null;
    }
}
