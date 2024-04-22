package red.jackf.whereisit.api.criteria.builtin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

/**
 * A criterion matching against an enchantment (or lack of), optionally along with a target level.
 */
public class EnchantmentCriterion extends Criterion {
    public static final CriterionType<EnchantmentCriterion> TYPE = CriterionType.of(EnchantmentCriterion::new);
    private static final String ID_KEY = "EnchantmentId";
    private static final String LEVEL_KEY = "EnchantmentLevel";
    private Enchantment enchantment = null;
    private Integer targetLevel = null;

    public EnchantmentCriterion() {
        super(TYPE);
    }

    /**
     * Creates a criterion targeting a specific enchantment.
     * @param enchantment Enchantment to be checked
     * @param targetLevel Target level. If 0, then this criterion succeeds if the item is missing the enchantment.
     *                    If null, then any level matches.
     */
    public EnchantmentCriterion(Enchantment enchantment, @Nullable Integer targetLevel) {
        this();
        this.enchantment = enchantment;
        this.targetLevel = targetLevel;
    }

    @Override
    public void readTag(CompoundTag tag) {
        this.enchantment = BuiltInRegistries.ENCHANTMENT.get(ResourceLocation.tryParse(tag.getString(ID_KEY)));
        if (tag.contains(LEVEL_KEY, Tag.TAG_INT)) this.targetLevel = tag.getInt(LEVEL_KEY);
    }

    @Override
    public void writeTag(CompoundTag tag) {
        tag.putString(ID_KEY, String.valueOf(BuiltInRegistries.ENCHANTMENT.getKey(enchantment)));
        if (targetLevel != null) tag.putInt(LEVEL_KEY, targetLevel);
    }

    @Override
    public boolean valid() {
        return enchantment != null;
    }

    @Override
    public boolean test(ItemStack stack) {
        ItemEnchantments component = stack.get(EnchantmentHelper.getComponentType(stack));
        if (component == null) return false;

        int level = component.getLevel(enchantment);
        if (targetLevel != null) return targetLevel == level;
        else return level > 0;
    }

    @Override
    public String toString() {
        return "EnchantmentCriterion{" +
                "enchantment=" + enchantment +
                ", targetLevel=" + targetLevel +
                '}';
    }
}
