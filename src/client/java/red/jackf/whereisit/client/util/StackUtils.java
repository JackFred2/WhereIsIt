package red.jackf.whereisit.client.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

public class StackUtils {
    public static ItemStack fromFluid(Fluid fluid) {
        var bucket = fluid.getBucket();
        if (bucket != Items.AIR) return new ItemStack(bucket);
        return ItemStack.EMPTY;
    }
}
