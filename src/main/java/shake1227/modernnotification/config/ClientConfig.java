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
    public final ForgeConfigSpec.DoubleValue notificationVolume;

    ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Audio");
        notificationVolume = builder
                .comment("Volume of the notification sound (0.0 = Mute, 1.0 = Max)")
                .defineInRange("notificationVolume", 1.0, 0.0, 1.0);

        builder.pop();
    }
}