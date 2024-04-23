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
 * Acts as an 'AND' gate for a list of criterion. Create this using {@link AllOfCriterion (Collection)}, then (recommended)
 * {@link AllOfCriterion#compact()} before you add it to a search request.
 */
public class AllOfCriterion implements Criterion, Consumer<Criterion> {
    public static final MapCodec<AllOfCriterion> CODEC = Criterion.CODEC.listOf().xmap(AllOfCriterion::new, all -> all.criteria).fieldOf("all_of");
    public static final CriterionType<AllOfCriterion> TYPE = CriterionType.of(CODEC);

    public final List<Criterion> criteria = new ArrayList<>();

    @SuppressWarnings("unused")
    public AllOfCriterion() {}

    public AllOfCriterion(Collection<Criterion> criteria) {
        this.criteria.addAll(criteria);
    }

    /**
     * Flattens the AND condition if just 1 criterion is specified.
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
        return criteria.stream().allMatch(c -> c.test(stack));
    }

    @Override
    public void accept(Criterion criterion) {
        if (criterion instanceof AllOfCriterion allOfCriterion) {
            this.criteria.addAll(allOfCriterion.criteria);
        } else {
            this.criteria.add(criterion);
        }
    }

    @Override
    public String toString() {
        return "AllOfCriterion{" +
                "criteria=" + criteria +
                '}';
    }
}
