package red.jackf.whereisit.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.whereisit.REIHandler;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.WhereIsItClient;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {

    @Shadow
    protected Slot focusedSlot;

    @Inject(method= "keyPressed", at=@At("TAIL"))
    private void handleModdedKeys(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (WhereIsItClient.FIND_ITEMS.matchesKey(keyCode, scanCode)) {
            Item itemToFind = null;
            if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
                itemToFind = this.focusedSlot.getStack().getItem();
            } else if (WhereIsIt.REILoaded) {
                double gameScale = (double)MinecraftClient.getInstance().getWindow().getScaledWidth() / (double)MinecraftClient.getInstance().getWindow().getWidth();
                double mouseX = MinecraftClient.getInstance().mouse.getX() * gameScale;
                double mouseY = MinecraftClient.getInstance().mouse.getY() * gameScale;

                itemToFind = REIHandler.findREIItems(mouseX, mouseY);
            }

            if (itemToFind != null) {
                PacketByteBuf findItemRequest = new PacketByteBuf(Unpooled.buffer());
                findItemRequest.writeIdentifier(Registry.ITEM.getId(itemToFind));
                ClientSidePacketRegistry.INSTANCE.sendToServer(WhereIsIt.FIND_ITEM_PACKET_ID, findItemRequest);
            }
        }
    }
}
