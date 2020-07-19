package red.jackf.whereisit.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import red.jackf.whereisit.FoundType;

import java.util.HashMap;
import java.util.Map;

public class FoundS2C extends PacketByteBuf {
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
