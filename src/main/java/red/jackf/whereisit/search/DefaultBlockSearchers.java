package red.jackf.whereisit.search;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.world.Nameable;
import red.jackf.jackfredlib.api.ResultHolder;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.api.search.BlockSearcher;

import java.util.HashSet;

public class DefaultBlockSearchers {
    public static void setup() {
        setupTransferApi();
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void setupTransferApi() {
        BlockSearcher.EVENT.register(BlockSearcher.FALLBACK, (request, player, level, state, pos) -> {
            var checked = new HashSet<Storage<ItemVariant>>();
            for (var direction : Direction.values()) { // each side
                var storage = ItemStorage.SIDED.find(level, pos, direction);
                if (storage != null && !checked.contains(storage)) { // side valid and we haven't already tried this inv
                    checked.add(storage);
                    for (var view : storage) { // for each view in this side
                        if (view.isResourceBlank()) continue;
                        var resource = view.getResource().toStack((int) view.getAmount()); // get stack

                        if (SearchRequest.check(resource, request)) { // if stack passes test
                            var result = SearchResult.builder(pos);
                            result.item(resource);
                            if (level.getBlockEntity(pos) instanceof Nameable nameable)
                                result.name(nameable.getCustomName(), null);
                            return ResultHolder.value(result.build());
                        }
                    }
                }
            }

            return ResultHolder.pass();
        });
    }
}
