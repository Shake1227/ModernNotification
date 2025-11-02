package shake1227.modernnotification.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ClientConfig {

    public static final ClientConfig INSTANCE;
    public static final ForgeConfigSpec SPEC;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
                .configure(ClientConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }

    public final ForgeConfigSpec.IntValue defaultDuration;

    public final ForgeConfigSpec.ConfigValue<String> backgroundColorTop;
    public final ForgeConfigSpec.ConfigValue<String> backgroundColorBottom;

    public final ForgeConfigSpec.ConfigValue<String> successColor;
    public final ForgeConfigSpec.ConfigValue<String> warningColor;
    public final ForgeConfigSpec.ConfigValue<String> failureColor;
    public final ForgeConfigSpec.ConfigValue<String> systemColor;

    public final ForgeConfigSpec.ConfigValue<String> successGradientStart;
    public final ForgeConfigSpec.ConfigValue<String> successGradientEnd;
    public final ForgeConfigSpec.ConfigValue<String> warningGradientStart;
    public final ForgeConfigSpec.ConfigValue<String> warningGradientEnd;
    public final ForgeConfigSpec.ConfigValue<String> failureGradientStart;
    public final ForgeConfigSpec.ConfigValue<String> failureGradientEnd;
    public final ForgeConfigSpec.ConfigValue<String> systemGradientStart;
    public final ForgeConfigSpec.ConfigValue<String> systemGradientEnd;

    public final ForgeConfigSpec.ConfigValue<String> adminGradientStart;
    public final ForgeConfigSpec.ConfigValue<String> adminGradientEnd;

    ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push("General");
        defaultDuration = builder
                .comment("Default duration for notifications in seconds")
                .defineInRange("defaultDuration", 5, 1, 300);

        backgroundColorTop = builder
                .comment("Background color (Top) in RRGGBBAA hex format")
                .define("backgroundColorTop", "000000CC");

        backgroundColorBottom = builder
                .comment("Background color (Bottom) in RRGGBBAA hex format")
                .define("backgroundColorBottom", "222222CC");
        builder.pop();

        builder.push("Category Colors");
        successColor = builder.define("successColor", "00FF00FF");
        warningColor = builder.define("warningColor", "FFFF00FF");
        failureColor = builder.define("failureColor", "FF0000FF");
        systemColor = builder.define("systemColor", "00FFFFFF");
        builder.pop();

        builder.push("Gradient Colors (Timer Bar)");
        successGradientStart = builder.define("successGradientStart", "00FF00FF");
        successGradientEnd = builder.define("successGradientEnd", "00AA00FF");
        warningGradientStart = builder.define("warningGradientStart", "FFFF00FF");
        warningGradientEnd = builder.define("warningGradientEnd", "AAAA00FF");
        failureGradientStart = builder.define("failureGradientStart", "FF0000FF");
        failureGradientEnd = builder.define("failureGradientEnd", "AA0000FF");
        systemGradientStart = builder.define("systemGradientStart", "00FFFFFF");
        systemGradientEnd = builder.define("systemGradientEnd", "00AAAAFF");
        builder.pop();

        builder.push("Admin Notification Colors");
        adminGradientStart = builder
                .comment("Admin notification gradient start color (RRGGBBAA)")
                .define("adminGradientStart", "FFA500FF");
        adminGradientEnd = builder
                .comment("Admin notification gradient end color (RRGGBBAA)")
                .define("adminGradientEnd", "FFD700FF");
        builder.pop();
    }
}

