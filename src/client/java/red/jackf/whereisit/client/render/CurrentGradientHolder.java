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
        if (WhereIsItConfig.INSTANCE.getConfig().getClient().randomScheme) {
            selected = RANDOM_CANDIDATES[(int) (Math.random() * RANDOM_CANDIDATES.length)].getGradient();
        } else {
            var scheme = WhereIsItConfig.INSTANCE.getConfig().getClient().colourScheme;
            if (scheme == ColourScheme.SOLID) {
                selected = Colour.fromInt(WhereIsItConfig.INSTANCE.getConfig().getClient().solidColour.getRGB());
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
