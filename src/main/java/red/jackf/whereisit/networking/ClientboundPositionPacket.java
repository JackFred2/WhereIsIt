package red.jackf.whereisit.networking;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import red.jackf.whereisit.WhereIsIt;

import java.util.HashSet;
import java.util.Set;

public record ClientboundPositionPacket(Set<BlockPos> positions) implements FabricPacket {
    public static final PacketType<ClientboundPositionPacket> TYPE = PacketType.create(WhereIsIt.id("c2s_searchforitem"), ClientboundPositionPacket::new);

    public ClientboundPositionPacket(FriendlyByteBuf buf) {
        this(parse(buf));
    }

    private static Set<BlockPos> parse(FriendlyByteBuf buf) {
        var set = new HashSet<BlockPos>();
        buf.readCollection(HashSet::new, FriendlyByteBuf::readBlockPos);
        return set;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(positions, FriendlyByteBuf::writeBlockPos);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
