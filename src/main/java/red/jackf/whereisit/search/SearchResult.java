package red.jackf.whereisit.search;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public record SearchResult(BlockPos pos, ItemStack item) {
}
