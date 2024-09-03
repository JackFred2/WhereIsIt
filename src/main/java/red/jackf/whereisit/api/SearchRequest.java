package red.jackf.whereisit.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;
import red.jackf.whereisit.api.criteria.builtin.AllOfCriterion;
import red.jackf.whereisit.api.search.NestedItemsGrabber;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a request to search for an item.
 */
public class SearchRequest implements Consumer<Criterion> {
    public static final String ID = "Id";
    public static final String DATA = "Data";
    private final List<Criterion> criteria = new ArrayList<>();

    /**
     * Perform a check on an ItemStack with the given request. Use this method to correctly handle nested items.
     * @param stack ItemStack to test against
     * @param request Search Request to test with
     * @return Whether this ItemStack or any sub-items if applicable matches the request
     */
    public static boolean check(ItemStack stack, SearchRequest request) {
        if (request.test(stack)) return true;

        if (WhereIsItConfig.INSTANCE.instance().getCommon().doNestedSearch) {
            var nested = NestedItemsGrabber.get(stack);

            return nested.anyMatch(request::test);
        }

        return false;
    }

    /**
     * Adds a new criterion to this request. The criterion is checked for validity before being added.
     * @param criterion The criterion to add to this request
     */
    public void accept(Criterion criterion) {
        if (criterion.valid()) {
            if (criterion instanceof AllOfCriterion allOfCriterion) {
                criteria.addAll(allOfCriterion.criteria);
            } else {
                this.criteria.add(criterion);
            }
        } else {
            var data = new CompoundTag();
            criterion.writeTag(data);
            WhereIsIt.LOGGER.warn("Criterion data for " + CriterionType.REGISTRY.getKey(criterion.type) + " invalid: " + data);
        }
    }

    /**
     * @return Whether this request has any criteria.
     */
    public boolean hasCriteria() {
        return !criteria.isEmpty();
    }

    /**
     * Serializes an individual criterion into a compound tag. Null if not registered to {@link CriterionType#REGISTRY}.
     * @param criterion Criterion to serialize
     * @return Serialized criterion, or null if not registered.
     */
    @Nullable
    public static CompoundTag toTag(Criterion criterion) {
        var type = CriterionType.REGISTRY.getKey(criterion.type);
        if (type == null) return null;
        var tag = new CompoundTag();
        tag.putString(ID, type.toString());
        var data = new CompoundTag();
        criterion.writeTag(data);
        tag.put(DATA, data);
        return tag;
    }

    /**
     * Serailizes this request into a Compound Tag.
     * @return Serialized search request
     */
    public CompoundTag pack() {
        var list = new ListTag();
        for (Criterion criterion : this.criteria) {
            var tag = toTag(criterion);
            if (tag != null)
                list.add(tag);
        }
        var tag = new CompoundTag();
        tag.put(DATA, list);
        return tag;
    }

    /**
     * Load an individual criterion from a compound tag. Null if invalid or unknown.
     * @param criterionTag Compound tag to read the criterion's data from
     * @return Deserialized criterion, or null if unknown or an error occured.
     */
    @Nullable
    public static Criterion fromTag(CompoundTag criterionTag) {
        var typeId = ResourceLocation.tryParse(criterionTag.getString(ID));
        var type = CriterionType.REGISTRY.get(typeId);
        if (type != null) {
            var data = criterionTag.getCompound(DATA);
            var criterion = type.get();
            criterion.readTag(data);
            if (criterion.valid())
                return criterion;
            else
                WhereIsIt.LOGGER.warn("Criterion data for " + typeId + " invalid: " + data);
        } else {
            WhereIsIt.LOGGER.warn("Unknown criterion: " + typeId);
        }

        return null;
    }

    /**
     * Load a search request from a Compound Tag.
     * @param root Compound tag to read from
     * @return Constructed search request. If an error occcurs, an empty request is returned which is not checked.
     */
    public static SearchRequest load(CompoundTag root) {
        try {
            var request = new SearchRequest();
            if (root.contains(DATA, Tag.TAG_LIST)) {
                var list = root.getList(DATA, Tag.TAG_COMPOUND);
                for (Tag tag : list) {
                    if (tag instanceof CompoundTag compound
                            && compound.contains(ID, Tag.TAG_STRING)
                            && compound.contains(DATA, Tag.TAG_COMPOUND)) {
                        var criterion = fromTag(compound);
                        if (criterion != null)
                            request.accept(criterion);
                    } else {
                        WhereIsIt.LOGGER.warn("Invalid criterion tag: " + tag);
                    }
                }
            } else {
                WhereIsIt.LOGGER.warn("No data for search request");
            }
            return request;
        } catch (Exception ex) {
            WhereIsIt.LOGGER.error("Error decoding search request", ex);
            return new SearchRequest();
        }
    }

    /**
     * Test all of this request's criterion against a given ItemStack. Returns false if any criterion fail. If no criterion,
     * return true as default.
     * @param stack Stack to test against
     * @return If no criterion fail against the itemstack.
     */
    @ApiStatus.Internal
    public boolean test(ItemStack stack) {
        for (Criterion criterion : criteria)
            if (!criterion.test(stack)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SearchRequest[" + criteria.stream().map(Criterion::toString).collect(Collectors.joining(", ")) + "]";
    }
}
