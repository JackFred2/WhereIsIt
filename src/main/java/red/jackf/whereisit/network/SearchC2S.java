package red.jackf.whereisit.network;

import io.netty.buffer.Unpooled;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;

public class SearchC2S extends PacketByteBuf {
    public static final Identifier ID = WhereIsIt.id("find_item_c2s");

    public SearchC2S(@NotNull Item toFind, boolean matchNbt, @Nullable NbtCompound nbtCompound) {
        super(Unpooled.buffer());
        this.writeIdentifier(Registry.ITEM.getId(toFind));
        this.writeBoolean(matchNbt);
        if (matchNbt) {
            this.writeNbt(nbtCompound);
        }
    }

    public static Context read(PacketByteBuf buf) {
        Item item = Registry.ITEM.get(buf.readIdentifier());
        boolean matchNbt = buf.readBoolean();
        NbtCompound tag = matchNbt ? buf.readNbt() : null;
        return new Context(item, matchNbt, tag);
    }

    public record Context(Item item, boolean matchNbt, @Nullable NbtCompound tag) {
    }
}
