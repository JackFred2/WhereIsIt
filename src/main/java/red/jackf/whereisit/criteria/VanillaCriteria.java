package red.jackf.whereisit.criteria;

import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.criteria.*;

public abstract class VanillaCriteria {
    public static final Criterion.Type<ItemCriterion> ITEM = Criterion.register(WhereIsIt.id("item_id"), ItemCriterion::new);
    public static final Criterion.Type<NbtCriterion> NBT = Criterion.register(WhereIsIt.id("nbt"), NbtCriterion::new);
    public static final Criterion.Type<EnchantmentCriterion> ENCHANTMENT = Criterion.register(WhereIsIt.id("enchantment"), EnchantmentCriterion::new);
    public static final Criterion.Type<PotionEffectCriterion> POTION_EFFECT = Criterion.register(WhereIsIt.id("potion_effect"), PotionEffectCriterion::new);
    public static final Criterion.Type<ItemTagCriterion> TAG = Criterion.register(WhereIsIt.id("item_tag"), ItemTagCriterion::new);
    public static final Criterion.Type<FluidCriterion> FLUID = Criterion.register(WhereIsIt.id("fluid_id"), FluidCriterion::new);
    public static final Criterion.Type<NameCriterion> NAME = Criterion.register(WhereIsIt.id("name"), NameCriterion::new);

    public static final Criterion.Type<AnyOfCriterion> ANY_OF = Criterion.register(WhereIsIt.id("any_of"), AnyOfCriterion::new);
    public static final Criterion.Type<AllOfCriterion> ALL_OF = Criterion.register(WhereIsIt.id("all_of"), AllOfCriterion::new);
    public static void setup() {

    }
}
