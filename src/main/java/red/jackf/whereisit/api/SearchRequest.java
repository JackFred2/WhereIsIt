package red.jackf.whereisit.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.criteria.Criterion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SearchRequest implements Consumer<Criterion> {
    private static final String ID = "Id";
    private static final String DATA = "Data";
    private final List<Criterion> criteria = new ArrayList<>();

    public void add(Criterion criterion) {
        if (criterion.valid()) {
            this.criteria.add(criterion);
        } else {
            var data = new CompoundTag();
            criterion.writeTag(data);
            WhereIsIt.LOGGER.warn("Criterion data for " + Criterion.Type.REGISTRY.getKey(criterion.type) + " invalid: " + data);
        }
    }

    public boolean hasCriteria() {
        return !criteria.isEmpty();
    }

    /**
     * Serializes a criterion into a compound tag. Null if invalid.
     */
    @Nullable
    public static CompoundTag toTag(Criterion criterion) {
        var type = Criterion.Type.REGISTRY.getKey(criterion.type);
        if (type == null) return null;
        var tag = new CompoundTag();
        tag.putString(ID, type.toString());
        var data = new CompoundTag();
        criterion.writeTag(data);
        tag.put(DATA, data);
        return tag;
    }

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
     * Load a criterion from a compound tag. Null if invalid or unknown.
     */
    @Nullable
    public static Criterion fromTag(CompoundTag criterionTag) {
        var typeId = ResourceLocation.tryParse(criterionTag.getString(ID));
        var type = Criterion.Type.REGISTRY.get(typeId);
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

    public static SearchRequest load(CompoundTag root) {
        var request = new SearchRequest();
        if (root.contains(DATA, Tag.TAG_LIST)) {
            var list = root.getList(DATA, Tag.TAG_COMPOUND);
            for (Tag tag : list) {
                if (tag instanceof CompoundTag compound
                        && compound.contains(ID, Tag.TAG_STRING)
                        && compound.contains(DATA, Tag.TAG_COMPOUND)) {
                    var criterion = fromTag(compound);
                    if (criterion != null)
                        request.add(criterion);
                } else {
                    WhereIsIt.LOGGER.warn("Invalid criterion tag: " + tag);
                }
            }
        } else {
            WhereIsIt.LOGGER.warn("No data for search request");
        }
        return request;
    }

    public boolean test(ItemStack stack) {
        for (Criterion criterion : criteria)
            if (!criterion.test(stack)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SearchRequest[" + criteria.stream().map(Criterion::toString).collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public void accept(Criterion criterion) {
        this.add(criterion);
    }
}
