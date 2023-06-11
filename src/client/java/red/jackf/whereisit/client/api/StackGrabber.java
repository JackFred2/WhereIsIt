package red.jackf.whereisit.client.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Takes a {@link Screen}, and attempts to obtain a relevant ItemStack.
 */
public interface StackGrabber {
    Event<StackGrabber> EVENT = EventFactory.createArrayBacked(StackGrabber.class, listeners -> (screen, mouseX, mouseY) -> {
        for (StackGrabber listener : listeners) {
            var stack = listener.grabStack(screen, mouseX, mouseY);
            if (stack != null) return stack;
        }
        return null;
    });

    @Nullable
    ItemStack grabStack(Screen screen, int mouseX, int mouseY);
}
