package red.jackf.whereisit;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.minecraft.util.math.MathHelper;

@Config(name = WhereIsIt.MODID)
public class WhereIsItConfig implements ConfigData {
    @ConfigEntry.BoundedDiscrete(max = 16)
    public int searchRadius = 12;

    @ConfigEntry.BoundedDiscrete(max = 300, min = 10)
    @ConfigEntry.Gui.Tooltip
    public int fadeOutTime = 140;

    @ConfigEntry.ColorPicker
    public int colour = 0x4fff4f;

    @Override
    public void validatePostLoad() {
        searchRadius = MathHelper.clamp(searchRadius, 0, 16);
        colour = MathHelper.clamp(colour, 0x000000, 0xffffff);
        fadeOutTime = MathHelper.clamp(fadeOutTime, 0, 300);
    }
}
