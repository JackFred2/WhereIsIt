package red.jackf.whereisit.config;

import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class WhereIsItConfig {
    public static final GsonConfigInstance<WhereIsItConfig> INSTANCE
            = GsonConfigInstance.createBuilder(WhereIsItConfig.class)
                .setPath(FabricLoader.getInstance().getConfigDir().resolve("whereisit.json"))
                .overrideGsonBuilder(WhereIsItGSON.get())
                .build();

    @ConfigEntry
    @Nullable
    public Client client = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? new Client() : null;

    @ConfigEntry
    public Common common = new Common();

    public void validate() {
        this.common.searchRange = Mth.clamp(this.common.searchRange, 4, 16);
    }

    public static class Client {
        @ConfigEntry
        public float testFloat = -3f;
    }

    public static class Common {
        @ConfigEntry
        public int searchRange = 8;
    }
}
