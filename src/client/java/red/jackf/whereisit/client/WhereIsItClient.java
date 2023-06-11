package red.jackf.whereisit.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import red.jackf.whereisit.client.api.StackGrabber;

public class WhereIsItClient implements ClientModInitializer {
	public static final Logger LOGGER = LogUtils.getLogger();
	private static final KeyMapping SEARCH = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.whereisit.search", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.categories.whereisit"));

	@Override
	public void onInitializeClient() {
		LOGGER.debug("Setup Client");

		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) ->
			ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
				if (SEARCH.matches(key, scancode)) {
					int mouseX = (int) (client.mouseHandler.xpos() * (double)client.getWindow().getGuiScaledWidth() / (double)client.getWindow().getScreenWidth());
					int mouseY = (int) (client.mouseHandler.ypos() * (double)client.getWindow().getGuiScaledHeight() / (double)client.getWindow().getScreenHeight());
					var stack = StackGrabber.EVENT.invoker().grabStack(screen1, mouseX, mouseY);
					if (stack != null) {
						LOGGER.debug("Searching for " + stack);
					} else {
						LOGGER.debug("Empty search");
					}
				}
			})
		);

		// default
		StackGrabber.EVENT.register(DefaultGrabber::grab);
	}
}