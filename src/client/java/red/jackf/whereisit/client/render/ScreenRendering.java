package red.jackf.whereisit.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.search.NestedItemStackSearcher;
import red.jackf.whereisit.client.WhereIsItClient;
import red.jackf.whereisit.config.WhereIsItConfig;

public class ScreenRendering {
    private static final ResourceLocation SLOT_HIGHLIGHT = WhereIsIt.id("textures/gui/slot_highlight.png");
    public static void render(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            var time = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
            for (Slot slot : containerScreen.getMenu().slots) {
                if (slot.isActive() && slot.hasItem() && WhereIsItClient.lastRequest != null &&
                        NestedItemStackSearcher.check(slot.getItem(), WhereIsItClient.lastRequest)) {
                    var x = slot.x + containerScreen.leftPos;
                    var y = slot.y + containerScreen.topPos;
                    var progress = (float) ((slot.x + (mouseX / 8) + (mouseY / 8) + (2 * time)) % 256) / 256;
                    var colour = WhereIsItClient.getColour(progress);
                    graphics.fill(x, y, x + 16, y + 16, colour);
                }
            }
        }
    }
}
