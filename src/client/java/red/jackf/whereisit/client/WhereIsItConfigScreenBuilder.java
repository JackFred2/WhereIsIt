package red.jackf.whereisit.client;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import red.jackf.whereisit.config.WhereIsItConfig;

import static net.minecraft.network.chat.Component.translatable;

public class WhereIsItConfigScreenBuilder {

    public static Screen build(Screen parent) {
        return YetAnotherConfigLib.create(WhereIsItConfig.INSTANCE, (defaults, config, builder) -> builder
            .title(translatable("whereisit.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(translatable("whereisit.config.title"))
                        .group(OptionGroup.createBuilder()
                                .name(translatable("whereisit.config.common"))
                                .option(Option.<Integer>createBuilder()
                                        .name(translatable("whereisit.config.common.searchRange"))
                                        .binding(
                                                defaults.common.searchRangeBlocks,
                                                () -> config.common.searchRangeBlocks,
                                                i -> config.common.searchRangeBlocks = i
                                        )
                                        .description(OptionDescription.of(translatable("whereisit.config.common.searchRange.description")))
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(4, 16)
                                                .step(1)
                                                .valueFormatter(i -> translatable("whereisit.config.common.searchRange.slider", i)))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(translatable("whereisit.config.client"))
                                .option(Option.<Integer>createBuilder()
                                        .name(translatable("whereisit.config.client.fadeoutTime"))
                                        .binding(
                                                config.getClient().fadeoutTimeTicks,
                                                () -> config.getClient().fadeoutTimeTicks,
                                                i -> config.getClient().fadeoutTimeTicks = i
                                        )
                                        .description(OptionDescription.of(translatable("whereisit.config.client.fadeoutTime.description")))
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(100, 400)
                                                .step(1)
                                                .valueFormatter(i -> translatable("whereisit.config.client.fadeoutTime.slider", "%.2f".formatted(i.floatValue() / 20))))
                                        .build())
                                .build())
                        .build())
        ).generateScreen(parent);
    }
}
