package red.jackf.whereisit.api.criteria;

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
import red.jackf.whereisit.criteria.VanillaCriteria;

public class FluidCriterion extends Criterion {
    private static final String KEY = "FluidId";
    private Fluid fluid = Fluids.EMPTY;
    public FluidCriterion() {
        super(VanillaCriteria.FLUID);
    }

    public FluidCriterion(Fluid fluid) {
        this();
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
}
