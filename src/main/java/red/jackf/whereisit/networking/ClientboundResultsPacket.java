package red.jackf.whereisit.networking;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchResult;

import java.util.HashSet;
import java.util.Set;

/**
 * Buffer format:
 * position: BlockPos
 * hasItemDetails: boolean
 * IF hasItemDetails:
 * item: ItemStack
 */
public record ClientboundResultsPacket(long id, Set<SearchResult> results) implements FabricPacket {
    public static final PacketType<ClientboundResultsPacket> TYPE = PacketType.create(WhereIsIt.id("c2s_searchforitem"), ClientboundResultsPacket::new);

    public ClientboundResultsPacket(FriendlyByteBuf buf) {
        this(buf.readLong(), parse(buf));
    }

    private static Set<SearchResult> parse(FriendlyByteBuf buf) {
        return buf.readCollection(HashSet::new, bbuf -> {
            var pos = bbuf.readBlockPos();
            ItemStack item = null;
            if (bbuf.readBoolean()) item = bbuf.readItem();
            return new SearchResult(pos, item);
        });
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(id);
        buf.writeCollection(results, (bbuf, result) -> {
            bbuf.writeBlockPos(result.pos());
            if (result.item() != null) {
                bbuf.writeBoolean(true);
                bbuf.writeItem(result.item());
            } else {
                bbuf.writeBoolean(false);
            }
        });
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
