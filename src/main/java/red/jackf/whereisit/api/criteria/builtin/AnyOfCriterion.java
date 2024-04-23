package red.jackf.whereisit.api.criteria.builtin;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Acts as an 'OR' gate for a list of criterion. Create this using {@link AnyOfCriterion(Collection)}, then (recommended)
 * {@link AnyOfCriterion#compact()} before you add it to a search request.
 */
public class AnyOfCriterion implements Criterion, Consumer<Criterion> {
    public static final MapCodec<AnyOfCriterion> CODEC = Criterion.CODEC.listOf().xmap(AnyOfCriterion::new, all -> all.criteria).fieldOf("any_of");
    public static final CriterionType<AnyOfCriterion> TYPE = CriterionType.of(CODEC);

    private final List<Criterion> criteria = new ArrayList<>();
    public AnyOfCriterion() {}

    public AnyOfCriterion(Collection<Criterion> criteria) {
        this.criteria.addAll(criteria);
    }

    /**
     * Flattens the OR condition if just 1 criterion is specified.
     */
    public Criterion compact() {
        if (criteria.size() == 1) return criteria.get(0);
        return this;
    }

    @Override
    public CriterionType<?> type() {
        return TYPE;
    }

    @Override
    public boolean valid() {
        return !criteria.isEmpty() && criteria.stream().allMatch(Criterion::valid);
    }

    @Override
    public boolean test(ItemStack stack) {
        return criteria.stream().anyMatch(c -> c.test(stack));
    }

    @Override
    public void accept(Criterion criterion) {
        this.criteria.add(criterion);
    }

    @Override
    public String toString() {
        return "AnyOfCriterion{" +
                "criteria=" + criteria +
                '}';
    }
}
