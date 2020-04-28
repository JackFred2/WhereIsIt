package red.jackf.whereisit;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.minecraft.util.math.MathHelper;

@Config(name = WhereIsIt.MODID)
public class WhereIsItConfig implements ConfigData {
    @ConfigEntry.BoundedDiscrete(max = 16)
    public int searchRadius = 12;

    @ConfigEntry.BoundedDiscrete(max = 255)
    public int outlineR = 127;

    @ConfigEntry.BoundedDiscrete(max = 255)
    public int outlineG = 255;

    @ConfigEntry.BoundedDiscrete(max = 255)
    public int outlineB = 127;

    @ConfigEntry.BoundedDiscrete(max = 300)
    @ConfigEntry.Gui.Tooltip
    public int fadeOutTime = 140;

    @Override
    public void validatePostLoad() {
        searchRadius = MathHelper.clamp(searchRadius, 0, 16);
        outlineR = MathHelper.clamp(outlineR, 0, 255);
        outlineG = MathHelper.clamp(outlineG, 0, 255);
        outlineB = MathHelper.clamp(outlineB, 0, 255);
        fadeOutTime = MathHelper.clamp(fadeOutTime, 0, 300);
    }
}
