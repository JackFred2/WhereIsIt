package red.jackf.whereisit.compat;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.WhereIsItConfig;

@Environment(EnvType.CLIENT)
public class ModMenuHandler implements ModMenuApi {
    @Override
    public String getModId() {
        return WhereIsIt.MODID;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (parent) -> AutoConfig.getConfigScreen(WhereIsItConfig.class, parent).get();
    }
}
