package red.jackf.randomadditions;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.util.InputUtil;

import static red.jackf.randomadditions.RandomAdditions.id;

public class RandomAdditionsClient implements ClientModInitializer {

    public static final FabricKeyBinding FIND_ITEMS = FabricKeyBinding.Builder.create(
                    id("key.find_items"),
                    InputUtil.Type.KEYSYM,
                    84,
                    "key.categories.inventory"
               ).build();

    @Override
    public void onInitializeClient() {
        KeyBindingRegistry.INSTANCE.register(FIND_ITEMS);
    }
}
