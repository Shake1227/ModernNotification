package shake1227.modernnotification.core;

import net.minecraft.util.StringRepresentable;
import shake1227.modernnotification.config.ServerConfig;
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
            case SUCCESS -> ServerConfig.INSTANCE.successColor.get();
            case WARNING -> ServerConfig.INSTANCE.warningColor.get();
            case FAILURE -> ServerConfig.INSTANCE.failureColor.get();
            case SYSTEM -> ServerConfig.INSTANCE.systemColor.get();
        };
        return ColorUtils.parseColor(hex);
    }

    public int getGradientStartColor() {
        String hex = switch (this) {
            case SUCCESS -> ServerConfig.INSTANCE.successGradientStart.get();
            case WARNING -> ServerConfig.INSTANCE.warningGradientStart.get();
            case FAILURE -> ServerConfig.INSTANCE.failureGradientStart.get();
            case SYSTEM -> ServerConfig.INSTANCE.systemGradientStart.get();
        };
        return ColorUtils.parseColor(hex);
    }

    public int getGradientEndColor() {
        String hex = switch (this) {
            case SUCCESS -> ServerConfig.INSTANCE.successGradientEnd.get();
            case WARNING -> ServerConfig.INSTANCE.warningGradientEnd.get();
            case FAILURE -> ServerConfig.INSTANCE.failureGradientEnd.get();
            case SYSTEM -> ServerConfig.INSTANCE.systemGradientEnd.get();
        };
        return ColorUtils.parseColor(hex);
    }
}