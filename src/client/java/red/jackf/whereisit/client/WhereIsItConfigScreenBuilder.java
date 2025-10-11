package red.jackf.whereisit.client;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
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
    private static final ResourceLocation COLOUR_PREVIEW_BORDER = WhereIsIt.id("colour_preview_border");

    public static Screen build(Screen parent) {
        var instance = WhereIsItConfig.INSTANCE;

        return YetAnotherConfigLib.createBuilder()
                .title(translatable("whereisit.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(translatable("whereisit.config.title"))
                        .group(makeClientGroup(instance.defaults(), instance.instance()))
                        //.group(makeCommonGroup(instance.defaults(), instance.instance()))
                        //.group(makeClientCompatibilityGroup(instance.defaults(), instance.instance()))
                        //.group(makeClientDebugGroup(instance.defaults(), instance.instance()))
                        //.group(makeCommonDebugGroup(instance.defaults(), instance.instance()))
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
            private static final int IMAGE_WIDTH = 700;
            private static final int IMAGE_HEIGHT = 536;
            private static final int LABEL_MID_X = 340;
            private static final int LABEL_MID_Y = 146;
            private static final int LABEL_REF_WIDTH = 136;
            private static final int LABEL_REF_HEIGHT = 54;

            @Override
            public int render(GuiGraphics graphics, int x, int y, int renderWidth, float tickDelta) {
                float ratio = (float) renderWidth / IMAGE_WIDTH;
                int height = (int) (IMAGE_HEIGHT * ratio);

                graphics.pose().translate(x, y);
                graphics.pose().scale(ratio, ratio);
                graphics.blit(
                        WhereIsIt.id("textures/gui/config/show_container_names_example.png"),
                        0, 0, 0, 0,
                        IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT
                );


                float f = scaleGetter.get();
                if (f == 0f) return height;

                int halfWidth = (int) ((LABEL_REF_WIDTH / 2f) * f);
                int halfHeight = (int) ((LABEL_REF_HEIGHT / 2f) * f);
                var bgColour = ((int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255F)) << 24;
                graphics.fill(LABEL_MID_X - halfWidth, LABEL_MID_Y - halfHeight, LABEL_MID_X + halfWidth, LABEL_MID_Y + halfHeight, bgColour);

                graphics.pose().translate(LABEL_MID_X, LABEL_MID_Y);
                graphics.pose().scale(f * 5f, f * 5f);
                var font = Minecraft.getInstance().font;
                var textWidth = font.width("Tools");
                graphics.drawString(font, "Tools", -textWidth / 2, -font.lineHeight / 2, 0xFFFFFFFF, false);

                return height;
            }

            @Override
            public void close() { }
        });
    }

    private static Collection<? extends Option<?>> makeColourOptions(WhereIsItConfig defaults, WhereIsItConfig config) {
        var solidColourOption = Option.<Color>createBuilder()
                .name(translatable("whereisit.config.client.solidColour"))
                .binding(
                        defaults.getClient().solidColour,
                        () -> config.getClient().solidColour,
                        c -> config.getClient().solidColour = c
                )
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
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
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(ColourScheme.class))
                .description(colourScheme -> OptionDescription.createBuilder()
                        .customImage(CompletableFuture.supplyAsync(() -> getGradientPreview(colourScheme, solidColourOption.pendingValue())))
                        .build())
                .build();

        return List.of(colourSchemeOption, solidColourOption);
    }

    private static Optional<ImageRenderer> getGradientPreview(ColourScheme scheme, Color solidColour) {
        return Optional.of(new ImageRenderer() {
            @Override
            public int render(GuiGraphics graphics, int x, int y, int renderWidth, float tickDelta) {
                int borderThickness = 8;
                int renderHeight = 64;
                int width = renderWidth - 2 * borderThickness;
                int height = renderHeight - 2 * borderThickness;

                graphics.pose().translate(x, y);
                graphics.blit(
                        COLOUR_PREVIEW_BORDER, // ResourceLocation текстуры
                        0, 0,                  // x, y
                        0, 0,                  // u, v
                        renderWidth, renderHeight, // width, height
                        256, 256               // размер текстуры
                );
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

                return renderHeight;
            }

            @Override
            public void close() { }
        });
    }
}
