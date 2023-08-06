package red.jackf.whereisit.config;

import red.jackf.jackfredlib.api.colour.Colours;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jackfredlib.api.colour.Gradients;

public enum ColourScheme {
    SOLID(Colours.RED),
    PRIDE(Gradients.RAINBOW),
    GAY(Gradients.GAY),
    LESBIAN(Gradients.LESBIAN),
    BISEXUAL(Gradients.BISEXUAL),
    PANSEXUAL(Gradients.PANSEXUAL),
    INTERSEX(Gradients.INTERSEX_SMOOTH),
    NONBINARY(Gradients.NONBINARY),
    TRANS(Gradients.TRANS),
    ACE(Gradients.ACE),
    ARO(Gradients.ARO),
    BRITISH(Gradient.of(Colours.RED, Colours.WHITE, Colours.BLUE, Colours.RED));

    private final Gradient gradient;

    ColourScheme(Gradient gradient) {
        this.gradient = gradient;
    }

    public Gradient getGradient() {
        return gradient;
    }
}
