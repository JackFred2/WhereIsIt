package red.jackf.randomadditions;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class RandomAdditions implements ModInitializer {
	public static final String MODID = "randomadditions";

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Hello Fabric world!");
	}
}
