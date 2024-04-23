package red.jackf.whereisit.api.criteria.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

import java.util.Locale;
import java.util.Optional;

/**
 * Matches against the custom name of an ItemStack. If null, checks for lack of name. Looks for the whole name.
 */
public record NameCriterion(@Nullable String name) implements Criterion {
    public static final MapCodec<NameCriterion> CODEC = Codec.STRING.optionalFieldOf("name").xmap(opt -> new NameCriterion(opt.orElse(null)), name -> Optional.ofNullable(name.name));
    public static final CriterionType<NameCriterion> TYPE = CriterionType.of(CODEC);

    @Override
    public CriterionType<?> type() {
        return TYPE;
    }

    @Override
    public boolean test(ItemStack stack) {
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName == null) {
            return name == null;
        } else if (this.name == null) {
            return false;
        } else {
            return customName.getString().toLowerCase(Locale.ROOT).contains(this.name.toLowerCase(Locale.ROOT));
        }
    }
}
