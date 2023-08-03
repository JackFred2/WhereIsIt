package red.jackf.whereisit.api;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record SearchResult(BlockPos pos, @Nullable ItemStack item, @Nullable Component name) {
}
