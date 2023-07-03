package red.jackf.whereisit.client.defaults;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import red.jackf.whereisit.api.criteria.EnchantmentCriterion;
import red.jackf.whereisit.client.api.OverlayStackBehavior;

public class OverlayStackBehaviorDefaults {
    public static void setup() {
        OverlayStackBehavior.EVENT.register((consumer, stack) -> {
            if (!stack.is(Items.ENCHANTED_BOOK)) return false;
            var enchantments = EnchantmentHelper.getEnchantments(stack);
            if (enchantments.isEmpty()) return false;
            enchantments.forEach((ench, level) -> consumer.accept(new EnchantmentCriterion(ench, null)));
            return true;
        });
    }
}
