package red.jackf.whereisit.client;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.config.WhereIsItConfig;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;
import static net.minecraft.network.chat.Component.translatable;

public class WhereIsItConfigScreenBuilder {

    public static Screen build(Screen parent) {
        return YetAnotherConfigLib.create(WhereIsItConfig.INSTANCE, (defaults, config, builder) -> builder
                .title(translatable("whereisit.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(translatable("whereisit.config.title"))
                        .group(OptionGroup.createBuilder()
                                .name(translatable("whereisit.config.client"))
                                .option(Option.<Integer>createBuilder()
                                        .name(translatable("whereisit.config.client.fadeoutTime"))
                                        .binding(
                                                defaults.getClient().fadeoutTimeTicks,
                                                () -> config.getClient().fadeoutTimeTicks,
                                                i -> config.getClient().fadeoutTimeTicks = i
                                        )
                                        .description(OptionDescription.of(translatable("whereisit.config.client.fadeoutTime.description")))
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(5 * TICKS_PER_SECOND, 30 * TICKS_PER_SECOND)
                                                .step(1)
                                                .valueFormatter(i -> translatable("whereisit.config.client.fadeoutTime.slider", "%.2f".formatted(i.floatValue() / 20))))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(translatable("whereisit.config.client.showSlotHighlights"))
                                        .binding(
                                                defaults.getClient().showSlotHighlights,
                                                () -> config.getClient().showSlotHighlights,
                                                b -> config.getClient().showSlotHighlights = b
                                        )
                                        .description(OptionDescription.createBuilder()
                                                .text(translatable("whereisit.config.client.showSlotHighlights.description"))
                                                .image(WhereIsIt.id("textures/gui/config/slot_highlight_example.png"), 108, 44)
                                                .build()
                                        )
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .coloured(true)
                                                .onOffFormatter())
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(translatable("whereisit.config.common"))
                                .option(Option.<Integer>createBuilder()
                                        .name(translatable("whereisit.config.common.searchRange"))
                                        .binding(
                                                defaults.getCommon().searchRangeBlocks,
                                                () -> config.getCommon().searchRangeBlocks,
                                                i -> config.getCommon().searchRangeBlocks = i
                                        )
                                        .description(OptionDescription.of(translatable("whereisit.config.common.searchRange.description")))
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(4, 16)
                                                .step(1)
                                                .valueFormatter(i -> translatable("whereisit.config.common.searchRange.slider", i)))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(translatable("whereisit.config.common.nestedSearching"))
                                        .binding(
                                                defaults.getCommon().doNestedSearch,
                                                () -> config.getCommon().doNestedSearch,
                                                b -> config.getCommon().doNestedSearch = b
                                        )
                                        .description(b -> OptionDescription.createBuilder()
                                                .text(translatable("whereisit.config.common.nestedSearching.description"))
                                                .image(WhereIsIt.id("textures/gui/config/nested_search_example_%s.png".formatted(b ? "enabled" : "disabled")), 200, 92)
                                                .build()
                                        )
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .coloured(true)
                                                .onOffFormatter())
                                        .build())
                                .build())
                        .build())
        ).generateScreen(parent);
    }
}
