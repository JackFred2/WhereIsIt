package red.jackf.whereisit.config;

import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class WhereIsItConfig {
    public static final GsonConfigInstance<WhereIsItConfig> INSTANCE
            = GsonConfigInstance.createBuilder(WhereIsItConfig.class)
                .setPath(FabricLoader.getInstance().getConfigDir().resolve("whereisit.json"))
                .overrideGsonBuilder(WhereIsItGSON.get())
                .build();

    @SuppressWarnings("FieldMayBeFinal")
    @ConfigEntry
    @Nullable
    private Client client = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? new Client() : null;

    @SuppressWarnings("FieldMayBeFinal")
    @ConfigEntry
    private Common common = new Common();

    @SuppressWarnings("FieldMayBeFinal")
    @ConfigEntry
    @Nullable
    private Server server = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER ? new Server() : null;

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
        @ConfigEntry
        public boolean searchUsingItemInHand = false;

        @ConfigEntry
        public boolean playSoundOnRequest = false;

        @ConfigEntry
        public boolean closeGuiOnFoundResults = true;

        @ConfigEntry
        public boolean showSlotHighlights = true;

        @ConfigEntry
        public boolean showContainerNamesInResults = true;

        @ConfigEntry
        public float containerNameLabelScale = 1f;

        @ConfigEntry
        public boolean randomScheme = true;

        @ConfigEntry
        public ColourScheme colourScheme = ColourScheme.PRIDE;

        @ConfigEntry
        public Color solidColour = new Color(0xFFBADA55);

        @ConfigEntry
        public boolean printSearchRequestsInChat = false;

        public void validate() {
            this.solidColour = new Color(this.solidColour.getRGB() | 0xFF_000000);
            this.containerNameLabelScale = Mth.clamp(this.containerNameLabelScale, 0.25f, 2f);
        }

        @ConfigEntry
        public Compatibility compatibility = new Compatibility();

        public static class Compatibility {
            @ConfigEntry
            public boolean recipeBookSupport = true;
            @ConfigEntry
            public boolean jeiSupport = true;
            @ConfigEntry
            public boolean reiSupport = true;
            @ConfigEntry
            public boolean emiSupport = true;
        }
    }

    public static class Common {
        @ConfigEntry
        public int searchRangeBlocks = 8;

        @ConfigEntry
        public boolean doNestedSearch = true;

        @ConfigEntry
        public boolean enableDefaultSearchers = true;

        @ConfigEntry
        public boolean printSearchTime = false;

        @ConfigEntry
        public List<String> commandAliases = new ArrayList<>(commandAliasesDefault);
        private static final List<String> commandAliasesDefault = List.of(
                "whereis"
        );

        @ConfigEntry
        public int fadeoutTimeTicks = 15 * TICKS_PER_SECOND;

        @ConfigEntry
        public boolean forceServerSideHighlightsOnly = false;

        public void validate() {
            this.searchRangeBlocks = Mth.clamp(this.searchRangeBlocks, 4, 24);
            this.fadeoutTimeTicks = Mth.clamp(this.fadeoutTimeTicks, 5 * TICKS_PER_SECOND, 30 * TICKS_PER_SECOND);
        }
    }

    public static class Server {
        @ConfigEntry
        public boolean rateLimit = true;
    }
}
