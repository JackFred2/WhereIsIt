package red.jackf.whereisit.client.defaults;

import red.jackf.whereisit.client.api.WhereIsItClientPlugin;
import red.jackf.whereisit.client.api.events.OnResult;
import red.jackf.whereisit.client.render.Rendering;

public class WhereIsItDefaultClientPlugin implements WhereIsItClientPlugin {
    public void load() {
        SearchRequestPopulatorDefaults.setup();
        OverlayStackBehaviorDefaults.setup();
        SearchInvokerDefaults.setup();
        ShouldIgnoreKeyDefaults.setup();
        OnResult.EVENT.register(Rendering::addResults);
    }
}
