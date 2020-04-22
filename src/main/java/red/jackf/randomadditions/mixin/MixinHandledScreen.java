package red.jackf.randomadditions.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
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
import red.jackf.randomadditions.RandomAdditions;
import red.jackf.randomadditions.RandomAdditionsClient;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {

    @Shadow
    protected Slot focusedSlot;

    @Inject(method= "keyPressed", at=@At("TAIL"))
    private void handleModdedKeys(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (RandomAdditionsClient.FIND_ITEMS.matchesKey(keyCode, scanCode)) {
            if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
                // Try find
                Item itemToFind = this.focusedSlot.getStack().getItem();

                PacketByteBuf findItemRequest = new PacketByteBuf(Unpooled.buffer());
                findItemRequest.writeIdentifier(Registry.ITEM.getId(itemToFind));
                ClientSidePacketRegistry.INSTANCE.sendToServer(RandomAdditions.FIND_ITEM_PACKET_ID, findItemRequest);
            }
        }
    }
}
