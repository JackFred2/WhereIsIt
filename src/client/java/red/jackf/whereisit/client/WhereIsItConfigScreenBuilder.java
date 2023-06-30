package red.jackf.whereisit.client;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import red.jackf.whereisit.config.WhereIsItConfig;

import static net.minecraft.network.chat.Component.translatable;

public class WhereIsItConfigScreenBuilder {

    public static Screen build(Screen parent) {
        return YetAnotherConfigLib.create(WhereIsItConfig.INSTANCE, (defaults, config, builder) -> builder
            .title(translatable("whereisit.config.title"))
            .category(ConfigCategory.createBuilder()
                    .name(translatable("whereisit.config.common"))
                    .option(Option.<Integer>createBuilder()
                            .name(translatable("whereisit.config.common.searchRange"))
                            .binding(
                                    defaults.common.searchRange,
                                    () -> config.common.searchRange,
                                    i -> config.common.searchRange = i
                            )
                            .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                    .range(4, 16)
                                    .step(1)
                                    .valueFormatter(i -> translatable("whereisit.config.common.searchRange.slider", i)))
                            .build())
                    .build())
        ).generateScreen(parent);
    }
}
