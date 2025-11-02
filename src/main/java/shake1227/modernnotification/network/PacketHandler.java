package shake1227.modernnotification.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import shake1227.modernnotification.ModernNotification;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ModernNotification.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;
    private static int nextID() {
        return id++;
    }

    public static void register() {
        INSTANCE.registerMessage(
                nextID(),
                S2CNotificationPacket.class,
                S2CNotificationPacket::toBytes,
                S2CNotificationPacket::new,
                S2CNotificationPacket::handle
        );
    }
}

