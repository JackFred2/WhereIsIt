package red.jackf.whereisit.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import red.jackf.whereisit.WhereIsItConfig;

@Environment(EnvType.CLIENT)
public class ModMenuHandler implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (parent) -> AutoConfig.getConfigScreen(WhereIsItConfig.class, parent).get();
    }
}
