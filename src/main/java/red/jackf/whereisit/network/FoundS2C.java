package red.jackf.whereisit.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.utilities.FoundType;
import red.jackf.whereisit.utilities.SearchResult;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structure:
 * <ul>
 * <li> int: size of results
 * </ul>
 * for each result:
 * <ul>
 * <li> BlockPos: position
 * <li> FoundType: type of finding (either normal or deep)
 * <li> boolean: if there's a custom name
 * <li> (if has a custom name): Text: custom name
 * </ul>
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
        Map<BlockPos, SearchResult> results = new LinkedHashMap<>();
        int max = buf.readInt();
        for (int i = 0; i < max; i++) {
            results.put(buf.readBlockPos(), new SearchResult(buf.readEnumConstant(FoundType.class), buf.readBoolean() ? buf.readText() : null));
        }
        return results;
    }
}
