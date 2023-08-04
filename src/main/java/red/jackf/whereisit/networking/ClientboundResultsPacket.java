package red.jackf.whereisit.networking;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
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
 * <li>if (hasCustomName) nameOffset: 3 * double</li>
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
            Component name = null;
            Vec3 nameOffset = null;
            if (bbuf.readBoolean()) item = bbuf.readItem();
            if (bbuf.readBoolean()) {
                name = bbuf.readComponent();
                nameOffset = new Vec3(bbuf.readDouble(), bbuf.readDouble(), bbuf.readDouble());
            }
            return new SearchResult(pos, item, name, nameOffset);
        });
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(id);
        buf.writeCollection(results, (bbuf, result) -> {
            bbuf.writeBlockPos(result.pos());
            bbuf.writeBoolean(result.item() != null);
            if (result.item() != null) bbuf.writeItem(result.item());
            bbuf.writeBoolean(result.name() != null && result.nameOffset() != null);
            if (result.name() != null && result.nameOffset() != null) {
                bbuf.writeComponent(result.name());
                bbuf.writeDouble(result.nameOffset().x);
                bbuf.writeDouble(result.nameOffset().y);
                bbuf.writeDouble(result.nameOffset().z);
            }
        });
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
