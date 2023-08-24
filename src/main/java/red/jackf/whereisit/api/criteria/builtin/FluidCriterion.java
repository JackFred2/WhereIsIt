package red.jackf.whereisit.api.criteria.builtin;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

import java.util.Objects;

/**
 * Matches against a specific fluid, by targeting buckets, bottles and similar containers. Uses Fabric's Transfer API.
 */
public class FluidCriterion extends Criterion {
    public static final CriterionType<FluidCriterion> TYPE = CriterionType.of(FluidCriterion::new);
    private static final String KEY = "FluidId";
    private Fluid fluid = Fluids.EMPTY;
    public FluidCriterion() {
        super(TYPE);
    }

    public FluidCriterion(Fluid fluid) {
        super(TYPE);
        this.fluid = fluid;
    }

    @Override
    public void writeTag(CompoundTag tag) {
        tag.putString(KEY, BuiltInRegistries.FLUID.getKey(this.fluid).toString());
    }

    @Override
    public void readTag(CompoundTag tag) {
        var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(tag.getString(KEY)));
        if (fluid != Fluids.EMPTY) this.fluid = fluid;
    }

    @Override
    public boolean valid() {
        return this.fluid != Fluids.EMPTY;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean test(ItemStack stack) {
        var storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (storage == null) return false;
        for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
            var resource = view.getResource();
            if (resource.getFluid() == this.fluid) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "FluidCriterion{" +
                "fluid=" + fluid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FluidCriterion that = (FluidCriterion) o;
        return Objects.equals(fluid, that.fluid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid);
    }
}
