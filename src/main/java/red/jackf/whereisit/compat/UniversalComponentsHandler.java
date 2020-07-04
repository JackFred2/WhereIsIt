package red.jackf.whereisit.compat;

import io.github.cottonmc.component.UniversalComponents;
import io.github.cottonmc.component.item.InventoryComponent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class UniversalComponentsHandler {
    public static boolean searchItem(ItemStack item, Item searchingFor) {
        Optional<InventoryComponent> comp = UniversalComponents.INVENTORY_COMPONENT.maybeGet(item);
        return comp.isPresent() && comp.get().contains(searchingFor);
    }

    public static boolean hasInvComponent(ItemStack item) {
        return UniversalComponents.INVENTORY_COMPONENT.maybeGet(item).isPresent();
    }
}
