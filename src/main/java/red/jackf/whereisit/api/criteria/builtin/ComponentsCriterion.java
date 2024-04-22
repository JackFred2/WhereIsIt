package red.jackf.whereisit.api.criteria.builtin;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

public class ComponentsCriterion extends Criterion {
    public static final CriterionType<ComponentsCriterion> TYPE = CriterionType.of(ComponentsCriterion::new);
    private static final String TAG_KEY = "Components";

    private DataComponentPatch components = DataComponentPatch.EMPTY;

    public ComponentsCriterion() {
        super(TYPE);
    }

    public ComponentsCriterion(ItemStack stack) {
        this();

        this.components = stack.getComponentsPatch();
    }

    @Override
    public void readTag(CompoundTag tag) {
        Tag componentsTag = tag.get(TAG_KEY);
        if (componentsTag != null)
            DataComponentPatch.CODEC.decode(NbtOps.INSTANCE, componentsTag).ifSuccess(pair -> this.components = pair.getFirst());
    }

    @Override
    public void writeTag(CompoundTag tag) {
        DataComponentPatch.CODEC.encodeStart(NbtOps.INSTANCE, this.components).ifSuccess(componentsTag -> tag.put(TAG_KEY, componentsTag));
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.getComponentsPatch().equals(this.components);
    }
}
