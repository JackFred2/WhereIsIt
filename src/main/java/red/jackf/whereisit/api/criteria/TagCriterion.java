package red.jackf.whereisit.api.criteria;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.criteria.VanillaCriteria;

/**
 * Checks against an item tag
 */
public class TagCriterion extends Criterion {
    private static final String KEY = "TagId";
    private TagKey<Item> tag = null;
    public TagCriterion() {
        super(VanillaCriteria.TAG);
    }

    public TagCriterion(TagKey<Item> tag) {
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
        if (id != null)
            this.tag = TagKey.create(Registries.ITEM, id);
    }

    @Override
    public boolean valid() {
        return tag != null;
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(tag);
    }
}
