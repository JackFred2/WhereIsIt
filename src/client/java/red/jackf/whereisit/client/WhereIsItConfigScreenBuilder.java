package red.jackf.whereisit.client;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Colours;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jackfredlib.client.api.colour.GradientUtils;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.client.render.CurrentGradientHolder;
import red.jackf.whereisit.config.ColourScheme;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
                        .group(makeClientGroup(instance.defaults(), instance.instance()))
                        .group(makeCommonGroup(instance.defaults(), instance.instance()))
                        .group(ListOption.<String>createBuilder()
                                .name(translatable("whereisit.config.common.commandAliases"))
                                .description(OptionDescription.of(
                                        translatable("whereisit.config.common.commandAliases.description"),
                                        translatable("whereisit.config.common.commandAliases.requiresDataReload").withStyle(ChatFormatting.RED)
                                ))
                                .controller(StringControllerBuilder::create)
                                .binding(
                                        instance.defaults().getCommon().commandAliases,
                                        () -> instance.instance().getCommon().commandAliases,
                                        l -> instance.instance().getCommon().commandAliases = l
                                ).initial("find")
                                .insertEntriesAtEnd(true)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(translatable("whereisit.config.compatibility"))
                        .group(makeClientCompatibilityGroup(instance.defaults(), instance.instance()))
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(translatable("whereisit.config.debug"))
                        .group(makeClientDebugGroup(instance.defaults(), instance.instance()))
                        .group(makeCommonDebugGroup(instance.defaults(), instance.instance()))
                        .build())
                .save(() -> {
                    instance.save();
                    CurrentGradientHolder.refreshColourScheme();
                })
                .build()
                .generateScreen(parent);
    }

    private static OptionGroup makeClientGroup(WhereIsItConfig defaults, WhereIsItConfig config) {
        return OptionGroup.createBuilder()
                .name(translatable("whereisit.config.client"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("whereisit.config.client.searchUsingItemInHand"))
                        .binding(
                                defaults.getClient().searchUsingItemInHand,
                                () -> config.getClient().searchUsingItemInHand,
                                b -> config.getClient().searchUsingItemInHand = b
                        )
                        .description(OptionDescription.of(translatable("whereisit.config.client.searchUsingItemInHand.description")))
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("whereisit.config.client.playSoundOnRequest"))
                        .binding(
                                defaults.getClient().playSoundOnRequest,
                                () -> config.getClient().playSoundOnRequest,
                                b -> config.getClient().playSoundOnRequest = b
                        )
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("whereisit.config.client.closeGuiOnFoundResults"))
                        .binding(
                                defaults.getClient().closeGuiOnFoundResults,
                                () -> config.getClient().closeGuiOnFoundResults,
                                b -> config.getClient().closeGuiOnFoundResults = b
                        )
                        .description(b -> OptionDescription.createBuilder()
                                .text(translatable("whereisit.config.client.closeGuiOnFoundResults.description"))
                                .build()
                        )
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("whereisit.config.client.showSlotHighlights"))
                        .binding(
                                defaults.getClient().showSlotHighlights,
                                () -> config.getClient().showSlotHighlights,
                                b -> config.getClient().showSlotHighlights = b
                        )
                        .description(b -> OptionDescription.createBuilder()
                                .text(translatable("whereisit.config.client.showSlotHighlights.description"))
                                .image(WhereIsIt.id("textures/gui/config/slot_highlight_example_%s.png".formatted(b ? "enabled" : "disabled")), 108, 44)
                                .build()
                        )
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .onOffFormatter())
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(translatable("whereisit.config.client.showSlotHighlights.mouseFactor"))
                        .description(OptionDescription.of(translatable("whereisit.config.client.showSlotHighlights.mouseFactor.description")))
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .formatValue(f -> translatable("mco.download.percent", (int) (f * 100)))
                                .step(0.01f)
                                .range(0f, 4f))
                        .binding(
                                defaults.getClient().slotHighlightMouseFactor,
                                () -> config.getClient().slotHighlightMouseFactor,
                                f -> config.getClient().slotHighlightMouseFactor = f
                        )
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(translatable("whereisit.config.client.showSlotHighlights.xFactor"))
                        .description(OptionDescription.of(translatable("whereisit.config.client.showSlotHighlights.xFactor.description")))
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .formatValue(f -> translatable("mco.download.percent", (int) (f * 100)))
                                .step(0.01f)
                                .range(0f, 4f))
                        .binding(
                                defaults.getClient().slotHighlightXFactor,
                                () -> config.getClient().slotHighlightXFactor,
                                f -> config.getClient().slotHighlightXFactor = f
                        )
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(translatable("whereisit.config.client.highlightTimeFactor"))
                        .description(OptionDescription.of(translatable("whereisit.config.client.highlightTimeFactor.description")))
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .formatValue(f -> translatable("mco.download.percent", (int) (f * 100)))
                                .step(0.01f)
                                .range(0.1f, 4f))
                        .binding(
                                defaults.getClient().highlightTimeFactor,
                                () -> config.getClient().highlightTimeFactor,
                                f -> config.getClient().highlightTimeFactor = f
                        )
                        .build())
                .options(makeLabelOptions(defaults, config))
                .options(makeColourOptions(defaults, config))
                .build();
    }

    private static Collection<? extends Option<?>> makeLabelOptions(WhereIsItConfig defaults, WhereIsItConfig config) {
        var nameSizeOption = Option.<Float>createBuilder()
                .name(translatable("whereisit.config.client.containerNameLabelScale"))
                .description(f -> OptionDescription.createBuilder()
                        .customImage(CompletableFuture.supplyAsync(() -> getLabelImage(() -> f)))
                        .build())
                .binding(
                        defaults.getClient().containerNameLabelScale,
                        () -> config.getClient().containerNameLabelScale,
                        f -> config.getClient().containerNameLabelScale = f
                )
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .formatValue(f -> translatable("mco.download.percent", (int) (f * 100)))
                        .range(0.25f, 2f)
                        .step(0.01f))
                .build();
        var showNameToggle = Option.<Boolean>createBuilder()
                .name(translatable("whereisit.config.client.showContainerNamesInResults"))
                .description(b -> OptionDescription.createBuilder()
                        .customImage(CompletableFuture.supplyAsync(() -> getLabelImage(() -> b ? nameSizeOption.pendingValue() : 0)))
                        .build()
                )
                .binding(
                        defaults.getClient().showContainerNamesInResults,
                        () -> config.getClient().showContainerNamesInResults,
                        b -> config.getClient().showContainerNamesInResults = b
                )
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .coloured(true)
                        .yesNoFormatter())
                .build();
        return List.of(showNameToggle, nameSizeOption);
    }

    private static Optional<ImageRenderer> getLabelImage(Supplier<Float> scaleGetter) {
        return Optional.of(new ImageRenderer() {
            private static final int imageWidth = 700;
            private static final int imageHeight = 536;
            private static final int labelMidX = 340;
            private static final int labelMidY = 146;
            private static final int labelRefWidth = 136;
            private static final int labelRefHeight = 54;
            @Override
            public int render(GuiGraphics graphics, int x, int y, int renderWidth, float tickDelta) {
                float ratio = (float) renderWidth / imageWidth;
                int height = (int) (imageHeight * ratio);

                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                graphics.pose().scale(ratio, ratio, 1);
                graphics.blit(WhereIsIt.id("textures/gui/config/show_container_names_example.png"),
                        0, 0, 0, 0,
                        imageWidth, imageHeight, imageWidth, imageHeight);

                float f = scaleGetter.get();

                if (f == 0f) {
                    graphics.pose().popPose();
                    return height;
                }

                int halfWidth = (int) ((labelRefWidth / 2f) * f);
                int halfHeight = (int) ((labelRefHeight / 2f) * f);
                var bgColour = ((int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255F)) << 24;
                graphics.fill(labelMidX - halfWidth, labelMidY - halfHeight, labelMidX + halfWidth, labelMidY + halfHeight, bgColour);

                graphics.pose().translate(labelMidX, labelMidY, 0);
                graphics.pose().scale(f * 5, f * 5, f * 5);
                var font = Minecraft.getInstance().font;
                var textWidth = font.width("Tools");
                graphics.drawString(font, "Tools", -textWidth / 2, -font.lineHeight / 2, 0xFF_FFFFFF, false);
                graphics.pose().popPose();

                return height;
            }

            @Override
            public void close() {

            }
        });
    }

    private static Option<Boolean> makeRecipeViewerOption(String langId, String modid, boolean def, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        var desc = OptionDescription.createBuilder().text(
                translatable("whereisit.config.compatibility.client.%sSupport.description".formatted(langId)),
                translatable("whereisit.config.compatibility.requiresModInstalled")
        );
        if (!FabricLoader.getInstance().isModLoaded(modid))
            desc.text(translatable("whereisit.config.compatibility.modNotInstalled").withStyle(ChatFormatting.RED));

        return Option.<Boolean>createBuilder()
                .name(translatable("whereisit.config.compatibility.client.%sSupport".formatted(langId)))
                .binding(def, getter, setter)
                .description(desc.build())
                .available(FabricLoader.getInstance().isModLoaded(modid))
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .coloured(true)
                        .onOffFormatter())
                .build();
    }

    private static OptionGroup makeClientDebugGroup(WhereIsItConfig defaults, WhereIsItConfig config) {
        return OptionGroup.createBuilder()
                .name(translatable("whereisit.config.client"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("whereisit.config.debug.client.printSearchRequestsInChat"))
                        .binding(
                                defaults.getClient().debug.printSearchRequestsInChat,
                                () -> config.getClient().debug.printSearchRequestsInChat,
                                b -> config.getClient().debug.printSearchRequestsInChat = b
                        )
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .build();
    }

    private static OptionGroup makeCommonDebugGroup(WhereIsItConfig defaults, WhereIsItConfig config) {
        return OptionGroup.createBuilder()
                .name(translatable("whereisit.config.common"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("whereisit.config.debug.common.printSearchTime"))
                        .description(OptionDescription.of(translatable("whereisit.config.debug.common.printSearchTime.description")))
                        .binding(
                                defaults.getCommon().debug.printSearchTime,
                                () -> config.getCommon().debug.printSearchTime,
                                b -> config.getCommon().debug.printSearchTime = b
                        )
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("whereisit.config.common.enableDefaultSearchers"))
                        .binding(
                                defaults.getCommon().debug.enableDefaultSearchers,
                                () -> config.getCommon().debug.enableDefaultSearchers,
                                b -> config.getCommon().debug.enableDefaultSearchers = b
                        )
                        .description(OptionDescription.of(translatable("whereisit.config.common.enableDefaultSearchers.description")))
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("whereisit.config.debug.common.forceServerSideHighlightsOnly"))
                        .binding(
                                defaults.getCommon().debug.forceServerSideHighlightsOnly,
                                () -> config.getCommon().debug.forceServerSideHighlightsOnly,
                                b -> config.getCommon().debug.forceServerSideHighlightsOnly = b
                        )
                        .description(OptionDescription.of(translatable("whereisit.config.debug.common.forceServerSideHighlightsOnly.description")))
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("whereisit.config.debug.common.labelsAreSeeThrough"))
                        .binding(
                                defaults.getCommon().debug.labelsAreSeeThrough,
                                () -> config.getCommon().debug.labelsAreSeeThrough,
                                b -> config.getCommon().debug.labelsAreSeeThrough = b
                        )
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .build();
    }

    private static OptionGroup makeClientCompatibilityGroup(WhereIsItConfig defaults, WhereIsItConfig config) {
        var jeiSupport = makeRecipeViewerOption("jei", "jei",
                defaults.getClient().compatibility.jeiSupport,
                () -> config.getClient().compatibility.jeiSupport,
                b -> config.getClient().compatibility.jeiSupport = b
        );
        var reiSupport = makeRecipeViewerOption("rei", "roughlyenoughitems",
                defaults.getClient().compatibility.reiSupport,
                () -> config.getClient().compatibility.reiSupport,
                b -> config.getClient().compatibility.reiSupport = b
        );
        var emiSupport = makeRecipeViewerOption("emi", "emi",
                defaults.getClient().compatibility.emiSupport,
                () -> config.getClient().compatibility.emiSupport,
                b -> config.getClient().compatibility.emiSupport = b
        );

        return OptionGroup.createBuilder()
                .name(translatable("whereisit.config.client"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("whereisit.config.compatibility.client.recipeBookSupport"))
                        .description(OptionDescription.createBuilder()
                                .text(translatable("whereisit.config.compatibility.client.recipeBookSupport.description"))
                                .image(WhereIsIt.id("textures/gui/config/recipe_book_example.png"), 152, 66)
                                .build())
                        .binding(
                                defaults.getClient().compatibility.recipeBookSupport,
                                () -> config.getClient().compatibility.recipeBookSupport,
                                b -> config.getClient().compatibility.recipeBookSupport = b
                        )
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .onOffFormatter())
                        .build())
                .option(jeiSupport)
                .option(reiSupport)
                .option(emiSupport)
                .build();
    }

    private static Collection<? extends Option<?>> makeColourOptions(WhereIsItConfig defaults, WhereIsItConfig config) {
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
                        .customImage(CompletableFuture.supplyAsync(() -> getGradientPreview(ColourScheme.SOLID, color)))
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
                        .customImage(CompletableFuture.supplyAsync(() -> getGradientPreview(colourScheme, solidColourOption.pendingValue())))
                        .build())
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(ColourScheme.class))
                .listener((opt, scheme) -> solidColourOption.setAvailable(scheme == ColourScheme.SOLID || scheme == ColourScheme.FLASHING))
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
                        .onOffFormatter()
                        .coloured(true))
                .listener((opt, enabled) -> {
                    solidColourOption.setAvailable(!enabled && (colourSchemeOption.pendingValue() == ColourScheme.SOLID || colourSchemeOption.pendingValue() == ColourScheme.FLASHING));
                    colourSchemeOption.setAvailable(!enabled);
                })
                .build();

        return List.of(randomSchemeOption, colourSchemeOption, solidColourOption);
    }

    private static Optional<ImageRenderer> getGradientPreview(ColourScheme scheme, Color solidColour) {
        var renderer = new ImageRenderer() {
            private static void blit(GuiGraphics graphics, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight) {
                graphics.blit(COLOUR_PREVIEW_BORDER, x, y, width, height, u, v, regionWidth, regionHeight, 24, 64);
            }

            @Override
            public int render(GuiGraphics graphics, int x, int y, int renderWidth, float tickDelta) {
                int borderThickness = 8;
                int renderHeight = 64;

                int width = renderWidth - 2 * borderThickness;
                int height = renderHeight - 2 * borderThickness;

                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                blit(graphics, 0, 0, borderThickness, renderHeight, 0, 0, borderThickness, renderHeight); // left
                blit(graphics, renderWidth - borderThickness, 0, borderThickness, renderHeight, 2 * borderThickness, 0, borderThickness, renderHeight); // right
                blit(graphics, borderThickness, 0, width, borderThickness, borderThickness, 0, borderThickness, borderThickness); // top
                blit(graphics, borderThickness, renderHeight - borderThickness, width, borderThickness, borderThickness, renderHeight - borderThickness, borderThickness, borderThickness); // bottom
                Gradient previewScheme;
                Colour solid = Colour.fromInt(solidColour.getRGB());
                if (scheme == ColourScheme.SOLID) {
                    previewScheme = solid;
                } else if (scheme == ColourScheme.FLASHING) {
                    previewScheme = Gradient.of(solid, Colours.BLACK, solid).repeat(2);
                } else {
                    previewScheme = scheme.getGradient();
                }
                GradientUtils.drawHorizontalGradient(graphics, borderThickness, borderThickness, width, height, previewScheme, 0, 1);
                graphics.pose().popPose();

                return renderHeight;
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
                                .range(4, 24)
                                .step(1)
                                .formatValue(i -> translatable("whereisit.config.common.searchRange.slider", i)))
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
                .option(Option.<Integer>createBuilder()
                        .name(translatable("whereisit.config.common.fadeoutTime"))
                        .binding(
                                defaults.getCommon().fadeoutTimeTicks,
                                () -> config.getCommon().fadeoutTimeTicks,
                                i -> config.getCommon().fadeoutTimeTicks = i
                        )
                        .description(OptionDescription.of(
                                translatable("whereisit.config.common.fadeoutTime.description1"),
                                translatable("whereisit.config.common.fadeoutTime.description2")
                        ))
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(5 * TICKS_PER_SECOND, 30 * TICKS_PER_SECOND)
                                .step(1)
                                .formatValue(i -> translatable("whereisit.config.common.fadeoutTime.slider", "%.2f".formatted(i.floatValue() / 20))))
                        .build())
                .build();
    }
}
