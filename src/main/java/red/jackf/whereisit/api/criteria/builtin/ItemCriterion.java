package red.jackf.whereisit.api.criteria.builtin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

import java.util.Objects;

/**
 * Checks against an item ID.
 */
public class ItemCriterion extends Criterion {
    public static final CriterionType<ItemCriterion> TYPE = CriterionType.of(ItemCriterion::new);
    private static final String KEY = "ItemId";
    private Item item = Items.AIR;

    public ItemCriterion() {
        super(TYPE);
    }

    public ItemCriterion(Item item) {
        this();
        this.item = item;
    }

    @Override
    public void writeTag(CompoundTag tag) {
        tag.putString(KEY, BuiltInRegistries.ITEM.getKey(this.item).toString());
    }

    @Override
    public void readTag(CompoundTag tag) {
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString(KEY)));
        if (item != Items.AIR) this.item = item;
    }

    @Override
    public boolean valid() {
        return this.item != Items.AIR;
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(this.item);
    }

    @Override
    public String toString() {
        return "ItemCriterion{" +
                "item=" + item +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemCriterion that = (ItemCriterion) o;
        return Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }
}
