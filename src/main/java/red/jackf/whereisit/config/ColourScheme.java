package red.jackf.whereisit.config;

import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.util.ColourGetter;

public enum ColourScheme {
    SOLID(f -> 0xFF_FF0000),
    PRIDE(ColourGetter.Gradient.builder()
            .addRgb(0f, 255, 0, 0)
            .addRgb(0.17f, 255, 255, 0)
            .addRgb(0.33f, 0, 255, 0)
            .addRgb(0.5f, 0, 255, 255)
            .addRgb(0.67f, 0, 0, 255)
            .addRgb(0.83f, 255, 0, 255)
            .addRgb(1f, 255, 0, 0)
            .build()),
    GAY(ColourGetter.Gradient.builder()
            .addRgb(0f, 63, 137, 113)
            .addRgb(0.4f, 169, 228, 195)
            .addRgb(0.5f, 255, 255, 255)
            .addRgb(0.6f, 113, 171, 223)
            .addRgb(1f, 57, 30, 116)
            .buildWithSmoothTransition(0.05f)),
    LESBIAN(ColourGetter.Gradient.builder()
            .addRgb(0f, 213, 45, 0)
            .addRgb(0.4f, 255, 154, 86)
            .addRgb(0.5f, 255, 255, 255)
            .addRgb(0.6f, 209, 98, 164)
            .addRgb(1f, 163, 2, 98)
            .buildWithSmoothTransition(0.05f)),
    BISEXUAL(ColourGetter.Gradient.builder()
            .addRgb(0f, 208, 0, 112)
            .addRgb(0.5f, 140, 71, 153)
            .addRgb(1f, 0, 50, 160)
            .buildWithSmoothTransition(0.05f)),
    PANSEXUAL(ColourGetter.Gradient.builder()
            .addRgb(0f, 234, 70, 139)
            .addRgbBlock(0.5f, 0.05f, 248, 217, 63)
            .addRgb(1f, 83, 173, 250)
            .buildWithSmoothTransition(0.05f)),
    INTERSEX(ColourGetter.Gradient.builder()
            .addRgb(0.35f, 254, 215, 0)
            .addRgbBlock(0.5f, 0.1f, 122, 0, 183)
            .addRgb(0.65f, 254, 215, 0)
            .build()),
    NONBINARY(ColourGetter.Gradient.builder()
            .addRgb(0f, 253, 243, 82)
            .addRgbBlock(0.33f, 0.05f, 255, 255, 255)
            .addRgbBlock(0.66f,0.05f, 146, 94, 203)
            .addRgb(1f, 45, 45, 45)
            .buildWithSmoothTransition(0.05f)),
    TRANS(ColourGetter.Gradient.builder()
            .addRgb(0.05f, 120, 202, 246)
            .addRgb(0.25f, 234, 173, 184)
            .addRgb(0.5f, 255, 255, 255)
            .addRgb(0.75f, 234, 173, 184)
            .addRgb(0.95f, 120, 202, 246)
            .build()),
    ACE(ColourGetter.Gradient.builder()
            .addRgb(0f, 0, 0, 0)
            .addRgb(0.33f, 162, 162, 162)
            .addRgb(0.66f, 255, 255, 255)
            .addRgb(1f, 117, 33, 125)
            .buildWithSmoothTransition(0.05f)),
    ARO(ColourGetter.Gradient.builder()
            .addRgb(0f, 90, 161, 74)
            .addRgb(0.25f, 175, 208, 127)
            .addRgb(0.5f, 255, 255, 255)
            .addRgb(0.75f, 169, 169, 169)
            .addRgb(1f, 0, 0, 0)
            .buildWithSmoothTransition(0.05f)),
    BRITISH(ColourGetter.Gradient.builder()
            .addRgb(0f, 14, 33, 102)
            .addRgb(0.25f, 255, 255, 255)
            .addRgb(0.5f, 183, 51, 51)
            .addRgb(0.75f, 255, 255, 255)
            .addRgb(1f, 14, 33, 102)
            .build());

    private final ColourGetter gradient;

    ColourScheme(ColourGetter gradient) {
        this.gradient = gradient;
    }

    public ColourGetter getGradient() {
        return gradient;
    }
}
