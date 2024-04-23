package red.jackf.whereisit.client.util;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.*;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.api.SearchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TextUtil {

    public static List<Component> prettyPrint(Tag root) {
        var text = new ArrayList<Component>();

        text.add(Component.literal("Search Request:"));

        if (root instanceof CompoundTag compound) {
            prettyPrintCompound(text::add, null, 0, compound);
        } else {
            // wrap for ease
            CompoundTag wrapped = new CompoundTag();
            wrapped.put("request", root);
            prettyPrintCompound(text::add, null, 0, wrapped);
        }

        return text;
    }

    private static MutableComponent white(String str) {
        return Component.literal(str).withStyle(ChatFormatting.WHITE);
    }

    private static final ChatFormatting[] COLOURS = new ChatFormatting[]{
            ChatFormatting.RED,
            ChatFormatting.GOLD,
            ChatFormatting.YELLOW,
            ChatFormatting.GREEN,
            ChatFormatting.DARK_GREEN,
            ChatFormatting.AQUA,
            ChatFormatting.BLUE,
            ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.DARK_PURPLE
    };
    private static MutableComponent byIndent(String str, int indentLevel) {
        var format = COLOURS[indentLevel % COLOURS.length];
        return Component.literal(str).withStyle(format);
    }

    private static void prettyPrintCompound(Consumer<Component> consumer, @Nullable MutableComponent firstPrefix, int indentLevel, CompoundTag tag) {
        final boolean[] doneFirst = {false};
        tag.getAllKeys().stream().sorted((s1, s2) -> s1.equals(SearchRequest.ID) ? -1 : s2.equals(SearchRequest.ID) ? 1 : 0).forEach(key ->  {
            Tag value = tag.get(key);
            var prefix = byIndent("  ".repeat(indentLevel) + key, indentLevel).append(white(": "));
            if (!doneFirst[0] && firstPrefix != null) {
                doneFirst[0] = true;
                prefix = firstPrefix.append(byIndent(key, indentLevel)).append(white(":  "));
            }
            if (value instanceof CompoundTag compound) {
                consumer.accept(prefix);
                prettyPrintCompound(consumer, null, indentLevel + 1, compound);
            } else if (value instanceof ListTag list) {
                consumer.accept(prefix.append(white("")));
                prettyPrintList(consumer,  indentLevel + 1, list);
            } else {
                prettyPrintValue(consumer, prefix, value);
            }
        });
    }

    private static void prettyPrintList(Consumer<Component> consumer, int indentLevel, ListTag rootList) {
        for (Tag tag : rootList) {
            var prefix = white("  ".repeat(Math.max(0, indentLevel - 1)) + "* ");
            if (tag instanceof CompoundTag compound) {
                prettyPrintCompound(consumer, prefix, indentLevel, compound);
            } else if (tag instanceof ListTag list) {
                prettyPrintList(consumer,  indentLevel + 1, list);
            } else {
                prettyPrintValue(consumer, prefix, tag);
            }
        }
    }

    private static void prettyPrintValue(Consumer<Component> consumer, MutableComponent prefix, Tag tag) {
        consumer.accept(prefix.append(white("").append(NbtUtils.toPrettyComponent(tag))));
    }
}
