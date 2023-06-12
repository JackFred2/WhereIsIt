package red.jackf.whereisit.client.compat;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.client.WhereIsItClient;
import red.jackf.whereisit.client.api.StackGrabber;
import red.jackf.whereisit.client.util.StackUtils;

public class WhereIsItREIPlugin implements REIClientPlugin {
    public WhereIsItREIPlugin() {
        super();
        WhereIsItClient.LOGGER.debug("Enabling REI Support");

        StackGrabber.EVENT.register((screen, mouseX, mouseY) -> {
            if (!REIRuntime.getInstance().isOverlayVisible()) return null;
            var overlayOpt = REIRuntime.getInstance().getOverlay();
            if (overlayOpt.isEmpty()) return null;
            var overlay = overlayOpt.get();
            var hoveredEntry = overlay.getEntryList().getFocusedStack();
            if (!hoveredEntry.isEmpty()) {
                var entry = fromEntryStack(hoveredEntry);
                if (entry != null) return entry;
            }
            var favourites = overlay.getFavoritesList();
            if (favourites.isPresent()) {
                var hoveredFavourite = favourites.get().getFocusedStack();
                if (!hoveredFavourite.isEmpty()) {
                    var entry = fromEntryStack(hoveredFavourite);
                    if (entry != null) return entry;
                }
            }
            // Recipe Screen
            // TODO: Support the tag screen; may have to look into TagTreeWidget
            if (screen instanceof DisplayScreen) {
                for (var widget : Widgets.walk(screen.children(), g ->
                    g instanceof Slot slot && slot.isMouseOver(mouseX, mouseY)
                )) {
                    var slot = (Slot) widget;
                    var current = fromEntryStack(slot.getCurrentEntry());
                    if (current != null) return current;
                }
            }
            return null;
        });
    }

    @Nullable
    private static ItemStack fromEntryStack(EntryStack<?> entryStack) {
        var value = entryStack.getValue();
        if (value instanceof ItemStack stack) return stack;
        else if (value instanceof FluidStack fluidStack) {
            var fluid = fluidStack.getFluid();
            var stack = StackUtils.fromFluid(fluid);
            if (!stack.isEmpty()) return stack;
        }
        return null;
    }
}
