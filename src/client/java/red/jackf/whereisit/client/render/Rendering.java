package red.jackf.whereisit.client.render;

import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
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
                scheduleLabel(value.pos().getCenter().add(value.nameOffset()), value.name(),
                        WhereIsItConfig.INSTANCE.instance().getCommon().debug.labelsAreSeeThrough);
            }
        });

        InvalidateRenderStateCallback.EVENT.register(scheduledLabels::clear);
    }

    public static boolean shouldBeRendering() {
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

    public static long getTicksSinceSearch() { return ticksSinceSearch; }
    public static void incrementTicksSinceSearch() { ticksSinceSearch++; }
    public static void resetSearchTime() { ticksSinceSearch = 0; }
    public static Map<BlockPos, SearchResult> getResults() { return results; }
    public static Map<BlockPos, SearchResult> getNamedResults() { return namedResults; }

    // ----------------------------
    // SLOT HIGHLIGHTING (in AbstractContainerScreenMixin Mixin)
    // ----------------------------
    public static void renderSlotHighlight(AbstractContainerScreen<?> screen, GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        if (!shouldBeRendering() || lastRequest == null) return;

        float time = getBaseProgress(ticksSinceSearch, tickDelta);

        for (Slot slot : screen.getMenu().slots) {
            if (!slot.isActive() || !slot.hasItem()) continue;
            if (!SearchRequest.check(slot.getItem(), lastRequest)) continue;

            int x = slot.x;
            int y = slot.y;

            float progress = time;
            progress += (slot.x / 256f) * WhereIsItConfig.INSTANCE.instance().getClient().slotHighlightXFactor;
            int colour = CurrentGradientHolder.getColour(progress);
            graphics.fill(x, y, x + 16, y + 16, colour);
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
        pose.translate(pos.x, pos.y, pos.z);
        pose.mulPose(camera.rotation());

        float factor = 0.025f * WhereIsItConfig.INSTANCE.instance().getClient().containerNameLabelScale;
        pose.scale(factor, -factor, factor);

        Matrix4f matrix = pose.last().pose();
        int width = Minecraft.getInstance().font.width(label.text);
        float x = -width / 2f;

        VertexConsumer bgBuffer = consumers.getBuffer(RenderType.textBackgroundSeeThrough());
        int bgColour = ((int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255F)) << 24;

        bgBuffer.addVertex(matrix, x - 1, -1f, 0).setColor(bgColour).setLight(LightTexture.FULL_BRIGHT);
        bgBuffer.addVertex(matrix, x - 1, 10f, 0).setColor(bgColour).setLight(LightTexture.FULL_BRIGHT);
        bgBuffer.addVertex(matrix, x + width, 10f, 0).setColor(bgColour).setLight(LightTexture.FULL_BRIGHT);
        bgBuffer.addVertex(matrix, x + width, -1f, 0).setColor(bgColour).setLight(LightTexture.FULL_BRIGHT);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_ALWAYS);

        Font.DisplayMode mode = label.seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
        Minecraft.getInstance().font.drawInBatch(
                label.text, x, 0, 0xFFFFFFFF, false, matrix, consumers, mode, 0, LightTexture.FULL_BRIGHT
        );

        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        pose.popPose();
    }

    // ----------------------------
    // BLOCK BOX RENDERING (FILLED CUBES)
    // ----------------------------
    public static void renderBoxes(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, float tickDelta) {
        if (results.isEmpty()) return;

        Vec3 camPos = camera.getPosition();

        // Создаем новый PoseStack и применяем вращение камеры
        PoseStack pose = new PoseStack();
        pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(camera.getXRot()));
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(camera.getYRot() - 180f));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.debugQuads());

        // Получаем прогресс для RGB анимации
        float progress = getRenderingProgress(tickDelta);

        // Получаем RGB цвет из градиента (как в старом коде)
        int rgbColor = CurrentGradientHolder.getColour(getBaseProgress(ticksSinceSearch, tickDelta));
        float r = ARGB.red(rgbColor) / 255f;
        float g = ARGB.green(rgbColor) / 255f;
        float b = ARGB.blue(rgbColor) / 255f;

        // Альфа с учетом fadeout (от 100% до 50%)
        float baseAlpha = 0.4f;
        float alpha = baseAlpha - (progress * baseAlpha / 2f);

        // Масштаб для анимации (как в старом коде)
        float scale = easingFunc(progress);

        for (SearchResult result : getResults().values()) {
            // Рендерим основной бокс с RGB цветом
            renderBox(camPos, result.pos(), consumer, pose, r, g, b, alpha, scale);

            // Рендерим дополнительные позиции (для двойных сундуков)
            for (BlockPos otherPos : result.otherPositions()) {
                renderBox(camPos, otherPos, consumer, pose, r, g, b, alpha, scale);
            }
        }

        bufferSource.endBatch(RenderType.debugQuads());
    }

    // Прогресс рендеринга для fadeout
    private static float getRenderingProgress(float tickDelta) {
        return Math.min((getTicksSinceSearch() + tickDelta) / WhereIsItConfig.INSTANCE.instance().getCommon().fadeoutTimeTicks, 1f);
    }

    // Базовый прогресс для RGB анимации (как в старом коде)
    private static float getBaseProgress(long ticks, float delta) {
        float base = ticks + delta;
        base *= WhereIsItConfig.INSTANCE.instance().getClient().highlightTimeFactor;
        return (base % 80) / 80;
    }

    // Функция сглаживания для анимации масштаба (как в старом коде)
    private static float easingFunc(float progress) {
        var power = 32f;
        return (float) ((1 - Math.pow(progress, power)) * (1 - Math.pow(1 - progress, power)) * (1 - (progress / 4f)));
    }

    // Обновленный метод renderBox с поддержкой масштаба
    private static void renderBox(Vec3 cameraPos, BlockPos pos, VertexConsumer consumer,
                                  PoseStack pose, float r, float g, float b, float a, float scale) {
        pose.pushPose();

        // Смещение от камеры для правильной позиции
        final double xOffset = pos.getX() + (0.5 - cameraPos.x);
        final double yOffset = pos.getY() + (0.5 - cameraPos.y);
        final double zOffset = pos.getZ() + (0.5 - cameraPos.z);
        pose.translate(xOffset, yOffset, zOffset);

        // Масштабируем куб с анимацией
        pose.scale(scale * 0.5f, scale * 0.5f, scale * 0.5f);

        Matrix4f matrix = pose.last().pose();
        int color = ARGB.color((int)(a * 255), (int)(r * 255), (int)(g * 255), (int)(b * 255));

        // 6 граней куба - каждая грань это 4 вершины

        // Грань -Z (север)
        consumer.addVertex(matrix, -1, -1, -1).setColor(color);
        consumer.addVertex(matrix, -1, 1, -1).setColor(color);
        consumer.addVertex(matrix, 1, 1, -1).setColor(color);
        consumer.addVertex(matrix, 1, -1, -1).setColor(color);

        // Грань +Z (юг)
        consumer.addVertex(matrix, -1, -1, 1).setColor(color);
        consumer.addVertex(matrix, 1, -1, 1).setColor(color);
        consumer.addVertex(matrix, 1, 1, 1).setColor(color);
        consumer.addVertex(matrix, -1, 1, 1).setColor(color);

        // Грань -Y (низ)
        consumer.addVertex(matrix, -1, -1, -1).setColor(color);
        consumer.addVertex(matrix, 1, -1, -1).setColor(color);
        consumer.addVertex(matrix, 1, -1, 1).setColor(color);
        consumer.addVertex(matrix, -1, -1, 1).setColor(color);

        // Грань +Y (верх)
        consumer.addVertex(matrix, -1, 1, -1).setColor(color);
        consumer.addVertex(matrix, -1, 1, 1).setColor(color);
        consumer.addVertex(matrix, 1, 1, 1).setColor(color);
        consumer.addVertex(matrix, 1, 1, -1).setColor(color);

        // Грань -X (запад)
        consumer.addVertex(matrix, -1, -1, -1).setColor(color);
        consumer.addVertex(matrix, -1, -1, 1).setColor(color);
        consumer.addVertex(matrix, -1, 1, 1).setColor(color);
        consumer.addVertex(matrix, -1, 1, -1).setColor(color);

        // Грань +X (восток)
        consumer.addVertex(matrix, 1, -1, -1).setColor(color);
        consumer.addVertex(matrix, 1, 1, -1).setColor(color);
        consumer.addVertex(matrix, 1, 1, 1).setColor(color);
        consumer.addVertex(matrix, 1, -1, 1).setColor(color);

        pose.popPose();
    }
}
