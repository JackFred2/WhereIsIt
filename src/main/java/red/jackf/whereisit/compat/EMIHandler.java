package red.jackf.whereisit.compat;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class EMIHandler {
    public static ItemStack findEMIItems() {
        EmiStackInteraction interaction = EmiApi.getHoveredStack(false);
        return !interaction.isEmpty() ? interaction.getStack().getEmiStacks().get(0).getItemStack() : null;
    }
}
