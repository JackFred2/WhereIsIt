package red.jackf.whereisit.api.criteria.builtin;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

/**
 * Matches against a specific fluid, by targeting buckets, bottles and similar containers. Uses Fabric's Transfer API.
 */
public record FluidCriterion(Fluid fluid) implements Criterion {
    public static final MapCodec<FluidCriterion> CODEC = BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").xmap(FluidCriterion::new, FluidCriterion::fluid);
    public static final CriterionType<FluidCriterion> TYPE = CriterionType.of(CODEC);

    @Override
    public CriterionType<?> type() {
        return TYPE;
    }

    @Override
    public boolean valid() {
        return this.fluid != Fluids.EMPTY;
    }

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
}
