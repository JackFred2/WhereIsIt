package red.jackf.whereisit.defaults;

import net.minecraft.core.Registry;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.criteria.CriterionType;
import red.jackf.whereisit.api.criteria.builtin.*;

public class BuiltInCriteria {
    private static void register(String path, CriterionType<?> type) {
        Registry.register(CriterionType.REGISTRY, WhereIsIt.id(path), type);
    }

    public static void setup() {
        register("all_of", AllOfCriterion.TYPE);
        register("any_of", AnyOfCriterion.TYPE);
        register("enchantment", EnchantmentCriterion.TYPE);
        register("fluid", FluidCriterion.TYPE);
        register("item", ItemCriterion.TYPE);
        register("item_tag", ItemTagCriterion.TYPE);
        register("name", NameCriterion.TYPE);
        register("nbt", NbtCriterion.TYPE);
        register("potion_effect", PotionEffectCriterion.TYPE);
    }
}
