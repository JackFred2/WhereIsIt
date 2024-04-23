package red.jackf.whereisit.api.criteria.builtin;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

public record PotionEffectCriterion(Potion potion) implements Criterion {
    public static final MapCodec<PotionEffectCriterion> CODEC = BuiltInRegistries.POTION.byNameCodec().xmap(PotionEffectCriterion::new, PotionEffectCriterion::potion)
            .fieldOf("potion");
    public static final CriterionType<PotionEffectCriterion> TYPE = CriterionType.of(CODEC);

    @Override
    public CriterionType<?> type() {
        return TYPE;
    }

    @Override
    public boolean valid() {
        return potion != null;
    }

    @Override
    public boolean test(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return false;
        return contents.potion().isPresent() && contents.potion().get().value().equals(this.potion);
    }
}
