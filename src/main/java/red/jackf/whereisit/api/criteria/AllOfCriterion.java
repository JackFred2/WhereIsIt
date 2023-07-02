package red.jackf.whereisit.api.criteria;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.criteria.VanillaCriteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Acts as an 'AND' gate for a list of criterion. Create this using {@link AllOfCriterion (Collection)}, then (recommended)
 * {@link AllOfCriterion#compact()} before you add it to a search request.
 */
public class AllOfCriterion extends Criterion implements Consumer<Criterion> {
    private static final String KEY = "AllOf";
    private final List<Criterion> criteria = new ArrayList<>();
    public AllOfCriterion() {
        super(VanillaCriteria.ALL_OF);
    }

    public AllOfCriterion(Collection<Criterion> criteria) {
        this();
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
        return criteria.size() > 0 && criteria.stream().allMatch(Criterion::valid);
    }

    @Override
    public boolean test(ItemStack stack) {
        return criteria.stream().allMatch(c -> c.test(stack));
    }

    @Override
    public void accept(Criterion criterion) {
        this.criteria.add(criterion);
    }

    @Override
    public String toString() {
        return "AllOfCriterion[" +
                criteria.stream().map(Criterion::toString).collect(Collectors.joining(", ")) +
                "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllOfCriterion that = (AllOfCriterion) o;
        return Objects.equals(criteria, that.criteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(criteria);
    }
}
