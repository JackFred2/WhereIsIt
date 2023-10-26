package red.jackf.whereisit.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class WhereIsItConfig {
    public static final ConfigClassHandler<WhereIsItConfig> INSTANCE
            = ConfigClassHandler.createBuilder(WhereIsItConfig.class)
                .id(WhereIsIt.id("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("whereisit.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SuppressWarnings("FieldMayBeFinal")
    @SerialEntry(nullable = true, required = false)
    @Nullable
    private Client client = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? new Client() : null;

    @SuppressWarnings("FieldMayBeFinal")
    @SerialEntry
    private Common common = new Common();

    @SuppressWarnings("FieldMayBeFinal")
    @SerialEntry(nullable = true, required = false)
    @Nullable
    private Server server = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER ? new Server() : null;

    public static void cleanupOldConfig() {
        try {
            var oldPath = FabricLoader.getInstance().getConfigDir().resolve("whereisit.json");
            var newPath = FabricLoader.getInstance().getConfigDir().resolve("whereisit.json5");
            if (Files.isRegularFile(oldPath) && !Files.exists(newPath)) {
                WhereIsIt.LOGGER.info("Think we're first load of new version, removing old config file");
                Files.delete(oldPath);
            }
        } catch (IOException ignored) {}
    }

    public void validate() {
        if (this.client != null) this.client.validate();
        this.common.validate();
    }

    public Client getClient() {
        if (client != null) return client;
        throw new AssertionError("Attempted to get client config on dedicated server");
    }

    public Server getServer() {
        if (server != null) return server;
        throw new AssertionError("Attempted to get server config on client");
    }

    public Common getCommon() {
        return common;
    }

    public static class Client {
        @SerialEntry(comment = "When no GUI is open, whether to search using the item in your hand when pressing the keybind.")
        public boolean searchUsingItemInHand = false;

        @SerialEntry(comment = "Play sound on request trigger.")
        public boolean playSoundOnRequest = false;

        @SerialEntry(comment = "Whether the currently open GUI will close if results were found. Will only happen once per search.")
        public boolean closeGuiOnFoundResults = true;

        @SerialEntry(comment = "Whether to highlight slots that matched the last search.")
        public boolean showSlotHighlights = true;

        @SerialEntry(comment = "How much the mouse's position influences the colour of a slot highlight. Range: [0, 4]")
        public float slotHighlightMouseFactor = 1f;

        @SerialEntry(comment = "How much a slot's X position influences the colour of a slot highlight. Range: [0, 4]")
        public float slotHighlightXFactor = 1f;

        @SerialEntry(comment = "Modifier for how fast Where Is It will cycle through the current colour scheme's gradient. Range: [0.1, 4]")
        public float highlightTimeFactor = 1f;

        @SerialEntry(comment = "Show Container Names for Results.")
        public boolean showContainerNamesInResults = true;

        @SerialEntry(comment = "Visual scale of the container names in the range [0.25, 2].")
        public float containerNameLabelScale = 1f;

        @SerialEntry(comment = "Whether to use a random pride colour scheme each search.")
        public boolean randomScheme = true;

        @SerialEntry(comment = "Highlight Colour Scheme. One of SOLID, PRIDE, GAY, LESBIAN, BISEXUAL, PANSEXUAL, INTERSEX, NONBINARY, TRANS, ACE, ARO, BRITISH")
        public ColourScheme colourScheme = ColourScheme.PRIDE;

        @SerialEntry(comment = "Highlight colour to use when colourScheme is SOLID.")
        public Color solidColour = new Color(0xFFBADA55);

        @SerialEntry(comment = "Client-sided compatibility options")
        public Compatibility compatibility = new Compatibility();

        public static class Compatibility {
            @SerialEntry(comment = "Whether to enable grabbing items/tags from the vanilla recipe book.")
            public boolean recipeBookSupport = true;

            @SerialEntry(comment = "Whether to enable grabbing items/tags from JEI.")
            public boolean jeiSupport = true;

            @SerialEntry(comment = "Whether to enable grabbing items/tags from REI.")
            public boolean reiSupport = true;

            @SerialEntry(comment = "Whether to enable grabbing items/tags from EMI.")
            public boolean emiSupport = true;
        }

        @SerialEntry(comment = "Client-sided debug options")
        public Debug debug = new Debug();

        public static class Debug {
            @SerialEntry(comment = "DEBUG: print search requests in local chat.")
            public boolean printSearchRequestsInChat = false;
        }

        public void validate() {
            this.slotHighlightMouseFactor = Mth.clamp(this.slotHighlightMouseFactor, 0f, 4f);
            this.slotHighlightXFactor = Mth.clamp(this.slotHighlightXFactor, 0f, 4f);
            this.highlightTimeFactor = Mth.clamp(this.slotHighlightXFactor, 0.1f, 4f);
            this.solidColour = new Color(this.solidColour.getRGB() | 0xFF_000000);
            this.containerNameLabelScale = Mth.clamp(this.containerNameLabelScale, 0.25f, 2f);
        }
    }

    public static class Common {
        @SerialEntry(comment = "Radius from the player's position to search for items. In the range [4, 24].")
        public int searchRangeBlocks = 8;

        @SerialEntry(comment = "Whether to search within certain items that contain other items, such as Shulker Boxes or Bundles.")
        public boolean doNestedSearch = true;

        @SerialEntry(comment = "A list of aliases for the search command. Disable by removing all options.")
        public List<String> commandAliases = new ArrayList<>(commandAliasesDefault);
        private static final List<String> commandAliasesDefault = List.of(
                "whereis"
        );

        @SerialEntry(comment = "How long it takes world and slot highlights to fade out. Applies to both local regular highlights and server-side highlights.")
        public int fadeoutTimeTicks = 15 * TICKS_PER_SECOND;

        @SerialEntry(comment = "Common debug options")
        public Debug debug = new Debug();

        public static class Debug {
            @SerialEntry(comment = "DEBUG: Whether to enable the mod's block searchers for ender chests, and the fallback using Fabric's Transfer API.")
            public boolean enableDefaultSearchers = true;

            @SerialEntry(comment = "DEBUG: Whether to print search times in chat. Applies to all online players if on a server.")
            public boolean printSearchTime = false;

            @SerialEntry(comment = "DEBUG: Whether to force all requests to use server-side highlighting, instead of just clients without Where Is It. This may disable features of other mods that can ask for search results, but may have better rendering compatibility.")
            public boolean forceServerSideHighlightsOnly = false;
        }

        public void validate() {
            this.searchRangeBlocks = Mth.clamp(this.searchRangeBlocks, 4, 24);
            this.fadeoutTimeTicks = Mth.clamp(this.fadeoutTimeTicks, 5 * TICKS_PER_SECOND, 30 * TICKS_PER_SECOND);
        }
    }

    public static class Server {
        @SerialEntry(comment = "Whether to enable the server-side rate limit (3 every 5 seconds).")
        public boolean rateLimit = true;
    }
}
