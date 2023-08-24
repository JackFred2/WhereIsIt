package red.jackf.whereisit.command;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.EnchantmentCriterion;
import red.jackf.whereisit.api.criteria.ItemCriterion;
import red.jackf.whereisit.api.criteria.ItemTagCriterion;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.search.SearchHandler;

import java.util.stream.IntStream;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WhereIsCommand {
    private static final DynamicCommandExceptionType UNKNOWN_ITEM_TAG = new DynamicCommandExceptionType(tagId -> new LiteralMessage("Unknown tag: #" + tagId));

    @SuppressWarnings("SameParameterValue")
    private static <T> SuggestionProvider<CommandSourceStack> suggestsRegistryTag(ResourceKey<Registry<T>> registryKey) {
        return (ctx, builder) ->
                SharedSuggestionProvider.suggestResource(ctx.getSource()
                        .registryAccess()
                        .registryOrThrow(registryKey)
                        .getTagNames()
                        .map(TagKey::location), builder);
    }

    private WhereIsCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection ignored) {
        var config = WhereIsItConfig.INSTANCE.getConfig().getCommon();
        if (config.commandAliases.isEmpty()) return;

        var root = dispatcher.register(literal(config.commandAliases.get(0)).requires(CommandSourceStack::isPlayer));

        dispatcher.register(literal(config.commandAliases.get(0))
            .then(literal("item").then(
                argument("item_id", ResourceArgument.resource(buildContext, Registries.ITEM))
                    .executes(WhereIsCommand::searchItem)))
            .then(literal("tag").then(
                argument("item_tag", ResourceLocationArgument.id())
                        .suggests(suggestsRegistryTag(Registries.ITEM))
                    .executes(WhereIsCommand::searchItemTag)))
            .then(literal("enchantment").then(
                    argument("enchantment_id", ResourceArgument.resource(buildContext, Registries.ENCHANTMENT))
                        .executes(ctx -> WhereIsCommand.searchEnchantment(
                            ctx.getSource().getPlayerOrException(),
                            ResourceArgument.getResource(ctx, "enchantment_id", Registries.ENCHANTMENT),
                            null))
                        .then(
                            argument("enchantment_level", IntegerArgumentType.integer(0))
                                .suggests((ctx, builder) -> {
                                    var enchantment = ResourceArgument.getResource(ctx, "enchantment_id", Registries.ENCHANTMENT).value();
                                    return SharedSuggestionProvider.suggest(IntStream.rangeClosed(0, enchantment.getMaxLevel()).mapToObj(Integer::toString), builder);
                                })
                                .executes(ctx -> WhereIsCommand.searchEnchantment(
                                    ctx.getSource().getPlayerOrException(),
                                    ResourceArgument.getResource(ctx, "enchantment_id", Registries.ENCHANTMENT),
                                    IntegerArgumentType.getInteger(ctx, "enchantment_level")
                                )))
            )));

        for (int i = 1; i < config.commandAliases.size(); i++) {
            dispatcher.register(literal(config.commandAliases.get(i)).redirect(root));
        }
    }

    private static int searchItem(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var request = new SearchRequest();
        request.accept(new ItemCriterion(ResourceArgument.getResource(ctx, "item_id", Registries.ITEM).value()));
        SearchHandler.handle(request, player);
        return 0;
    }

    private static int searchItemTag(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var request = new SearchRequest();
        var tagId = ResourceLocationArgument.getId(ctx, "item_tag");
        var tag = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tagId));
        if (tag.isEmpty()) throw UNKNOWN_ITEM_TAG.create(tagId);
        request.accept(new ItemTagCriterion(tag.get().key()));
        SearchHandler.handle(request, player);
        return 0;
    }

    private static int searchEnchantment(ServerPlayer player, Holder.Reference<Enchantment> enchantment, Integer level) {
        var request = new SearchRequest();
        request.accept(new EnchantmentCriterion(enchantment.value(), level));
        SearchHandler.handle(request, player);
        return 0;
    }
}
