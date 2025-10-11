package red.jackf.whereisit.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.util.*;

@SuppressWarnings("resource")
public class Rendering {
    private static final Map<BlockPos, SearchResult> results = new HashMap<>();
    private static final Map<BlockPos, SearchResult> namedResults = new HashMap<>();
    private static final List<ScheduledLabel> scheduledLabels = new ArrayList<>();

    private record ScheduledLabel(Vec3 position, Component text, boolean seeThrough) {}

    private static long ticksSinceSearch = 0;
    @Nullable
    private static SearchRequest lastRequest = null;

    public static void setup() {
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            if (!shouldBeRendering() || !WhereIsItConfig.INSTANCE.instance().getClient().showContainerNamesInResults)
                return;

            for (SearchResult value : namedResults.values()) {
                scheduleLabel(value.pos().getCenter().add(value.nameOffset()), value.name(), WhereIsItConfig.INSTANCE.instance().getCommon().debug.labelsAreSeeThrough);
            }
        });

        InvalidateRenderStateCallback.EVENT.register(new InvalidateRenderStateCallback() {
            @Override
            public void onInvalidate() {
                scheduledLabels.clear();
            }
        });
    }

    private static boolean shouldBeRendering() {
        return ticksSinceSearch <= WhereIsItConfig.INSTANCE.instance().getCommon().fadeoutTimeTicks;
    }

    public static void addResults(Collection<SearchResult> newResults) {
        for (SearchResult result : newResults) {
            results.put(result.pos(), result);
            if (result.name() != null) namedResults.put(result.pos(), result);
        }
    }

    public static void clearResults() {
        lastRequest = null;
        results.clear();
        namedResults.clear();
    }

    public static void setLastRequest(@Nullable SearchRequest request) {
        lastRequest = request;
    }

    public static long getTicksSinceSearch() {
        return ticksSinceSearch;
    }

    public static void incrementTicksSinceSearch() {
        ticksSinceSearch++;
    }

    public static void resetSearchTime() {
        ticksSinceSearch = 0;
    }

    public static Map<BlockPos, SearchResult> getResults() {
        return results;
    }

    public static Map<BlockPos, SearchResult> getNamedResults() {
        return namedResults;
    }

    // ----------------------------
    // SLOT HIGHLIGHTING
    // ----------------------------
    public static void renderSlotHighlight(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        if (!shouldBeRendering() || lastRequest == null) return;

        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            float time = ((ticksSinceSearch + tickDelta) * WhereIsItConfig.INSTANCE.instance().getClient().highlightTimeFactor) % 80 / 80f;

            for (Slot slot : containerScreen.getMenu().slots) {
                if (!slot.isActive() || !slot.hasItem()) continue;
                if (!SearchRequest.check(slot.getItem(), lastRequest)) continue;

                int x = slot.x + containerScreen.leftPos;
                int y = slot.y + containerScreen.topPos;
                float progress = time + (slot.x / 256f) * WhereIsItConfig.INSTANCE.instance().getClient().slotHighlightXFactor;
                progress -= ((mouseX + mouseY) / 1280f) * WhereIsItConfig.INSTANCE.instance().getClient().slotHighlightMouseFactor;

                int colour = CurrentGradientHolder.getColour(progress);

                // Используем метод fill объекта GuiGraphics
                graphics.fill(x, y, x + 16, y + 16, colour);
            }
        }
    }


    // ----------------------------
    // LABEL RENDERING
    // ----------------------------
    public static void scheduleLabel(Vec3 pos, Component name, boolean seeThrough) {
        if (pos == null || name == null) return;
        scheduledLabels.add(new ScheduledLabel(pos, name, seeThrough));
    }

    public static void renderLabels(PoseStack poseStack, Camera camera, MultiBufferSource consumers) {
        scheduledLabels.stream()
                .sorted(Comparator.comparingDouble(label -> -camera.getPosition().distanceToSqr(label.position)))
                .forEach(label -> renderLabel(label, poseStack, camera, consumers));
        scheduledLabels.clear();
    }

    private static void renderLabel(ScheduledLabel label, PoseStack pose, Camera camera, MultiBufferSource consumers) {
        pose.pushPose();
        Vec3 pos = label.position.subtract(camera.getPosition());

        // Переносим в мировые координаты
        pose.translate(pos.x, pos.y, pos.z);
        pose.mulPose(camera.rotation());

        // Масштабируем текст
        float factor = 0.025f * WhereIsItConfig.INSTANCE.instance().getClient().containerNameLabelScale;
        pose.scale(factor, -factor, factor);

        int width = Minecraft.getInstance().font.width(label.text);
        float x = -width / 2f;

        // Получаем буфер для рендера фона
        VertexConsumer bgBuffer = consumers.getBuffer(RenderType.textBackgroundSeeThrough());
        int bgColour = ((int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255F)) << 24;

        // Рисуем фон
        bgBuffer.addVertex(x - 1, -1f, 0).setColor(bgColour, bgColour, bgColour, bgColour);
        bgBuffer.addVertex(x - 1, 10f, 0).setColor(bgColour, bgColour, bgColour, bgColour);
        bgBuffer.addVertex(x + width, 10f, 0).setColor(bgColour, bgColour, bgColour, bgColour);
        bgBuffer.addVertex(x + width, -1f, 0).setColor(bgColour, bgColour, bgColour, bgColour);

        // Отключаем тест глубины через LWJGL
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_ALWAYS);

        // Рендерим текст
        Font.DisplayMode mode = label.seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
        Minecraft.getInstance().font.drawInBatch(
                label.text, x, 0, 0xFFFFFFFF, false, pose.last().pose(), consumers, mode, 0, LightTexture.FULL_BRIGHT
        );

        // Включаем тест глубины обратно
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        pose.popPose();
    }
}
