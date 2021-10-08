package red.jackf.whereisit.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.utilities.FoundType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class PositionData {
    public final BlockPos pos;
    public final long time;
    public final VoxelShape shape;
    public float r;
    public float g;
    public float b;
    private List<Text> texts = null;

    public PositionData(BlockPos pos, long time, VoxelShape shape, float r, float g, float b, @Nullable Text initialName) {
        this.pos = pos;
        this.time = time;
        this.shape = shape;
        this.r = r;
        this.g = g;
        this.b = b;

        if (initialName != null) {
            assertTextList();
            addText(initialName);
        }
    }

    public static PositionData from(BlockPos pos, long time, VoxelShape shape, FoundType type, @Nullable Text name) {
        if (type == FoundType.FOUND_DEEP) {
            return new PositionData(pos, time, shape,
                ((WhereIsIt.CONFIG.getAlternateColour() >> 16) & 0xff) / 255f,
                ((WhereIsIt.CONFIG.getAlternateColour() >> 8) & 0xff) / 255f,
                ((WhereIsIt.CONFIG.getAlternateColour()) & 0xff) / 255f,
                name);
        } else {
            return new PositionData(pos, time, shape,
                ((WhereIsIt.CONFIG.getColour() >> 16) & 0xff) / 255f,
                ((WhereIsIt.CONFIG.getColour() >> 8) & 0xff) / 255f,
                ((WhereIsIt.CONFIG.getColour()) & 0xff) / 255f,
                name);
        }
    }

    private void assertTextList() {
        if (texts == null) texts = new ArrayList<>();
    }

    public List<Text> getAllText() {
        return texts == null ? Collections.emptyList() : texts;
    }

    public void removeText(int index) {
        if (texts != null) texts.remove(index);
    }

    public void setText(int index, Text text) {
        assertTextList();
        texts.set(index, text);
    }

    public void addText(int index, Text text) {
        assertTextList();
        texts.add(index, text);
    }

    public void addText(Text text) {
        assertTextList();
        texts.add(text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PositionData that = (PositionData) o;
        return Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }
}
