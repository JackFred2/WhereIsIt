package red.jackf.whereisit.api.criteria;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemCriterion extends Criterion {
    private static final String KEY = "ItemId";
    private Item item = Items.AIR;

    public ItemCriterion() {
        super(VanillaCriteria.ITEM);
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
}
