package red.jackf.whereisit.api;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.criteria.Criterion;
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
    public static final Codec<SearchRequest> CODEC = Criterion.CODEC.listOf().xmap(SearchRequest::new, req -> req.criteria);

    public static final String ID = "Id";
    private final List<Criterion> criteria;

    public SearchRequest() {
        this.criteria = new ArrayList<>();
    }

    public SearchRequest(List<Criterion> criteria) {
        this.criteria = Lists.newArrayList(criteria);
    }

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
     * @return Whether this request has any criteria.
     */
    public boolean hasCriteria() {
        return !criteria.isEmpty();
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
            WhereIsIt.LOGGER.warn("Invalid criterion: " + criterion);
        }
    }

    /**
     * Serialises this request into a Tag for debugging purposes.
     *
     * @return Search request formatted into a Tag
     */
    public ListTag toTag() {
        var encoded = CODEC.encodeStart(NbtOps.INSTANCE, this);
        if (encoded.isSuccess()) {
            Tag tag = encoded.getOrThrow();
            if (tag instanceof ListTag listTag) {
                return listTag;
            }
        }
        return new ListTag();
    }

    /**
     * Test all of this request's criterion against a given ItemStack. Returns false if any criterion fail. If no criterion,
     * return true as default.
     * @param stack Stack to test against
     * @return If no criterion fail against the itemstack.
     */
    @ApiStatus.Internal
    private boolean test(ItemStack stack) {
        for (Criterion criterion : criteria)
            if (!criterion.test(stack)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SearchRequest[" + criteria.stream().map(Criterion::toString).collect(Collectors.joining(", ")) + "]";
    }
}
