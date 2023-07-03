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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.client.api.SearchInvoker;
import red.jackf.whereisit.client.api.SearchRequestPopulator;
import red.jackf.whereisit.client.api.ShouldIgnoreKey;
import red.jackf.whereisit.client.defaults.OverlayStackBehaviorDefaults;
import red.jackf.whereisit.client.defaults.SearchInvokerDefaults;
import red.jackf.whereisit.client.defaults.SearchRequestPopulatorDefaults;
import red.jackf.whereisit.client.defaults.ShouldIgnoreKeyDefaults;
import red.jackf.whereisit.client.render.ScreenRendering;
import red.jackf.whereisit.client.render.WorldRendering;
import red.jackf.whereisit.client.util.NotificationToast;
import red.jackf.whereisit.client.util.TextUtil;
import red.jackf.whereisit.config.ColourScheme;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.util.ColourGetter;

public class WhereIsItClient implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final KeyMapping SEARCH = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.whereisit.search", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.categories.whereisit"));

    private boolean inGame = false;
    @Nullable
    public static SearchRequest lastRequest = null;
    public static long lastSearchTime = 0;
    public static boolean closedScreenThisSearch = false;

    private static ColourGetter getter = f -> 0; // replaced in mod init

    private static final ColourScheme[] RANDOM_CANDIDATES = new ColourScheme[] {
            ColourScheme.PRIDE,
            ColourScheme.GAY,
            ColourScheme.LESBIAN,
            ColourScheme.BISEXUAL,
            ColourScheme.PANSEXUAL,
            ColourScheme.NONBINARY,
            ColourScheme.INTERSEX,
            ColourScheme.TRANS,
            ColourScheme.ACE,
            ColourScheme.ARO,
    };

    public static void updateColourScheme() {
        if (WhereIsItConfig.INSTANCE.getConfig().getClient().randomScheme) {
            getter = RANDOM_CANDIDATES[(int) (Math.random() * RANDOM_CANDIDATES.length)].getGradient();
        } else {
            var scheme = WhereIsItConfig.INSTANCE.getConfig().getClient().colourScheme;
            if (scheme == ColourScheme.SOLID) {
                getter = f -> WhereIsItConfig.INSTANCE.getConfig().getClient().solidColour.getRGB();
            } else {
                getter = scheme.getGradient();
            }
        }
    }

    public static int getColour(float factor) {
        return getter.eval(factor);
    }

    @Override
    public void onInitializeClient() {
        LOGGER.debug("Setup Client");
        updateColourScheme();

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {

			if (inGame) {
                if (WhereIsItConfig.INSTANCE.getConfig().getClient().showSlotHighlights)
                    ScreenEvents.afterRender(screen).register((screen2, graphics, mouseX1, mouseY1, tickDelta) -> ScreenRendering.render(screen2, graphics, mouseX1, mouseY1));

                // listen for keypress
                ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                    if (SEARCH.matches(key, scancode) && !ShouldIgnoreKey.EVENT.invoker().shouldIgnoreKey()) {
                        int mouseX = (int) (client.mouseHandler.xpos() * (double) client.getWindow()
                                .getGuiScaledWidth() / (double) client.getWindow().getScreenWidth());
                        int mouseY = (int) (client.mouseHandler.ypos() * (double) client.getWindow()
                                .getGuiScaledHeight() / (double) client.getWindow().getScreenHeight());
                        var request = new SearchRequest();
                        SearchRequestPopulator.EVENT.invoker().grabStack(request, screen1, mouseX, mouseY);
                        if (request.hasCriteria()) {
                            lastRequest = request;
                            lastSearchTime = -1;
                            closedScreenThisSearch = false;
                            WorldRendering.clearResults();

                            updateColourScheme();
                            LOGGER.debug("Starting request: %s".formatted(request));

                            if (WhereIsItConfig.INSTANCE.getConfig().getClient().printSearchRequestsInChat && Minecraft.getInstance().player != null) {
                                var text = TextUtil.prettyPrint(request.pack());
                                for (Component component : text) {
                                    Minecraft.getInstance().player.sendSystemMessage(component);
                                }
                            }

                            var anySucceeded = SearchInvoker.EVENT.invoker().search(request, results -> {
                                WhereIsItClient.LOGGER.debug("Search results: %s".formatted(results));
                                if (WhereIsItConfig.INSTANCE.getConfig().getClient().closeGuiOnFoundResults && !closedScreenThisSearch) {
                                    closedScreenThisSearch = true;
                                    Minecraft.getInstance().setScreen(null);
                                }
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
        SearchRequestPopulator.EVENT.register(SearchRequestPopulatorDefaults::populate);
        OverlayStackBehaviorDefaults.setup();
        SearchInvokerDefaults.setup();
        ShouldIgnoreKeyDefaults.setup();

        WorldRendering.setup();
    }
}