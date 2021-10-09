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

/**
 * Structure:
 * <ul>
 * <li> Identifier: Item identifier to search
 * <li> boolean: Is the search NBT-Sensitive
 * <li> (if NBT-Sensitive) NbtCompound: Nbt to search with
 * <li> SearchMode: Which results to prioritize
 * <li> int: Maximum to return
 * </ul>
 */

public class SearchC2S extends PacketByteBuf {
    public static final Identifier ID = WhereIsIt.id("find_item_c2s");

    public SearchC2S(@NotNull Item toFind, boolean matchNbt, @Nullable NbtCompound nbtCompound, int maximum) {
        super(Unpooled.buffer());
        this.writeIdentifier(Registry.ITEM.getId(toFind));
        this.writeBoolean(matchNbt);
        if (matchNbt) {
            this.writeNbt(nbtCompound);
        }
        this.writeInt(maximum);
    }

    public static Context read(PacketByteBuf buf) {
        Item item = Registry.ITEM.get(buf.readIdentifier());
        boolean matchNbt = buf.readBoolean();
        NbtCompound tag = matchNbt ? buf.readNbt() : null;
        int maximum = buf.readInt();
        return new Context(item, matchNbt, tag, maximum);
    }

    public record Context(Item item, boolean matchNbt, @Nullable NbtCompound tag, int maximum) {
    }
}
