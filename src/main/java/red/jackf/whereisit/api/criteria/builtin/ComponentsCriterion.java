package red.jackf.whereisit.api.criteria.builtin;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

public record ComponentsCriterion(DataComponentPatch components) implements Criterion {
    public static final MapCodec<ComponentsCriterion> CODEC = DataComponentPatch.CODEC.xmap(ComponentsCriterion::new, c -> c.components).fieldOf("components");
    public static final CriterionType<ComponentsCriterion> TYPE = CriterionType.of(CODEC);

    @Override
    public CriterionType<?> type() {
        return TYPE;
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.getComponentsPatch().equals(this.components);
    }
}
