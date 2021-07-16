package red.jackf.whereisit.utilities;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public record SearchResult(FoundType foundType, @Nullable Text name) {
}
