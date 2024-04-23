package red.jackf.whereisit.api.criteria.builtin;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

/**
 * Checks against an item ID.
 */
public record ItemCriterion(Item item) implements Criterion {
    public static final MapCodec<ItemCriterion> CODEC = BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").xmap(ItemCriterion::new, ItemCriterion::item);
    public static final CriterionType<ItemCriterion> TYPE = CriterionType.of(CODEC);

    @Override
    public CriterionType<?> type() {
        return TYPE;
    }

    @Override
    public boolean valid() {
        return this.item != Items.AIR;
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(this.item);
    }
}
