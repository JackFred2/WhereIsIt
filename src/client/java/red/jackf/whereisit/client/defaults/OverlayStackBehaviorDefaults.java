package red.jackf.whereisit.client.defaults;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import red.jackf.whereisit.api.criteria.builtin.EnchantmentCriterion;
import red.jackf.whereisit.api.criteria.builtin.PotionEffectCriterion;
import red.jackf.whereisit.client.api.events.OverlayStackBehavior;

import java.util.Set;

public class OverlayStackBehaviorDefaults {
    static void setup() {
        OverlayStackBehavior.EVENT.register((consumer, stack, alternate) -> {
            if (!stack.is(Items.ENCHANTED_BOOK)) return false;
            var enchantments = EnchantmentHelper.getEnchantments(stack);
            if (enchantments.isEmpty()) return false;
            enchantments.forEach((ench, level) -> consumer.accept(new EnchantmentCriterion(ench, alternate ? level : null)));
            return true;
        });

        final Set<Item> POTION_ITEMS = Set.of(
                Items.POTION,
                Items.SPLASH_POTION,
                Items.LINGERING_POTION,
                Items.TIPPED_ARROW
        );

        OverlayStackBehavior.EVENT.register((consumer, stack, alternate) -> {
            if (alternate) return false;
            if (!POTION_ITEMS.contains(stack.getItem())) return false;
            var potion = PotionUtils.getPotion(stack);
            if (potion == Potions.WATER) return false;
            consumer.accept(new PotionEffectCriterion(potion));
            return true;
        });
    }
}
