package shake1227.modernnotification.core;

import net.minecraft.util.StringRepresentable;
import shake1227.modernnotification.config.ClientConfig;
import shake1227.modernnotification.util.ColorUtils;

public enum NotificationCategory implements StringRepresentable {
    SUCCESS("success", "\u2713"),
    WARNING("warning", "\u26A0"),
    FAILURE("failure", "!"),
    SYSTEM("system", "\u2691");

    private final String name;
    private final String icon;

    NotificationCategory(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String getIcon() {
        return this.icon;
    }

    public int getColor() {
        String hex = switch (this) {
            case SUCCESS -> ClientConfig.INSTANCE.successColor.get();
            case WARNING -> ClientConfig.INSTANCE.warningColor.get();
            case FAILURE -> ClientConfig.INSTANCE.failureColor.get();
            case SYSTEM -> ClientConfig.INSTANCE.systemColor.get();
        };
        return ColorUtils.parseColor(hex);
    }

    public int getGradientStartColor() {
        String hex = switch (this) {
            case SUCCESS -> ClientConfig.INSTANCE.successGradientStart.get();
            case WARNING -> ClientConfig.INSTANCE.warningGradientStart.get();
            case FAILURE -> ClientConfig.INSTANCE.failureGradientStart.get();
            case SYSTEM -> ClientConfig.INSTANCE.systemGradientStart.get();
        };
        return ColorUtils.parseColor(hex);
    }

    public int getGradientEndColor() {
        String hex = switch (this) {
            case SUCCESS -> ClientConfig.INSTANCE.successGradientEnd.get();
            case WARNING -> ClientConfig.INSTANCE.warningGradientEnd.get();
            case FAILURE -> ClientConfig.INSTANCE.failureGradientEnd.get();
            case SYSTEM -> ClientConfig.INSTANCE.systemGradientEnd.get();
        };
        return ColorUtils.parseColor(hex);
    }
}

