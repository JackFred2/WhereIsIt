package red.jackf.whereisit.api.criteria;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import red.jackf.whereisit.WhereIsIt;

public record CriterionType<T extends Criterion>(MapCodec<T> codec) {
    public static final ResourceKey<Registry<CriterionType<? extends Criterion>>> REGISTRY_KEY = ResourceKey.createRegistryKey(WhereIsIt.id("criteria_supplier"));
    public static final Registry<CriterionType<? extends Criterion>> REGISTRY = FabricRegistryBuilder.createSimple(REGISTRY_KEY).buildAndRegister();

    public static <T extends Criterion> CriterionType<T> of(MapCodec<T> codec) {
        return new CriterionType<>(codec);
    }
}
