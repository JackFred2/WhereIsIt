package red.jackf.whereisit.client.render;

import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Colours;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.whereisit.config.ColourScheme;
import red.jackf.whereisit.config.WhereIsItConfig;

public class CurrentGradientHolder {
    private static final ColourScheme[] RANDOM_CANDIDATES = new ColourScheme[] {
            ColourScheme.PRIDE,
            ColourScheme.GAY,
            ColourScheme.LESBIAN,
            ColourScheme.BISEXUAL,
            ColourScheme.PANSEXUAL,
            ColourScheme.NONBINARY,
            ColourScheme.INTERSEX,
            ColourScheme.TRANS,
            ColourScheme.ACE,
            ColourScheme.ARO,
    };
    private static Gradient currentGradient = Colours.RED;

    private CurrentGradientHolder() {}

    public static void refreshColourScheme() {
        Gradient selected;
        if (WhereIsItConfig.INSTANCE.instance().getClient().randomScheme) {
            selected = RANDOM_CANDIDATES[(int) (Math.random() * RANDOM_CANDIDATES.length)].getGradient();
        } else {
            var scheme = WhereIsItConfig.INSTANCE.instance().getClient().colourScheme;
            if (scheme == ColourScheme.SOLID) {
                selected = Colour.fromInt(WhereIsItConfig.INSTANCE.instance().getClient().solidColour.getRGB());
            } else if (scheme == ColourScheme.FLASHING) {
                Colour solid = Colour.fromInt(WhereIsItConfig.INSTANCE.instance().getClient().solidColour.getRGB());
                selected = Gradient.of(solid, Colours.BLACK, solid).repeat(2);
            } else {
                selected = scheme.getGradient();
            }
        }
        currentGradient = selected.squish(0.025f);
    }

    public static int getColour(float factor) {
        return currentGradient.sample(factor).toARGB();
    }
}
