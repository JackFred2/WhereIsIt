package red.jackf.whereisit.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchRequest;

public record ServerboundSearchForItemPacket(long id, SearchRequest request) implements CustomPacketPayload {
    public static final Type<ServerboundSearchForItemPacket> TYPE = new Type<>(WhereIsIt.id("c2s_searchforitem"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSearchForItemPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            ServerboundSearchForItemPacket::id,
            ByteBufCodecs.COMPOUND_TAG.map(SearchRequest::load, SearchRequest::pack),
            ServerboundSearchForItemPacket::request,
            ServerboundSearchForItemPacket::new
    );

    @Override
    public @NotNull Type<ServerboundSearchForItemPacket> type() {
        return TYPE;
    }
}
