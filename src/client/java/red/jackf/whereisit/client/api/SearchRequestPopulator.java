package red.jackf.whereisit.client.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.SearchRequest;

/**
 * Takes a {@link Screen}, and attempts to add search criteria to a given request. Goes through all populators until
 * one adds criteria.
 * <p>
 * Normally, this just involves adding an {@link ItemStack} using {@link red.jackf.whereisit.api.criteria.ItemCriterion},
 * however it may be used to add more specific criteria, such as a {@link red.jackf.whereisit.api.criteria.TagCriterion}
 * in a recipe viewer, or fluid/energy levels.
 * </p>
 */
public interface SearchRequestPopulator {
    Event<SearchRequestPopulator> EVENT = EventFactory.createArrayBacked(SearchRequestPopulator.class, listeners -> (request, screen, mouseX, mouseY) -> {
        for (SearchRequestPopulator listener : listeners) {
            listener.grabStack(request, screen, mouseX, mouseY);
            if (request.hasCriteria()) break;
        }
    });

    /**
     * Add search criteria from a given screen.
     * @param request Request to add criteria to
     * @param screen Screen currently open on the client
     * @param mouseX Transformed mouse X
     * @param mouseY Transformed mouse Y
     */
    void grabStack(SearchRequest request, Screen screen, int mouseX, int mouseY);
}
