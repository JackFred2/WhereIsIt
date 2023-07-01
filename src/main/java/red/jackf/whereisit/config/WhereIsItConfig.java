package red.jackf.whereisit.config;

import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.util.ColourGetter;

import java.awt.*;

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

    public void validate() {
        if (this.client != null) this.client.validate();
        this.common.validate();
    }

    public Client getClient() {
        if (client != null) return client;
        throw new AssertionError("Attempted to get client config on dedicated server");
    }

    public Common getCommon() {
        return common;
    }

    public static class Client {
        @ConfigEntry
        public int fadeoutTimeTicks = 10 * TICKS_PER_SECOND;

        @ConfigEntry
        public boolean showSlotHighlights = true;

        @ConfigEntry
        public boolean randomScheme = true;

        @ConfigEntry
        public ColourScheme colourScheme = ColourScheme.PRIDE;

        @ConfigEntry
        public Color solidColour = new Color(0xFFBADA55);

        public void validate() {
            this.fadeoutTimeTicks = Mth.clamp(this.fadeoutTimeTicks, 5 * TICKS_PER_SECOND, 30 * TICKS_PER_SECOND);
        }
    }

    public static class Common {
        @ConfigEntry
        public int searchRangeBlocks = 8;

        @ConfigEntry
        public boolean doNestedSearch = true;

        public void validate() {
            this.searchRangeBlocks = Mth.clamp(this.searchRangeBlocks, 4, 16);
        }
    }
}
