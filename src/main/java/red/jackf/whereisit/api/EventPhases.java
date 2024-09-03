package red.jackf.whereisit.api;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.ResourceLocation;
import red.jackf.whereisit.WhereIsIt;

public interface EventPhases {
    ResourceLocation PRIORITY = WhereIsIt.id("priority");
    ResourceLocation DEFAULT = Event.DEFAULT_PHASE;
    ResourceLocation FALLBACK = WhereIsIt.id("fallback");
}
