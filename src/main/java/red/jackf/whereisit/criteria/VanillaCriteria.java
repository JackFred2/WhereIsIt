package red.jackf.whereisit.criteria;

import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.criteria.*;

public abstract class VanillaCriteria {
    public static final Criterion.Type<ItemCriterion> ITEM = Criterion.register(WhereIsIt.id("item_id"), ItemCriterion::new);
    public static final Criterion.Type<TagCriterion> TAG = Criterion.register(WhereIsIt.id("in_tag"), TagCriterion::new);
    public static final Criterion.Type<FluidCriterion> FLUID = Criterion.register(WhereIsIt.id("fluid"), FluidCriterion::new);

    public static final Criterion.Type<AnyOfCriterion> ANY_OF = Criterion.register(WhereIsIt.id("any_of"), AnyOfCriterion::new);
    public static void setup() {

    }
}
