package red.jackf.whereisit.config;

import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

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

    @ConfigEntry
    public Common common = new Common();

    public void validate() {
        if (this.client != null) this.client.validate();
        this.common.searchRangeBlocks = Mth.clamp(this.common.searchRangeBlocks, 4, 16);
    }

    public Client getClient() {
        if (client != null) return client;
        throw new AssertionError("Attempted to get client config on dedicated server");
    }

    public static class Client {
        @ConfigEntry
        public int fadeoutTimeTicks = 10 * TICKS_PER_SECOND;

        public void validate() {
            this.fadeoutTimeTicks = Mth.clamp(this.fadeoutTimeTicks, 5 * TICKS_PER_SECOND, 20 * TICKS_PER_SECOND);
        }
    }

    public static class Common {
        @ConfigEntry
        public int searchRangeBlocks = 8;
    }
}
