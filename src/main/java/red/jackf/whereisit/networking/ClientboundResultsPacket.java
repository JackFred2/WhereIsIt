package red.jackf.whereisit.networking;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchResult;

import java.util.Collection;
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
 * <li>numberOfOtherPositions: Collection&lt;BlockPos&gt;</li>
 */
public record ClientboundResultsPacket(long id, Collection<SearchResult> results) implements FabricPacket {
    public static final PacketType<ClientboundResultsPacket> TYPE = PacketType.create(WhereIsIt.id("c2s_searchforitem"), ClientboundResultsPacket::new);
    public static final long WHEREIS_COMMAND_ID = -1L;

    public ClientboundResultsPacket(FriendlyByteBuf buf) {
        this(buf.readLong(), parse(buf));
    }

    private static Set<SearchResult> parse(FriendlyByteBuf bbuf) {
        return bbuf.readCollection(HashSet::new, bbuf2 -> {
            var result = SearchResult.builder(bbuf2.readBlockPos());
            if (bbuf2.readBoolean())
                result.item(bbuf2.readItem());
            if (bbuf2.readBoolean()) result.name(
                    bbuf2.readComponent(),
                    bbuf2.readBoolean() ?
                            new Vec3(bbuf2.readDouble(), bbuf2.readDouble(), bbuf2.readDouble()) : null);
            result.otherPositions(bbuf2.readCollection(HashSet::new, FriendlyByteBuf::readBlockPos));
            return result.build();
        });
    }

    @Override
    public void write(FriendlyByteBuf bbuf) {
        bbuf.writeLong(id);
        bbuf.writeCollection(results, (bbuf2, result) -> {
            bbuf2.writeBlockPos(result.pos());
            bbuf2.writeBoolean(result.item() != null);
            if (result.item() != null) bbuf2.writeItem(result.item());
            bbuf2.writeBoolean(result.name() != null);
            if (result.name() != null) {
                bbuf2.writeComponent(result.name());
                bbuf2.writeBoolean(result.customNameOffset() != null);
                if (result.customNameOffset() != null) {
                    bbuf2.writeDouble(result.nameOffset().x);
                    bbuf2.writeDouble(result.nameOffset().y);
                    bbuf2.writeDouble(result.nameOffset().z);
                }
            }
            bbuf2.writeCollection(result.otherPositions(), FriendlyByteBuf::writeBlockPos);
        });
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
