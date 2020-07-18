package red.jackf.whereisit;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.minecraft.util.math.MathHelper;

@Config(name = WhereIsIt.MODID)
@Config.Gui.Background("minecraft:textures/block/barrel_top.png")
public class WhereIsItConfig implements ConfigData {
    @ConfigEntry.Category("clientOptions")
    @ConfigEntry.Gui.TransitiveObject
    public Client clientOptions = new Client();

    @ConfigEntry.Category("serverOptions")
    @ConfigEntry.Gui.TransitiveObject
    public Server serverOptions = new Server();

    public int getFadeoutTime() { return clientOptions.fadeOutTime; }
    public int getColour() { return clientOptions.colour; }
    public int getAlternateColour() { return clientOptions.alternateColour; }

    public int getSearchRadius() { return serverOptions.searchRadius; }
    public boolean doDeepSearch() { return serverOptions.doDeepSearch; }
    public boolean printSearchTime() { return serverOptions.printSearchTime; }
    public int getCooldown() { return serverOptions.cooldownTicks; }

    static class Client {
        @ConfigEntry.BoundedDiscrete(max = 300, min = 10)
        @ConfigEntry.Gui.Tooltip
        public int fadeOutTime = 140;

        @ConfigEntry.ColorPicker
        @ConfigEntry.Gui.Tooltip
        public int colour = 0x4fff4f;

        @ConfigEntry.ColorPicker
        @ConfigEntry.Gui.Tooltip
        public int alternateColour = 0xff4fff;
    }

    static class Server {
        @ConfigEntry.BoundedDiscrete(max = 16, min = 4)
        public int searchRadius = 12;

        @ConfigEntry.Gui.Tooltip
        public boolean doDeepSearch = true;

        public boolean printSearchTime = false;

        @ConfigEntry.BoundedDiscrete(max = 50, min = 0)
        @ConfigEntry.Gui.Tooltip
        public int cooldownTicks = 5;
    }

    @Override
    public void validatePostLoad() {
        clientOptions.colour = MathHelper.clamp(clientOptions.colour, 0x000000, 0xffffff);
        clientOptions.alternateColour = MathHelper.clamp(clientOptions.alternateColour, 0x000000, 0xffffff);
        clientOptions.fadeOutTime = MathHelper.clamp(clientOptions.fadeOutTime, 0, 300);

        serverOptions.searchRadius = MathHelper.clamp(serverOptions.searchRadius, 0, 16);
        serverOptions.cooldownTicks = MathHelper.clamp(serverOptions.searchRadius, 0, 50);
    }
}
