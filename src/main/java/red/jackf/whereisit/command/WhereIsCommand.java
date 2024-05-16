package red.jackf.whereisit.command;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import red.jackf.jackfredlib.api.extracommandsourcedata.ESD;
import red.jackf.whereisit.api.criteria.builtin.*;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.networking.ClientboundResultsPacket;
import red.jackf.whereisit.search.SearchHandler;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WhereIsCommand {
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
        var config = WhereIsItConfig.INSTANCE.instance().getCommon();
        if (config.commandAliases.isEmpty()) return;

        var root = dispatcher.register(literal(config.commandAliases.get(0)).requires(CommandSourceStack::isPlayer));

        var itemArg = literal("item").then(
            argument("item_id", ResourceArgument.resource(buildContext, Registries.ITEM))
                .redirect(root, ctx -> {
                    var item = ResourceArgument.getResource(ctx, "item_id", Registries.ITEM).value();
                    ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new ItemCriterion(item));
                    return ctx.getSource();
                }).executes(ctx -> {
                    var item = ResourceArgument.getResource(ctx, "item_id", Registries.ITEM).value();
                    ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new ItemCriterion(item));
                    doSearch(ctx);
                    return 0;
                }));

        var tagArg = literal("tag").then(
            argument("tag_id", ResourceLocationArgument.id())
                .suggests(suggestsRegistryTag(Registries.ITEM))
                .redirect(root, ctx -> {
                    var resLoc = ResourceLocationArgument.getId(ctx, "tag_id");
                    var tag = TagKey.create(Registries.ITEM, resLoc);
                    ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new ItemTagCriterion(tag));
                    return ctx.getSource();
                }).executes(ctx -> {
                    var resLoc = ResourceLocationArgument.getId(ctx, "tag_id");
                    var tag = TagKey.create(Registries.ITEM, resLoc);
                    ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new ItemTagCriterion(tag));
                    doSearch(ctx);
                    return 0;
                }));

        var nameArg = literal("name").then(
            argument("name", StringArgumentType.string())
                .redirect(root, ctx -> {
                    ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new NameCriterion(StringArgumentType.getString(ctx, "name")));
                    return ctx.getSource();
                }).executes(ctx -> {
                    ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new NameCriterion(StringArgumentType.getString(ctx, "name")));
                    doSearch(ctx);
                    return 0;
                }));

        var enchantmentArg = literal("enchantment").then(
            argument("enchantment_id", ResourceArgument.resource(buildContext, Registries.ENCHANTMENT)).then(
                argument("enchantment_level", IntegerArgumentType.integer(0))
                    .redirect(root, ctx -> {
                        var enchantment = ResourceArgument.getResource(ctx, "enchantment_id", Registries.ENCHANTMENT).value();
                        var level = IntegerArgumentType.getInteger(ctx, "enchantment_level");
                        ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new EnchantmentCriterion(enchantment, level));
                        return ctx.getSource();
                    }).executes(ctx -> {
                        var enchantment = ResourceArgument.getResource(ctx, "enchantment_id", Registries.ENCHANTMENT).value();
                        var level = IntegerArgumentType.getInteger(ctx, "enchantment_level");
                        ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new EnchantmentCriterion(enchantment, level));
                        doSearch(ctx);
                        return 0;
                    })
            ).then(
                literal("any")
                    .redirect(root, ctx -> {
                        var enchantment = ResourceArgument.getResource(ctx, "enchantment_id", Registries.ENCHANTMENT).value();
                        ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new EnchantmentCriterion(enchantment, null));
                        return ctx.getSource();
                    }).executes(ctx -> {
                        var enchantment = ResourceArgument.getResource(ctx, "enchantment_id", Registries.ENCHANTMENT).value();
                        ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new EnchantmentCriterion(enchantment, null));
                        doSearch(ctx);
                        return 0;
                    })
            ));

        var fluidArg = literal("fluid").then(
            argument("fluid_id", ResourceArgument.resource(buildContext, Registries.FLUID))
                .redirect(root, ctx -> {
                    var fluid = ResourceArgument.getResource(ctx, "fluid_id", Registries.FLUID).value();
                    ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new FluidCriterion(fluid));
                    return ctx.getSource();
                }).executes(ctx -> {
                    var fluid = ResourceArgument.getResource(ctx, "fluid_id", Registries.FLUID).value();
                    ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new FluidCriterion(fluid));
                    doSearch(ctx);
                    return 0;
                }));

        var potionArg = literal("potion").then(
            argument("potion_id", ResourceArgument.resource(buildContext, Registries.POTION))
                .redirect(root, ctx -> {
                    var potion = ResourceArgument.getResource(ctx, "potion_id", Registries.POTION).value();
                    ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new PotionEffectCriterion(potion));
                    return ctx.getSource();
                }).executes(ctx -> {
                    var potion = ResourceArgument.getResource(ctx, "potion_id", Registries.POTION).value();
                    ESD.getCustom(ctx, CommandCriteria.DEFINITION).addCriterion(new PotionEffectCriterion(potion));
                    doSearch(ctx);
                    return 0;
                }));

        dispatcher.register(literal(config.commandAliases.get(0))
                .then(itemArg)
                .then(tagArg)
                .then(nameArg)
                .then(fluidArg)
                .then(enchantmentArg)
                .then(potionArg));

        for (int i = 1; i < config.commandAliases.size(); i++) {
            dispatcher.register(literal(config.commandAliases.get(i)).redirect(root));
        }
    }

    private static void doSearch(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var request = ESD.getCustom(ctx, CommandCriteria.DEFINITION).toRequest();
        SearchHandler.handle(ClientboundResultsPacket.WHEREIS_COMMAND_ID, request, player);
    }
}
