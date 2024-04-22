package red.jackf.whereisit.client.defaults;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import red.jackf.whereisit.api.criteria.builtin.EnchantmentCriterion;
import red.jackf.whereisit.api.criteria.builtin.PotionEffectCriterion;
import red.jackf.whereisit.client.api.events.OverlayStackBehavior;

import java.util.Set;

public class OverlayStackBehaviorDefaults {
    static void setup() {
        OverlayStackBehavior.EVENT.register((consumer, stack, alternate) -> {
            ItemEnchantments enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
            if (enchantments == null) return false;

            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                consumer.accept(new EnchantmentCriterion(entry.getKey().value(), alternate ? entry.getIntValue() : null));
            }

            return true;
        });

        OverlayStackBehavior.EVENT.register((consumer, stack, alternate) -> {
            if (alternate) return false;

            PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null || potionContents.potion().isEmpty()) return false;

            Potion potion = potionContents.potion().get().value();
            if (potion.equals(Potions.WATER)) return false;

            consumer.accept(new PotionEffectCriterion(potion));
            return true;
        });
    }
}
