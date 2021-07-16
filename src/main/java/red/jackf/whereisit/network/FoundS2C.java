package red.jackf.whereisit.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import red.jackf.whereisit.FoundType;
import red.jackf.whereisit.WhereIsIt;

import java.util.HashMap;
import java.util.Map;

/**
 * Old results class for backwards compatibility. Will be removed sometime in the future when I'm confident everyone's
 * updated.
 */
@Deprecated
public class FoundS2C extends PacketByteBuf {
    public static final Identifier ID = WhereIsIt.id("found_item_s2c");

    public FoundS2C(Map<BlockPos, FoundType> results) {
        super(Unpooled.buffer());
        this.writeInt(results.size());
        for (Map.Entry<BlockPos, FoundType> entry : results.entrySet()) {
            this.writeBlockPos(entry.getKey());
            this.writeEnumConstant(entry.getValue());
        }
    }

    public static Map<BlockPos, FoundType> read(PacketByteBuf buf) {
        Map<BlockPos, FoundType> results = new HashMap<>();
        int max = buf.readInt();
        for (int i = 0; i < max; i++) {
            results.put(buf.readBlockPos(), buf.readEnumConstant(FoundType.class));
        }
        return results;
    }
}
