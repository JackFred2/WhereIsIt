package red.jackf.whereisit.util;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public interface Codecs {
    StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC3 = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            vec3 -> vec3.x,
            ByteBufCodecs.DOUBLE,
            vec3 -> vec3.y,
            ByteBufCodecs.DOUBLE,
            vec3 -> vec3.z,
            Vec3::new
    );
}
