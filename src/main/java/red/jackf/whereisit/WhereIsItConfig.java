package red.jackf.whereisit;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.minecraft.util.math.MathHelper;

@Config(name = WhereIsIt.MODID)
public class WhereIsItConfig implements ConfigData {
    @ConfigEntry.BoundedDiscrete(max = 16)
    int radius = 12;

    @Override
    public void validatePostLoad() {
        radius = MathHelper.clamp(radius, 0, 16);
    }
}
