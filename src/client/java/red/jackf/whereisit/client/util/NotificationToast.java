package red.jackf.whereisit.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class NotificationToast implements Toast {
    private static final long DISPLAY_TIME = 5000L;
    private static final Component TITLE_TEXT = Component.translatable("key.categories.whereisit");
    private final Component component;
    private long startTime = -1;

    public static void sendNotInstalledOnServer() {
        var toasts = Minecraft.getInstance().getToasts();
        var existing = toasts.getToast(NotificationToast.class, Toast.NO_TOKEN);
        if (existing == null) {
            toasts.addToast(new NotificationToast(Component.translatable("gui.whereisit.not_installed_serverside")));
        } else {
            existing.startTime = -1;
        }
    }

    private NotificationToast(Component component) {
        this.component = component;
    }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long time) {
        if (this.startTime == -1) this.startTime = time;
        guiGraphics.blit(TEXTURE, 0, 0, 0, 32, this.width(), this.height());
        guiGraphics.drawString(toastComponent.getMinecraft().font, TITLE_TEXT, 30, 7, -11534256, false);
        guiGraphics.drawString(toastComponent.getMinecraft().font, component, 30, 18, -16777216, false);
        return time - this.startTime >= (DISPLAY_TIME * toastComponent.getNotificationDisplayTimeMultiplier()) ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (NotificationToast) obj;
        return Objects.equals(this.component, that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component);
    }

    @Override
    public String toString() {
        return "NotificationToast[" +
                "component=" + component + ']';
    }

}
