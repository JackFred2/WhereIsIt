package red.jackf.whereisit.api.criteria;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import red.jackf.whereisit.WhereIsIt;

import java.util.function.Supplier;

public class CriterionType<T extends Criterion> {
    public static final ResourceKey<Registry<CriterionType<? extends Criterion>>> REGISTRY_KEY = ResourceKey.createRegistryKey(WhereIsIt.id("criteria_supplier"));
    public static final Registry<CriterionType<? extends Criterion>> REGISTRY = FabricRegistryBuilder.createSimple(REGISTRY_KEY)
            .buildAndRegister();
    private final Supplier<T> constructor;

    public T get() {
        return constructor.get();
    }

    private CriterionType(Supplier<T> constructor) {
        this.constructor = constructor;
    }

    public static <T extends Criterion> CriterionType<T> of(Supplier<T> constructor) {
        return new CriterionType<>(constructor);
    }
}
