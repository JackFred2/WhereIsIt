package red.jackf.whereisit.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.client.WhereIsItClient;

public class ScreenRendering {
    public static void render(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            var time = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() + tickDelta : 0;
            for (Slot slot : containerScreen.getMenu().slots) {
                if (slot.isActive() && slot.hasItem() && WhereIsItClient.lastRequest != null &&
                        SearchRequest.check(slot.getItem(), WhereIsItClient.lastRequest)) {
                    var x = slot.x + containerScreen.leftPos;
                    var y = slot.y + containerScreen.topPos;
                    var progress = 2 * time // shift over time
                            + slot.x // offset by slot X
                            - (mouseX + mouseY) / 6; // parallax with maths
                    progress /= 256; // slow down
                    var colour = WhereIsItClient.getColour(progress);
                    graphics.fill(x, y, x + 16, y + 16, colour);
                }
            }
        }
    }
}
