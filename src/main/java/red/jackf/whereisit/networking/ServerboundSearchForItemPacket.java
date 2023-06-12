package red.jackf.whereisit.networking;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchRequest;

public record ServerboundSearchForItemPacket(SearchRequest request) implements FabricPacket {

    public static final PacketType<ServerboundSearchForItemPacket> TYPE = PacketType.create(WhereIsIt.id("c2s_searchforitem"), ServerboundSearchForItemPacket::new);

    public ServerboundSearchForItemPacket(FriendlyByteBuf buf) {
        this(getRequest(buf));
    }

    private static SearchRequest getRequest(FriendlyByteBuf buf) {
        var nbt = buf.readNbt();
        return nbt != null ? SearchRequest.load(nbt) : new SearchRequest();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        var packed = request.pack();
        buf.writeNbt(packed);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
