package red.jackf.whereisit.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import red.jackf.whereisit.WhereIsIt;

/**
 * Item results packet; now with version number so new classes should not be needed.
 */
public class ResultsS2C extends PacketByteBuf {
    public static final Identifier ID = WhereIsIt.id("results_s2c");

    public ResultsS2C(ByteBuf parent) {
        super(parent);
    }
}
