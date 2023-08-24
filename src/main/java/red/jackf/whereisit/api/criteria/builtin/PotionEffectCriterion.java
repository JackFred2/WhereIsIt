package red.jackf.whereisit.api.criteria.builtin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import red.jackf.whereisit.api.criteria.Criterion;
import red.jackf.whereisit.api.criteria.CriterionType;

public class PotionEffectCriterion extends Criterion {
    public static final CriterionType<PotionEffectCriterion> TYPE = CriterionType.of(PotionEffectCriterion::new);
    private static final String KEY = "PotionId";
    private Potion potion;

    public PotionEffectCriterion() {
        super(TYPE);
    }

    public PotionEffectCriterion(Potion potion) {
        this();
        this.potion = potion;
    }

    @Override
    public void writeTag(CompoundTag tag) {
        tag.putString(KEY, BuiltInRegistries.POTION.getKey(potion).toString());
    }

    @Override
    public void readTag(CompoundTag tag) {
        this.potion = BuiltInRegistries.POTION.get(ResourceLocation.tryParse(tag.getString(KEY)));
    }

    @Override
    public boolean valid() {
        return potion != null;
    }

    @Override
    public boolean test(ItemStack stack) {
        return PotionUtils.getPotion(stack) == potion;
    }

    @Override
    public String toString() {
        return "PotionEffectCriterion{" +
                "potion=" + potion +
                '}';
    }
}
