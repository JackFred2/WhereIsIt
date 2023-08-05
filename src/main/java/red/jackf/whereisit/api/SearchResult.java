package red.jackf.whereisit.api;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SearchResult {
    private final BlockPos pos;
    private final @Nullable ItemStack item;
    private final @Nullable Component name;
    private final @Nullable Vec3 nameOffset;
    private final List<BlockPos> otherPositions = new ArrayList<>();
    private static final Vec3 DEFAULT_OFFSET = new Vec3(0, 1, 0);

    private SearchResult(BlockPos pos, @Nullable ItemStack item, @Nullable Component name, @Nullable Vec3 nameOffset) {
        this.pos = pos;
        this.item = item;
        this.name = name;
        this.nameOffset = nameOffset;
    }

    public static Builder builder(BlockPos pos) {
        return new Builder(pos.immutable());
    }

    public BlockPos pos() {
        return pos;
    }

    public @Nullable ItemStack item() {
        return item;
    }

    public @Nullable Component name() {
        return name;
    }

    public @Nullable Vec3 customNameOffset() {
        return nameOffset;
    }

    public Vec3 nameOffset() {
        return nameOffset == null ? DEFAULT_OFFSET : nameOffset;
    }

    public static class Builder {
        private final BlockPos pos;
        private @Nullable ItemStack item;
        private @Nullable Component name;
        private @Nullable Vec3 nameOffset;

        private Builder(BlockPos pos) {
            this.pos = pos;
        }

        public Builder item(ItemStack item) {
            this.item = item;
            return this;
        }

        public Builder name(Component name, Vec3 offset) {
            this.name = name;
            this.nameOffset = offset;
            return this;
        }

        public SearchResult build() {
            return new SearchResult(pos, item, name, nameOffset);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SearchResult) obj;
        return Objects.equals(this.pos, that.pos) &&
                Objects.equals(this.item, that.item) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.nameOffset, that.nameOffset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, item, name, nameOffset);
    }

    @Override
    public String toString() {
        return "SearchResult[" +
                "pos=" + pos + ", " +
                "item=" + item + ", " +
                "name=" + name + ", " +
                "nameOffset=" + nameOffset + ']';
    }
}
