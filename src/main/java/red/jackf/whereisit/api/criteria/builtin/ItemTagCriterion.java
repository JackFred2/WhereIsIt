package red.jackf.whereisit.api.criteria.builtin;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

/**
 * Checks against an item tag.
 */
public record ItemTagCriterion(TagKey<Item> tag) implements Criterion {
    public static final MapCodec<ItemTagCriterion> CODEC = ResourceLocation.CODEC
            .xmap(resLoc -> TagKey.create(Registries.ITEM, resLoc), TagKey::location)
            .xmap(ItemTagCriterion::new, ItemTagCriterion::tag).fieldOf("tag");
    public static final CriterionType<ItemTagCriterion> TYPE = CriterionType.of(CODEC);

    @Override
    public CriterionType<?> type() {
        return TYPE;
    }

    @Override
    public boolean valid() {
        return BuiltInRegistries.ITEM.getTag(tag).isPresent();
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(tag);
    }
}
