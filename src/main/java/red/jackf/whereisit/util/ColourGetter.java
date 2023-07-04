package red.jackf.whereisit.util;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.Objects;
import java.util.TreeMap;

public interface ColourGetter {
    int eval(Float point);

    class Gradient implements ColourGetter {
        private final TreeMap<Float, Integer> points;

        private Gradient(TreeMap<Float, Integer> points) {
            this.points = points;
        }

        private static int lerpColours(float factor, int colourA, int colourB) {
            return FastColor.ARGB32.color(255,
                Mth.lerpInt(factor, FastColor.ARGB32.red(colourA), FastColor.ARGB32.red(colourB)),
                Mth.lerpInt(factor, FastColor.ARGB32.green(colourA), FastColor.ARGB32.green(colourB)),
                Mth.lerpInt(factor, FastColor.ARGB32.blue(colourA), FastColor.ARGB32.blue(colourB))
            );
        }

        public int eval(Float point) {
            point = point % 1;
            var lower = points.floorEntry(point);
            if (Objects.equals(lower.getKey(), point)) return lower.getValue();
            var greater = points.ceilingEntry(point);
            var progress = (point - lower.getKey()) / (greater.getKey() - lower.getKey());

            return lerpColours(progress, lower.getValue(), greater.getValue());
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final TreeMap<Float, Integer> points = new TreeMap<>();
            private Builder() {}

            private static void checkInBounds(float point) {
                if (point < 0f || point > 1f) throw new IllegalArgumentException("Tried to add point outside of gradient!");
            }


            /**
             * Adds a keyframe to a gradient.
             * @param point Position in the gradient to add the keyframe
             * @param colour ARGB colour
             */
            public Builder add(float point, int colour) {
                checkInBounds(point);
                points.put(point, colour);
                return this;
            }

            /**
             * Adds a keyframe to a gradient.
             * @param point Position in the gradient to add the keyframe
             * @param r R colour component
             * @param g G colour component
             * @param b B colour component
             */
            public Builder addRgb(float point, int r, int g, int b) {
                return add(point, FastColor.ARGB32.color(255, r, g, b));
            }

            /**
             * Adds a solid block of a colour using two keyframes <code>width/2</code> distance from the <code>centerPoint</code>
             * @param centerPoint Position in the gradient to add the keyframe block
             * @param width Width of the keyframe block
             * @param r R colour component
             * @param g G colour component
             * @param b B colour component
             */
            public Builder addRgbBlock(float centerPoint, float width, int r, int g, int b) {
                checkInBounds(centerPoint + width/2);
                checkInBounds(centerPoint - width/2);
                points.put(centerPoint - width/2, FastColor.ARGB32.color(255, r, g, b));
                points.put(centerPoint + width/2, FastColor.ARGB32.color(255, r, g, b));
                return this;
            }

            /**
             * Build the gradient, extending the first and last colours to the edge of the gradient if not set
             */
            public Gradient build() {
                if (points.isEmpty()) {
                    throw new IllegalArgumentException("Tried to build a gradient with no colours!");
                }
                if (!points.containsKey(0f)) points.put(0f, points.firstEntry().getValue());
                if (!points.containsKey(1f)) points.put(1f, points.lastEntry().getValue());

                return new Gradient(points);
            }

            /**
             * Builds the gradient, slightly compressing it and adding a smooth colour transition on the edge for loops
             * @param width transition width of the gradient
             */
            public Gradient buildWithSmoothTransition(float width) {
                if (points.isEmpty()) {
                    throw new IllegalArgumentException("Tried to build a gradient with no colours!");
                }
                if (!points.containsKey(0f)) points.put(0f, points.firstEntry().getValue());
                if (!points.containsKey(1f)) points.put(1f, points.lastEntry().getValue());

                var newMap = new TreeMap<Float, Integer>();
                var compressionFactor = 1 - width;

                // compress the points domain from [0, 1] to [width/2, 1 - width/2]
                for (var entry : points.entrySet())
                    newMap.put(compressionFactor * (entry.getKey() - 0.5f) + 0.5f, entry.getValue());

                var midpointColour = lerpColours(0.5f, points.lastEntry().getValue(), points.firstEntry().getValue());
                newMap.put(0f, midpointColour);
                newMap.put(1f, midpointColour);

                return new Gradient(newMap);
            }
        }

        private static float[] rgbToHsv(int colour) {
            float r = FastColor.ARGB32.red(colour) / 255f;
            float g = FastColor.ARGB32.green(colour) / 255f;
            float b = FastColor.ARGB32.blue(colour) / 255f;

            float max = Math.max(r, Math.max(g, b)); // value
            float min = Math.min(r, Math.min(g, b));
            float chroma = max - min;
            float hue = 0f;
            if (chroma != 0) {
                if (max == r) hue = (g - b) / chroma;
                else if (max == g) hue = 2f + (b - r) / chroma;
                else if (max == b) hue = 4f + (r - g) / chroma;
                hue /= 6;
                if (hue < 0) hue += 1;
            }
            var saturation = max == 0f ? 0f : chroma / max;
            return new float[]{
                    hue,
                    saturation,
                    max
            };
        }
    }
}
