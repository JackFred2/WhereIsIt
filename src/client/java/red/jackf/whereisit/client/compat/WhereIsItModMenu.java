package red.jackf.whereisit.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import red.jackf.whereisit.client.WhereIsItConfigScreenBuilder;

public class WhereIsItModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return WhereIsItConfigScreenBuilder::build;
    }
}
