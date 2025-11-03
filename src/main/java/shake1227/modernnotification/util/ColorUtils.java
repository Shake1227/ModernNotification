package shake1227.modernnotification.util;

import net.minecraft.util.FastColor;

public class ColorUtils {

    public static int parseColor(String hex) {
        try {
            long longVal = Long.parseLong(hex, 16);
            if (hex.length() == 8) {
                int r = (int) ((longVal >> 24) & 0xFF);
                int g = (int) ((longVal >> 16) & 0xFF);
                int b = (int) ((longVal >> 8) & 0xFF);
                int a = (int) (longVal & 0xFF);
                return (a << 24) | (r << 16) | (g << 8) | b;
            } else if (hex.length() == 6) {
                return 0xFF000000 | (int) longVal;
            }
        } catch (NumberFormatException e) {
            return 0xFFFFFFFF;
        }
        return 0xFFFFFFFF;
    }

    public static int applyAlpha(int color, float alpha) {
        int originalAlpha = (color >> 24) & 0xFF;
        int newAlpha = (int) (originalAlpha * alpha);
        return (color & 0x00FFFFFF) | (newAlpha << 24);
    }

    public static int lerpColor(int color1, int color2, float delta) {
        int a1 = FastColor.ARGB32.alpha(color1);
        int r1 = FastColor.ARGB32.red(color1);
        int g1 = FastColor.ARGB32.green(color1);
        int b1 = FastColor.ARGB32.blue(color1);

        int a2 = FastColor.ARGB32.alpha(color2);
        int r2 = FastColor.ARGB32.red(color2);
        int g2 = FastColor.ARGB32.green(color2);
        int b2 = FastColor.ARGB32.blue(color2);

        int a = (int) MathUtils.lerp(a1, a2, delta);
        int r = (int) MathUtils.lerp(r1, r2, delta);
        int g = (int) MathUtils.lerp(g1, g2, delta);
        int b = (int) MathUtils.lerp(b1, b2, delta);

        return FastColor.ARGB32.color(a, r, g, b);
    }
}

