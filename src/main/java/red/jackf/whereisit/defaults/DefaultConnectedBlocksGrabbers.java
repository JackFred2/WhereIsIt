package red.jackf.whereisit.defaults;

import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

public class DefaultConnectedBlocksGrabbers {
    static void setup() {
        setupDoubleChests();
    }

    private static void setupDoubleChests() {
        // Double [Trapped] Chests
        ConnectedBlocksGrabber.EVENT.register(((positions, pos, level, state) -> {
            if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE)
                positions.add(pos.relative(ChestBlock.getConnectedDirection(state)));
        }));
    }
}
