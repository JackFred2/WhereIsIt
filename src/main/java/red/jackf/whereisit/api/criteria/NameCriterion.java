package red.jackf.whereisit.api.criteria;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.criteria.VanillaCriteria;

/**
 * Matches against the custom name of an ItemStack. If null, checks for lack of name. Looks for the whole name.
 */
public class NameCriterion extends Criterion {
    private static final String KEY = "Name";

    @Nullable
    private String name = null;

    public NameCriterion() {
        super(VanillaCriteria.NAME);
    }

    public NameCriterion(@Nullable String name) {
        this();
        this.name = name;
    }

    @Override
    public void writeTag(CompoundTag tag) {
        if (name != null) tag.putString(KEY, name);
    }

    @Override
    public void readTag(CompoundTag tag) {
        if (tag.contains(KEY, Tag.TAG_STRING)) this.name = tag.getString(KEY);
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            return stack.getHoverName().getString().equals(name);
        } else {
            return name == null;
        }
    }

    @Override
    public String toString() {
        return "NameCriterion{" +
                "name='" + name + '\'' +
                '}';
    }
}