package red.jackf.whereisit.client;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.gui.ImageRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.config.ColourScheme;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;
import static net.minecraft.network.chat.Component.translatable;

public class WhereIsItConfigScreenBuilder {
    private static final ResourceLocation COLOUR_PREVIEW_BORDER = WhereIsIt.id("textures/gui/config/colour_preview_border.png");

    public static Screen build(Screen parent) {
        var instance = WhereIsItConfig.INSTANCE;

        return YetAnotherConfigLib.createBuilder()
                .title(translatable("whereisit.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(translatable("whereisit.config.title"))
                        .group(makeClientGroup(instance.getDefaults(), instance.getConfig()))
                        .group(makeCommonGroup(instance.getDefaults(), instance.getConfig()))
                        .build())
                .save(() -> {
                    instance.save();
                    WhereIsItClient.updateScheme();
                })
                .build()
                .generateScreen(parent);
    }

    private static OptionGroup makeClientGroup(WhereIsItConfig defaults, WhereIsItConfig config) {
        var solidColourOption = Option.<Color>createBuilder()
                .name(translatable("whereisit.config.client.solidColour"))
                .binding(
                        defaults.getClient().solidColour,
                        () -> config.getClient().solidColour,
                        c -> config.getClient().solidColour = c
                )
                .controller(opt -> ColorControllerBuilder.create(opt)
                        .allowAlpha(false))
                .description(color -> OptionDescription.createBuilder()
                        .customImage(CompletableFuture.supplyAsync(() -> getPreviewImage(ColourScheme.SOLID, color)))
                        .build())
                .build();

        var colourSchemeOption = Option.<ColourScheme>createBuilder()
                .name(translatable("whereisit.config.client.colourScheme"))
                .binding(
                        defaults.getClient().colourScheme,
                        () -> config.getClient().colourScheme,
                        c -> config.getClient().colourScheme = c
                )
                .description(colourScheme -> OptionDescription.createBuilder()
                        .customImage(CompletableFuture.supplyAsync(() -> getPreviewImage(colourScheme, solidColourOption.pendingValue())))
                        .build())
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(ColourScheme.class))
                .listener((opt, scheme) -> solidColourOption.setAvailable(scheme == ColourScheme.SOLID))
                .build();

        var randomSchemeOption = Option.<Boolean>createBuilder()
                .name(translatable("whereisit.config.client.randomScheme"))
                .binding(
                        defaults.getClient().randomScheme,
                        () -> config.getClient().randomScheme,
                        b -> config.getClient().randomScheme = b
                )
                .description(OptionDescription.of(translatable("whereisit.config.client.randomScheme.description")))
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .onOffFormatter())
                .listener((opt, enabled) -> {
                    solidColourOption.setAvailable(!enabled && colourSchemeOption.pendingValue() == ColourScheme.SOLID);
                    colourSchemeOption.setAvailable(!enabled);
                })
                .build();

        return OptionGroup.createBuilder()
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
                .option(randomSchemeOption)
                .option(colourSchemeOption)
                .option(solidColourOption)
                .build();
    }

    private static Optional<ImageRenderer> getPreviewImage(ColourScheme scheme, Color solid) {
        var renderer = new ImageRenderer() {
            private static void blit(GuiGraphics graphics, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight) {
                graphics.blit(COLOUR_PREVIEW_BORDER, x, y, width, height, u, v, regionWidth, regionHeight, 24, 64);
            }

            @Override
            public int render(GuiGraphics graphics, int x, int y, int renderWidth) {
                int borderThickness = 8;
                int height = 64;

                var width = renderWidth - (2 * borderThickness);

                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                blit(graphics, 0, 0, borderThickness, height, 0, 0, borderThickness, height); // left
                blit(graphics, renderWidth - borderThickness, 0, borderThickness, height, 2 * borderThickness, 0, borderThickness, height); // right
                blit(graphics, borderThickness, 0, width, borderThickness, borderThickness, 0, borderThickness, borderThickness); // top
                blit(graphics, borderThickness, height - borderThickness, width, borderThickness, borderThickness, height - borderThickness, borderThickness, borderThickness); // bottom
                for (int i = 0; i < width; i += 1) {
                    graphics.fill(i + borderThickness, borderThickness, i + borderThickness + 1, 56, scheme == ColourScheme.SOLID ? solid.getRGB() : scheme.getGradient().eval(((float) i) / width));
                }
                graphics.pose().popPose();

                return height;
            }

            @Override
            public void close() {

            }
        };
        return Optional.of(renderer);
    }

    private static OptionGroup makeCommonGroup(WhereIsItConfig defaults, WhereIsItConfig config) {
        return OptionGroup.createBuilder()
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
                .build();
    }
}
