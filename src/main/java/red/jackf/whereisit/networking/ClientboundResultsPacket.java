package red.jackf.whereisit.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

/**
 * Buffer format:
 * <li>id: long</li>
 * For each result:
 * <li>position: BlockPos</li>
 * <li>hasItemDetails: boolean</li>
 * <li>if (hasItemDetails) item: ItemStack</li>
 * <li>hasCustomName: boolean</li>
 * <li>if (hasCustomName) name: Component</li>
 * <li>if (hasCustomName) hasCustomNameOffset: boolean</li>
 * <li>if (hasCustomName && hasCustomNameOffset) nameOffset: 3 * double</li>
 * <li>numberOfOtherPositions: Collection&lt;BlockPos&gt;</li>
 * After:
 * <li>hasRequest: boolean</li>
 * <li>if (hasRequest) request: SearchRequest</li>
 */
public record ClientboundResultsPacket(long id, Collection<SearchResult> results, @Nullable SearchRequest request) implements CustomPacketPayload {
    public static final Type<ClientboundResultsPacket> TYPE = new Type<>(WhereIsIt.id("s2c_founditem"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundResultsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            ClientboundResultsPacket::id,
            SearchResult.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)),
            ClientboundResultsPacket::results,
            ByteBufCodecs.COMPOUND_TAG.map(SearchRequest::load, SearchRequest::pack).apply(ByteBufCodecs::optional).map(opt -> opt.orElse(null), Optional::ofNullable),
            ClientboundResultsPacket::request,
            ClientboundResultsPacket::new
    );

    public static final long WHEREIS_COMMAND_ID = -1L;

    @Override
    public Type<ClientboundResultsPacket> type() {
        return TYPE;
    }
}
