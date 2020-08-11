package red.jackf.whereisit.compat;

import net.fabricmc.loader.api.FabricLoader;
import red.jackf.whereisit.Searcher;
import red.jackf.whereisit.api.WhereIsItEntrypoint;

public class ModItemBehaviors implements WhereIsItEntrypoint {

    @Override
    public void setupBehaviors(Searcher searcher) {
        // Universal Components item inventories
        if (FabricLoader.getInstance().isModLoaded("universalcomponents")) {
            /*searcher.addItemBehavior(
                UniversalComponentsHandler::hasInvComponent,
                UniversalComponentsHandler::searchItem
            );*/
        }
    }
}
