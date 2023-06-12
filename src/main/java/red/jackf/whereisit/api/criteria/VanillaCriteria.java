package red.jackf.whereisit.api.criteria;

import red.jackf.whereisit.WhereIsIt;

public abstract class VanillaCriteria {
    public static Criterion.Type<ItemCriterion> ITEM = Criterion.register(WhereIsIt.id("item_id"), ItemCriterion::new);
    public static Criterion.Type<TagCriterion> TAG = Criterion.register(WhereIsIt.id("in_tag"), TagCriterion::new);
    public static void setup() {

    }
}
