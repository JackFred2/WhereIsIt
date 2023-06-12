package red.jackf.whereisit.api.criteria;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.WhereIsIt;

/**
 * A test for an ItemStack. Register a supplier to {@link #register(ResourceLocation, Type)}
 */
public abstract class Criterion {
    public final Type<?> type;

    public Criterion(Type<?> type) {
        this.type = type;
    }
    /**
     * Write this criterion's data to a tag for network serialization; ran on client side.
     * @param tag Tag to write to
     */
    public void writeTag(CompoundTag tag) {}

    /**
     * Load this criterion's data from a tag. Ran on the server side.
     *
     * @param tag Tag to read from
     */
    public void readTag(CompoundTag tag) {}

    /**
     * @return If this criterion has valid data.
     */
    public boolean valid() {
        return true;
    }

    /**
     * Test against a given ItemStack. This will only be called if {@link Criterion#readTag(CompoundTag)} returns true.
     * @param stack Stack to test again with loaded data
     * @return If this criterion matches the stack.
     */
    public abstract boolean test(ItemStack stack);

    public static <T extends Criterion> Type<T> register(ResourceLocation id, Type<T> type) {
        return Registry.register(Type.REGISTRY, id, type);
    }

    public interface Type<T extends Criterion> {
        ResourceKey<Registry<Type<? extends Criterion>>> REGISTRY_KEY = ResourceKey.createRegistryKey(WhereIsIt.id("criteria_supplier"));
        Registry<Type<? extends Criterion>> REGISTRY = FabricRegistryBuilder.createSimple(REGISTRY_KEY).buildAndRegister();

        T get();
    }
}
