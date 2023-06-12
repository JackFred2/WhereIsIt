package red.jackf.whereisit.criteria;

import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.ItemCriterion;
import red.jackf.whereisit.api.criteria.TagCriterion;

public abstract class VanillaCriteria {
    public static final Criterion.Type<ItemCriterion> ITEM = Criterion.register(WhereIsIt.id("item_id"), ItemCriterion::new);
    public static final Criterion.Type<TagCriterion> TAG = Criterion.register(WhereIsIt.id("in_tag"), TagCriterion::new);
    public static void setup() {

    }
}
