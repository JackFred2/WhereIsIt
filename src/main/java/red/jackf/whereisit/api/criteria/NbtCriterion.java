package red.jackf.whereisit.api.criteria;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.criteria.VanillaCriteria;

import java.util.Objects;

/**
 * Checks against an item's NBT tag. Ignores the damage tag of an item if equal to 0, unless specifically disabled.
 * Treats an empty NBT tag the same as no tag
 * To match against a lack of tag, pass null.
 */
public class NbtCriterion extends Criterion {
    private static final String TAG_KEY = "Tag";
    private static final String IGNORE_DAMAGE = "IgnoreZeroDamage";

    @Nullable
    private CompoundTag tagToCheck = null;
    private boolean ignoreZeroDamage = true;
    private static final CompoundTag ZERO_DAMAGE = new CompoundTag();
    static {
        ZERO_DAMAGE.putInt(ItemStack.TAG_DAMAGE, 0);
    }

    public NbtCriterion() {
        super(VanillaCriteria.NBT);
    }

    @Override
    public void writeTag(CompoundTag tag) {
        tag.putBoolean(IGNORE_DAMAGE, ignoreZeroDamage);
        if (tagToCheck != null) tag.put(TAG_KEY, tagToCheck);
    }

    @Override
    public void readTag(CompoundTag tag) {
        ignoreZeroDamage = tag.getBoolean(IGNORE_DAMAGE);
        if (tag.contains(TAG_KEY, Tag.TAG_COMPOUND)) tagToCheck = tag.getCompound(TAG_KEY);
    }

    public NbtCriterion(@Nullable CompoundTag tag, boolean ignoreZeroDamage) {
        this();

        this.ignoreZeroDamage = ignoreZeroDamage;
        this.tagToCheck = tag;
        if (ignoreZeroDamage && tagToCheck != null) {
            removeZeroDamage(tagToCheck);
            if (tagToCheck.isEmpty()) tagToCheck = null;
        }
    }

    private void removeZeroDamage(@Nullable CompoundTag tag) {
        if (tag == null) return;
        if (tag.contains(ItemStack.TAG_DAMAGE, Tag.TAG_INT) && tag.getInt(ItemStack.TAG_DAMAGE) == 0) tag.remove(ItemStack.TAG_DAMAGE);
    }

    @Override
    public boolean test(ItemStack stack) {
        CompoundTag tag;
        if (ignoreZeroDamage) {
            //noinspection DataFlowIssue : hasTag() implies getTag() != null
            tag = stack.hasTag() ? stack.getTag().copy() : null;
            if (tag != null) {
                removeZeroDamage(tag);
                if (tag.isEmpty()) tag = null;
            }
        } else {
            tag = stack.getTag();
        }

        return Objects.equals(tag, tagToCheck);
    }

    @Override
    public String toString() {
        return "NbtCriterion{" +
                "tagToCheck=" + tagToCheck +
                ", ignoreZeroDamage=" + ignoreZeroDamage +
                '}';
    }
}
