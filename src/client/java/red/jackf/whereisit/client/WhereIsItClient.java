package red.jackf.whereisit.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.client.api.SearchRequestPopulator;
import red.jackf.whereisit.client.util.NotificationToast;
import red.jackf.whereisit.networking.ClientboundPositionPacket;
import red.jackf.whereisit.networking.ServerboundSearchForItemPacket;

public class WhereIsItClient implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final KeyMapping SEARCH = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.whereisit.search", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.categories.whereisit"));

    private boolean inGame = false;

    @Override
    public void onInitializeClient() {
        LOGGER.debug("Setup Client");

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (inGame) ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                if (!ClientPlayNetworking.canSend(ServerboundSearchForItemPacket.TYPE)) {
                    NotificationToast.sendNotInstalledOnServer();
                    return;
                }
                if (SEARCH.matches(key, scancode)) {
                    int mouseX = (int) (client.mouseHandler.xpos() * (double) client.getWindow()
                            .getGuiScaledWidth() / (double) client.getWindow().getScreenWidth());
                    int mouseY = (int) (client.mouseHandler.ypos() * (double) client.getWindow()
                            .getGuiScaledHeight() / (double) client.getWindow().getScreenHeight());
                    var request = new SearchRequest();
                    SearchRequestPopulator.EVENT.invoker().grabStack(request, screen1, mouseX, mouseY);
                    if (request.hasCriteria()) {
                        LOGGER.debug(request.toString());
                        ClientPlayNetworking.send(new ServerboundSearchForItemPacket(request));
                    }
                }
            });
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> inGame = true);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> inGame = false);

        // default
        SearchRequestPopulator.EVENT.register(DefaultRequestPopulator::populate);

        ClientPlayNetworking.registerGlobalReceiver(ClientboundPositionPacket.TYPE, (packet, player, responseSender) -> {

        });
    }
}