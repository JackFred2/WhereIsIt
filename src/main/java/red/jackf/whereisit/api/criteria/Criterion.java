package red.jackf.whereisit.api.criteria;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.criteria.builtin.AnyOfCriterion;

/**
 * A test for an ItemStack. Register a supplier to {@link #register(ResourceLocation, CriterionType)}
 */
public interface Criterion {
    Codec<Criterion> CODEC = CriterionType.REGISTRY.byNameCodec()
            .dispatch("type", Criterion::type, CriterionType::codec);

    CriterionType<?> type();

    /**
     * @return If this criterion has valid data.
     */
    default boolean valid() {
        return true;
    }

    /**
     * Returns a compacted version of the criteria. Use this to simplify if needed, such as in {@link AnyOfCriterion#compact()}
     */
    default Criterion compact() {
        return this;
    }

    /**
     * Test against a given ItemStack. This will only be called if {@link Criterion#valid()} returns true.
     * @param stack Stack to test again with loaded data
     * @return If this criterion matches the stack.
     */
    boolean test(ItemStack stack);

    /**
     * Register a new criteria to the registry. Criterion that aren't known to the server will be ignored.
     * @param id ID of the criterion in the registry.
     * @param type Type object of the criteria.
     * @return Type object of the criteria registered; equal to <code>type</code>.
     * @param <T> Extended Criterion class
     */
    static <T extends Criterion> CriterionType<T> register(ResourceLocation id, CriterionType<T> type) {
        return Registry.register(CriterionType.REGISTRY, id, type);
    }
}
