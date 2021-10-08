/**
 * This file is originally written by zml2008 for WorldEditCUI, located at https://github.com/mikroskeem/WorldEditCUI/blob/72dd3dd048237b16878fcd8e1f0bdafd5844d7ef/src/main/java/eu/mikroskeem/worldeditcui/OptifineHooks.java
 * <p>
 * This file is licensed under the Eclipse Public License 1.0.
 */

package red.jackf.whereisit.compat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiConsumer;

/**
 * Optifine shaders uses a lot more frame buffers to render the world -- we have to make sure we're on the right one.
 *
 * @see <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt">the shaders documentation</a>
 */
@Environment(EnvType.CLIENT)
public final class OptifineHooks {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // For inspection purposes, get the active program
    private static final MethodHandle SHADERS_ACTIVE_PROGRAM;
    private static final MethodHandle PROGRAM_GET_NAME;

    // Our part of the rendering pipeline -- we want to disable textures (maybe more?)
    private static final MethodHandle CONFIG_IS_SHADERS;
    private static final MethodHandle SHADERS_BEGIN_LEASH;
    private static final MethodHandle SHADERS_END_LEASH;
    private static final MethodHandle SHADERS_IS_SHADOW_PASS;
    private static boolean optifineDisabled = false;

    static {
        MethodHandle configIsShaders = null;
        MethodHandle shadersEndLeash = null;
        MethodHandle shadersBeginLeash = null;
        MethodHandle programGetName = null;
        MethodHandle shadersActiveProgram = null;
        MethodHandle shadersIsShadowPass = null;
        try {
            Class<?> config = Class.forName("net.optifine.Config");
            Class<?> shaders = Class.forName("net.optifine.shaders.Shaders");
            Class<?> program = Class.forName("net.optifine.shaders.Program");
            configIsShaders = LOOKUP.findStatic(config, "isShaders", MethodType.methodType(boolean.class));
            shadersEndLeash = LOOKUP.findStatic(shaders, "endLeash", MethodType.methodType(void.class));
            shadersBeginLeash = LOOKUP.findStatic(shaders, "beginLeash", MethodType.methodType(void.class));
            programGetName = LOOKUP.findVirtual(program, "getName", MethodType.methodType(String.class));
            shadersActiveProgram = LOOKUP.findStaticGetter(shaders, "activeProgram", program);
            shadersIsShadowPass = LOOKUP.findStaticGetter(shaders, "isShadowPass", boolean.class);
            LOGGER.debug("[WhereIsIt] Optifine integration successfully initialized");
        } catch (final IllegalAccessException | ClassNotFoundException | NoSuchMethodException | NoSuchFieldException ignore) {
            // Optifine not available
            optifineDisabled = true;
        }

        CONFIG_IS_SHADERS = configIsShaders;
        SHADERS_END_LEASH = shadersEndLeash;
        SHADERS_BEGIN_LEASH = shadersBeginLeash;
        PROGRAM_GET_NAME = programGetName;
        SHADERS_ACTIVE_PROGRAM = shadersActiveProgram;
        SHADERS_IS_SHADOW_PASS = shadersIsShadowPass;
    }

    /**
     * If optifine is available, modify shader state to do our rendering. Otherwise, just use our renderer.
     *
     * @param renderer the actual callback to do some rendering
     */
    public static void doOptifineAwareRender(final WorldRenderContext ctx, final BiConsumer<WorldRenderContext, Boolean> renderer) {
        if (SHADERS_END_LEASH == null || SHADERS_BEGIN_LEASH == null || CONFIG_IS_SHADERS == null || optifineDisabled) {
            renderer.accept(ctx, false);
            return;
        }

        try {
            final boolean shadersEnabled = (boolean) CONFIG_IS_SHADERS.invoke();
            if (!shadersEnabled) {
                renderer.accept(ctx, false);
            } else if (!(boolean) SHADERS_IS_SHADOW_PASS.invoke()) {
                SHADERS_BEGIN_LEASH.invoke();
                renderer.accept(ctx, true);
                SHADERS_END_LEASH.invoke();
            }
        } catch (final Throwable err) {
            optifineDisabled = true;
            LOGGER.error("[WhereIsIt] Failed to render WhereIsIt using OptiFine hooks", err);
        }
    }
}
