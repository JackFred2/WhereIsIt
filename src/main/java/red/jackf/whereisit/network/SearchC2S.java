package red.jackf.whereisit.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

public class SearchC2S extends PacketByteBuf {
    public SearchC2S(@NotNull Item toFind) {
        super(Unpooled.buffer());
        this.writeIdentifier(Registry.ITEM.getId(toFind));
    }

    public static Item read(PacketByteBuf buf) {
        return Registry.ITEM.get(buf.readIdentifier());
    }
}
