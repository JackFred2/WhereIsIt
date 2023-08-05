package red.jackf.whereisit.networking;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchResult;

import java.util.HashSet;
import java.util.Set;

/**
 * Buffer format:
 * <li>position: BlockPos</li>
 * <li>hasItemDetails: boolean</li>
 * <li>if (hasItemDetails) item: ItemStack</li>
 * <li>hasCustomName: boolean</li>
 * <li>if (hasCustomName) name: Component</li>
 * <li>if (hasCustomName) hasCustomNameOffset: boolean</li>
 * <li>if (hasCustomName && hasCustomNameOffset) nameOffset: 3 * double</li>
 */
public record ClientboundResultsPacket(long id, Set<SearchResult> results) implements FabricPacket {
    public static final PacketType<ClientboundResultsPacket> TYPE = PacketType.create(WhereIsIt.id("c2s_searchforitem"), ClientboundResultsPacket::new);

    public ClientboundResultsPacket(FriendlyByteBuf buf) {
        this(buf.readLong(), parse(buf));
    }

    private static Set<SearchResult> parse(FriendlyByteBuf buf) {
        return buf.readCollection(HashSet::new, bbuf -> {
            var result = SearchResult.builder(bbuf.readBlockPos());
            if (bbuf.readBoolean())
                result.item(bbuf.readItem());
            if (bbuf.readBoolean()) result.name(
                    bbuf.readComponent(),
                    bbuf.readBoolean() ?
                            new Vec3(bbuf.readDouble(), bbuf.readDouble(), bbuf.readDouble()) : null);
            return result.build();
        });
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(id);
        buf.writeCollection(results, (bbuf, result) -> {
            bbuf.writeBlockPos(result.pos());
            bbuf.writeBoolean(result.item() != null);
            if (result.item() != null) bbuf.writeItem(result.item());
            bbuf.writeBoolean(result.name() != null);
            if (result.name() != null) {
                bbuf.writeComponent(result.name());
                bbuf.writeBoolean(result.customNameOffset() != null);
                if (result.customNameOffset() != null) {
                    bbuf.writeDouble(result.nameOffset().x);
                    bbuf.writeDouble(result.nameOffset().y);
                    bbuf.writeDouble(result.nameOffset().z);
                }
            }
        });
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
