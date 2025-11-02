package shake1227.modernnotification.util;

public class MathUtils {

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float easeInOutCubic(float x) {
        return x < 0.5f ? 4.0f * x * x * x : 1.0f - (float) Math.pow(-2.0f * x + 2.0f, 3.0f) / 2.0f;
    }
}

