package red.jackf.whereisit.defaults;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import red.jackf.jackfredlib.api.ResultHolder;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.api.search.BlockSearcher;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.util.HashSet;

public class DefaultBlockSearchers {
    static void setup() {
        setupTransferApi();
        setupEnderChest();
        setupCheckShulkerItself();
    }

    // check the regular contents of an inventory
    @SuppressWarnings("UnstableApiUsage")
    private static void setupTransferApi() {
        BlockSearcher.EVENT.register(BlockSearcher.FALLBACK, (request, player, level, state, pos) -> {
            if (!WhereIsItConfig.INSTANCE.getConfig().getCommon().enableDefaultSearchers) return ResultHolder.pass();
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

    // check inside a player's local ender chest inventory
    private static void setupEnderChest() {
        BlockSearcher.EVENT.register(BlockSearcher.DEFAULT, (request, player, level, state, pos) -> {
            if (!WhereIsItConfig.INSTANCE.getConfig().getCommon().enableDefaultSearchers) return ResultHolder.pass();
            if (!state.is(Blocks.ENDER_CHEST)) return ResultHolder.pass();
            for (ItemStack enderItem : player.getEnderChestInventory().items)
                if (SearchRequest.check(enderItem, request))
                    return ResultHolder.value(SearchResult.builder(pos)
                            .item(enderItem)
                            .build());
            // there's very likely no other inventory directly at the ender chest so cancel here
            return ResultHolder.empty();
        });
    }

    // if searching for a shulker box, check if a placed down box is valid
    private static void setupCheckShulkerItself() {
        BlockSearcher.EVENT.register(BlockSearcher.DEFAULT, ((request, player, level, state, pos) -> {
            if (!WhereIsItConfig.INSTANCE.getConfig().getCommon().enableDefaultSearchers) return ResultHolder.pass();
            if (!state.is(BlockTags.SHULKER_BOXES)) return ResultHolder.pass();
            var shulkerBoxBe = level.getBlockEntity(pos, BlockEntityType.SHULKER_BOX);
            if (shulkerBoxBe.isEmpty()) return ResultHolder.pass();
            var fakeItem = new ItemStack(state.getBlock().asItem());
            fakeItem.setHoverName(shulkerBoxBe.get().getCustomName());
            if (SearchRequest.check(fakeItem, request)) {
                return ResultHolder.value(SearchResult.builder(pos)
                    .item(fakeItem)
                    .name(shulkerBoxBe.get().getCustomName(), null)
                    .build());
            }
            return ResultHolder.pass();
        }));
    }
}
