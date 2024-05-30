package red.jackf.whereisit.api.criteria.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

import java.util.Optional;

/**
 * A criterion matching against an enchantment (or lack of), optionally along with a target level.
 */
public record EnchantmentCriterion(Holder<Enchantment> enchantment, @Nullable Integer targetLevel) implements Criterion {
    public static final MapCodec<EnchantmentCriterion> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Enchantment.CODEC.fieldOf("enchantment").forGetter(EnchantmentCriterion::enchantment),
            Codec.INT.optionalFieldOf("targetLevel").forGetter(ench -> Optional.ofNullable(ench.targetLevel))
    ).apply(instance, EnchantmentCriterion::new));
    public static final CriterionType<EnchantmentCriterion> TYPE = CriterionType.of(CODEC);

    private EnchantmentCriterion(Holder<Enchantment> enchantment, Optional<Integer> targetLevel) {
        this(enchantment, targetLevel.orElse(null));
    }

    @Override
    public CriterionType<?> type() {
        return TYPE;
    }

    @Override
    public boolean valid() {
        return enchantment != null;
    }

    @Override
    public boolean test(ItemStack stack) {
        ItemEnchantments component = stack.get(EnchantmentHelper.getComponentType(stack));
        if (component == null) return false;

        int level = component.getLevel(enchantment);
        if (targetLevel != null) return targetLevel == level;
        else return level > 0;
    }

    @Override
    public String toString() {
        return "EnchantmentCriterion{" +
                "enchantment=" + enchantment.getRegisteredName() +
                ", targetLevel=" + targetLevel +
                '}';
    }
}
