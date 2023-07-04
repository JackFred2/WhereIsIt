package red.jackf.whereisit.api.criteria;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.criteria.VanillaCriteria;

import java.util.Objects;

/**
 * Checks against an item tag.
 */
public class ItemTagCriterion extends Criterion {
    private static final String KEY = "TagId";
    private TagKey<Item> tag = null;
    public ItemTagCriterion() {
        super(VanillaCriteria.TAG);
    }

    public ItemTagCriterion(TagKey<Item> tag) {
        this();
        this.tag = tag;
    }

    @Override
    public void writeTag(CompoundTag tag) {
        tag.putString(KEY, this.tag.location().toString());
    }

    @Override
    public void readTag(CompoundTag tag) {
        var id = ResourceLocation.tryParse(tag.getString(KEY));
        if (id != null) {
            this.tag = TagKey.create(Registries.ITEM, id);
        }
    }

    @Override
    public boolean valid() {
        return BuiltInRegistries.ITEM.getTag(tag).isPresent();
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(tag);
    }

    @Override
    public String toString() {
        return "ItemTagCriterion{" +
                "tag=" + tag +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemTagCriterion that = (ItemTagCriterion) o;
        return Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }
}
