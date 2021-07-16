package red.jackf.whereisit.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import red.jackf.whereisit.utilities.FoundType;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.utilities.SearchResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Structure:
 *
 * int: size of results
 * for each result:
 *   BlockPos: position
 *   FoundType: type of finding (either normal or deep)
 *   boolean: if there's a custom name
 *   (if above bool is true): Text: custom name
 */
public class FoundS2C extends PacketByteBuf {
    public static final Identifier ID = WhereIsIt.id("found_item_s2c");

    public FoundS2C(Map<BlockPos, SearchResult> results) {
        super(Unpooled.buffer());
        this.writeInt(results.size());
        for (Map.Entry<BlockPos, SearchResult> entry : results.entrySet()) {
            this.writeBlockPos(entry.getKey());
            this.writeEnumConstant(entry.getValue().foundType());
            boolean hasName = entry.getValue().name() != null;
            this.writeBoolean(hasName);
            if (hasName) this.writeText(entry.getValue().name());
        }
    }

    public static Map<BlockPos, SearchResult> read(PacketByteBuf buf) {
        Map<BlockPos, SearchResult> results = new HashMap<>();
        int max = buf.readInt();
        for (int i = 0; i < max; i++) {
            results.put(buf.readBlockPos(), new SearchResult(buf.readEnumConstant(FoundType.class), buf.readBoolean() ? buf.readText() : null));
        }
        return results;
    }
}
