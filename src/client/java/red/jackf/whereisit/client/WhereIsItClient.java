package red.jackf.whereisit.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.client.api.SearchInvoker;
import red.jackf.whereisit.client.api.SearchRequestPopulator;
import red.jackf.whereisit.client.render.ScreenRendering;
import red.jackf.whereisit.client.render.WorldRendering;
import red.jackf.whereisit.client.util.NotificationToast;
import red.jackf.whereisit.config.WhereIsItConfig;

public class WhereIsItClient implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final KeyMapping SEARCH = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.whereisit.search", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.categories.whereisit"));

    private boolean inGame = false;
    @Nullable
    public static SearchRequest lastRequest = null;
    public static long lastSearchTime = 0;

    @Override
    public void onInitializeClient() {
        LOGGER.debug("Setup Client");

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {

			if (inGame) {
                ScreenEvents.afterRender(screen).register((screen2, graphics, mouseX1, mouseY1, tickDelta) -> ScreenRendering.render(screen2, graphics, mouseX1, mouseY1));

                // listen for keypress
                ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                    if (SEARCH.matches(key, scancode)) {
                        int mouseX = (int) (client.mouseHandler.xpos() * (double) client.getWindow()
                                .getGuiScaledWidth() / (double) client.getWindow().getScreenWidth());
                        int mouseY = (int) (client.mouseHandler.ypos() * (double) client.getWindow()
                                .getGuiScaledHeight() / (double) client.getWindow().getScreenHeight());
                        var request = new SearchRequest();
                        SearchRequestPopulator.EVENT.invoker().grabStack(request, screen1, mouseX, mouseY);
                        if (request.hasCriteria()) {
                            lastRequest = request;
                            lastSearchTime = -1;
                            WorldRendering.clearResults();
                            LOGGER.debug("Starting request: %s".formatted(request));
                            var anySucceeded = SearchInvoker.EVENT.invoker().search(request, results -> {
                                WhereIsItClient.LOGGER.debug("Search results: %s".formatted(results));
                                WorldRendering.addResults(results);
                            });
                            if (!anySucceeded) NotificationToast.sendNotInstalledOnServer();
                        }
                    }
                });
            }
        });

        // don't try to search in the main menu lmao
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> inGame = true);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> inGame = false);

        ClientTickEvents.START_WORLD_TICK.register(level -> {
            if (lastSearchTime == -1) {
                lastSearchTime = level.getGameTime();
            } else if (level.getGameTime() > lastSearchTime + WhereIsItConfig.INSTANCE.getConfig().getClient().fadeoutTimeTicks) {
                lastRequest = null;
                WorldRendering.clearResults();
            }
        });


        // mod default handlers
        SearchRequestPopulator.EVENT.register(DefaultRequestPopulator::populate);
        DefaultSearchInvoker.setup();

        WorldRendering.setup();
    }
}