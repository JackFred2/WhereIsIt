package red.jackf.whereisit.api.criteria;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.criteria.builtin.AnyOfCriterion;

/**
 * A test for an ItemStack. Register a supplier to {@link #register(ResourceLocation, CriterionType)}
 */
public abstract class Criterion {
    public final CriterionType<?> type;

    public Criterion(CriterionType<?> type) {
        this.type = type;
    }
    /**
     * Write this criterion's data to a tag for network serialization; ran on client side.
     * @param tag Tag to write to
     */
    public void writeTag(CompoundTag tag) {}

    /**
     * Load this criterion's data from a tag. Ran on the server side.
     *
     * @param tag Tag to read from
     */
    public void readTag(CompoundTag tag) {}

    /**
     * @return If this criterion has valid data.
     */
    public boolean valid() {
        return true;
    }

    /**
     * Returns a compacted version of the criteria. Use this to simplify if needed, such as in {@link AnyOfCriterion#compact()}
     */
    public Criterion compact() {
        return this;
    }

    /**
     * Test against a given ItemStack. This will only be called if {@link Criterion#readTag(CompoundTag)} returns true.
     * @param stack Stack to test again with loaded data
     * @return If this criterion matches the stack.
     */
    public abstract boolean test(ItemStack stack);

    /**
     * Register a new criteria to the registry. Criterion that aren't known to the server will be ignored.
     * @param id ID of the criterion in the registry.
     * @param type Type object of the criteria.
     * @return Type object of the criteria registered; equal to <code>type</code>.
     * @param <T> Extended Criterion class
     */
    public static <T extends Criterion> CriterionType<T> register(ResourceLocation id, CriterionType<T> type) {
        return Registry.register(CriterionType.REGISTRY, id, type);
    }

}
