package red.jackf.whereisit.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.criteria.Criterion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchRequest {
    private static final String ID = "Id";
    private static final String DATA = "Data";
    private final List<Criterion> criteria = new ArrayList<>();

    public SearchRequest add(Criterion criterion) {
        this.criteria.add(criterion);
        return this;
    }

    public ListTag pack() {
        var list = new ListTag();
        for (Criterion criterion : this.criteria) {
            var tag = new CompoundTag();
            tag.putString(ID, Objects.requireNonNull(Criterion.Type.REGISTRY.getKey(criterion.type)).toString());
            var data = new CompoundTag();
            criterion.writeTag(data);
            tag.put(DATA, data);
            list.add(tag);
        }
        return list;
    }

    public static SearchRequest load(ListTag list) {
        var request = new SearchRequest();
        for (Tag tag : list) {
            if (tag instanceof CompoundTag compound
                    && compound.contains(ID, Tag.TAG_STRING)
                    && compound.contains(DATA, Tag.TAG_COMPOUND)) {
                var typeId = ResourceLocation.tryParse(compound.getString(ID));
                var type = Criterion.Type.REGISTRY.get(typeId);
                if (type != null) {
                    var data = compound.getCompound(DATA);
                    var criterion = type.get();
                    criterion.readTag(data);
                    if (criterion.valid())
                        request.add(criterion);
                    else
                        WhereIsIt.LOGGER.info("Criterion data for " + typeId + "invalid: " + data);
                } else {
                    WhereIsIt.LOGGER.info("Unknown criterion: " + typeId);
                }
            } else {
                WhereIsIt.LOGGER.info("Invalid criterion tag: " + tag);
            }
        }
        return request;
    }

    public boolean test(ItemStack stack) {
        for (Criterion criterion : criteria)
            if (!criterion.test(stack)) return false;
        return true;
    }
}
