package red.jackf.whereisit.network;

import io.netty.buffer.Unpooled;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SearchC2S extends PacketByteBuf {
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

    public static class Context {
        private final Item item;
        private final boolean matchNbt;
        @Nullable
        private final NbtCompound tag;

        public Context(Item item, boolean matchNbt, @Nullable NbtCompound tag) {
            this.item = item;
            this.matchNbt = matchNbt;
            this.tag = tag;
        }

        public Item getItem() {
            return item;
        }

        public boolean matchNbt() {
            return matchNbt;
        }

        @Nullable
        public NbtCompound getTag() {
            return tag;
        }
    }
}
