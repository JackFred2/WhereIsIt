package red.jackf.whereisit;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
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

    public int getFadeoutTime() {
        return clientOptions.fadeOutTime;
    }

    public int getColour() {
        return clientOptions.colour;
    }

    public int getAlternateColour() {
        return clientOptions.alternateColour;
    }

    public int getTextSizeModifier() {
        return clientOptions.textSizeModifier;
    }

    public boolean shouldShowResultLabels() { return clientOptions.showLabelsForResults; }

    public boolean isRainbowMode() {
        return clientOptions.rainbowMode;
    }

    public boolean forceSimpleRender() {
        return clientOptions.forceSimpleRender;
    }

    public boolean disableSlotHighlight() {
        return clientOptions.disableSlotHighlight;
    }

    public int getMaximumResults() { return clientOptions.maximumResults; }

    public int getSearchRadius() {
        return serverOptions.searchRadius;
    }

    public boolean doDeepSearch() {
        return serverOptions.doDeepSearch;
    }

    public boolean printSearchTime() {
        return serverOptions.printSearchTime;
    }

    public int getCooldown() {
        return serverOptions.cooldownTicks;
    }

    @Override
    public void validatePostLoad() {
        clientOptions.colour = MathHelper.clamp(clientOptions.colour, 0x000000, 0xffffff);
        clientOptions.alternateColour = MathHelper.clamp(clientOptions.alternateColour, 0x000000, 0xffffff);
        clientOptions.fadeOutTime = MathHelper.clamp(clientOptions.fadeOutTime, 10, 3600 * 20);
        clientOptions.textSizeModifier = MathHelper.clamp(clientOptions.textSizeModifier, 50, 400);

        clientOptions.maximumResults = MathHelper.clamp(clientOptions.maximumResults, 0, 128);

        serverOptions.searchRadius = MathHelper.clamp(serverOptions.searchRadius, 8, 48);
        serverOptions.cooldownTicks = MathHelper.clamp(serverOptions.cooldownTicks, 0, 50);
    }

    private static class Client {
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip
        public int fadeOutTime = 200;

        @ConfigEntry.ColorPicker
        @ConfigEntry.Gui.Tooltip
        public int colour = 0x4fff4f;

        @ConfigEntry.ColorPicker
        @ConfigEntry.Gui.Tooltip
        public int alternateColour = 0xff4fff;

        @ConfigEntry.Gui.Tooltip
        public boolean rainbowMode = true;

        @ConfigEntry.Gui.Tooltip
        public boolean disableSlotHighlight = false;

        @ConfigEntry.Gui.Tooltip
        public boolean forceSimpleRender = false;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.BoundedDiscrete(max = 400, min = 50)
        public int textSizeModifier = 100;

        public boolean showLabelsForResults = true;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(max = 128, min = 0)
        public int maximumResults = 0;
    }

    private static class Server {
        @ConfigEntry.BoundedDiscrete(max = 48, min = 8)
        public int searchRadius = 16;

        @ConfigEntry.Gui.Tooltip
        public boolean doDeepSearch = true;

        public boolean printSearchTime = false;

        @ConfigEntry.BoundedDiscrete(max = 50, min = 0)
        @ConfigEntry.Gui.Tooltip
        public int cooldownTicks = 5;
    }
}
