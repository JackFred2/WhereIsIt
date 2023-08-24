package red.jackf.whereisit.api.criteria;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;

import java.util.function.Supplier;

public class CriterionType<T extends Criterion> {
    public static final ResourceKey<Registry<CriterionType<? extends Criterion>>> REGISTRY_KEY = ResourceKey.createRegistryKey(WhereIsIt.id("criteria_supplier"));
    public static final Registry<CriterionType<? extends Criterion>> REGISTRY = FabricRegistryBuilder.createSimple(REGISTRY_KEY)
            .buildAndRegister();
    private final Supplier<T> constructor;
    private final @Nullable CommandAddon commandAddon;

    public T get() {
        return constructor.get();
    }

    public void addToCommand(LiteralCommandNode<CommandSourceStack> root) {
        if (commandAddon != null) commandAddon.add(root);
    }

    public CriterionType(Supplier<T> constructor, @Nullable CommandAddon commandAddon) {
        this.constructor = constructor;
        this.commandAddon = commandAddon;
    }

    public static <T extends Criterion> CriterionType<T> of(Supplier<T> constructor, @Nullable CommandAddon commandAddon) {
        return new CriterionType<>(constructor, commandAddon);
    }

    public static <T extends Criterion> CriterionType<T> of(Supplier<T> constructor) {
        return of(constructor, null);
    }

    public interface CommandAddon {
        void add(LiteralCommandNode<CommandSourceStack> root);
    }
}
