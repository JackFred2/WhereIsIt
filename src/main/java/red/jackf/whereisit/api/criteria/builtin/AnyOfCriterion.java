package red.jackf.whereisit.api.criteria.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Acts as an 'OR' gate for a list of criterion. Create this using {@link AnyOfCriterion(Collection)}, then (recommended)
 * {@link AnyOfCriterion#compact()} before you add it to a search request.
 */
public class AnyOfCriterion extends Criterion implements Consumer<Criterion> {
    public static final CriterionType<AnyOfCriterion> TYPE = CriterionType.of(AnyOfCriterion::new);
    private static final String KEY = "AnyOf";
    private final List<Criterion> criteria = new ArrayList<>();
    public AnyOfCriterion() {
        super(TYPE);
    }

    public AnyOfCriterion(Collection<Criterion> criteria) {
        super(TYPE);
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
    public void writeTag(CompoundTag tag) {
        var list = new ListTag();
        for (Criterion criterion : criteria) {
            list.add(SearchRequest.toTag(criterion));
        }
        tag.put(KEY, list);
    }

    @Override
    public void readTag(CompoundTag tag) {
        if (tag.contains(KEY, Tag.TAG_LIST)) {
            var list = tag.getList(KEY, Tag.TAG_COMPOUND);
            for (Tag entry : list) {
                if (entry instanceof CompoundTag compound) {
                    var criterion = SearchRequest.fromTag(compound);
                    if (criterion != null && criterion.valid()) criteria.add(criterion);
                }
            }
        }
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
        return "AnyOfCriterion[" +
                criteria.stream().map(Criterion::toString).collect(Collectors.joining(", ")) +
                "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnyOfCriterion that = (AnyOfCriterion) o;
        return Objects.equals(criteria, that.criteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(criteria);
    }
}
