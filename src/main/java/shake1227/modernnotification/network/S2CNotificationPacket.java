package shake1227.modernnotification.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import shake1227.modernnotification.ModernNotification;
import shake1227.modernnotification.config.ClientConfig;
import shake1227.modernnotification.core.NotificationCategory;
import shake1227.modernnotification.core.NotificationType;
import shake1227.modernnotification.core.ModSounds;
import shake1227.modernnotification.log.LogManager;
import shake1227.modernnotification.log.NotificationData;
import shake1227.modernnotification.notification.Notification;
import shake1227.modernnotification.notification.NotificationManager;

import java.util.List;
import java.util.function.Supplier;

public class S2CNotificationPacket {

    private final NotificationType type;
    private final NotificationCategory category;
    private final List<Component> title;
    private final List<Component> message;
    private final int durationSeconds;

    public S2CNotificationPacket(NotificationType type, NotificationCategory category, List<Component> title, List<Component> message, int durationSeconds) {
        this.type = type;
        this.category = category;
        this.title = title;
        this.message = message;
        this.durationSeconds = durationSeconds;
    }

    public static void toBytes(S2CNotificationPacket packet, FriendlyByteBuf buf) {
        ModernNotification.LOGGER.debug("S2C: Encoding packet...");
        buf.writeEnum(packet.type);
        buf.writeEnum(packet.category);
        buf.writeInt(packet.durationSeconds);

        buf.writeBoolean(packet.title != null);
        if (packet.title != null) {
            buf.writeCollection(packet.title, FriendlyByteBuf::writeComponent);
        }

        buf.writeCollection(packet.message, FriendlyByteBuf::writeComponent);
        ModernNotification.LOGGER.debug("S2C: Packet encoded.");
    }

    public S2CNotificationPacket(FriendlyByteBuf buf) {
        ModernNotification.LOGGER.debug("S2C: Decoding packet...");
        this.type = buf.readEnum(NotificationType.class);
        this.category = buf.readEnum(NotificationCategory.class);
        this.durationSeconds = buf.readInt();

        if (buf.readBoolean()) {
            this.title = buf.readList(FriendlyByteBuf::readComponent);
        } else {
            this.title = null;
        }

        this.message = buf.readList(FriendlyByteBuf::readComponent);
        ModernNotification.LOGGER.debug("S2C: Packet decoded. Type: {}", this.type);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ModernNotification.LOGGER.info("S2C: Packet handled on Client. Adding notification.");

                Notification notification = new Notification(this.type, this.category, this.title, this.message, this.durationSeconds);
                NotificationManager.getInstance().getRenderer().calculateDynamicWidth(notification);

                if (this.type != NotificationType.LEFT) {
                    NotificationData logData = new NotificationData(notification);
                    LogManager.getInstance().addLog(logData);
                }

                NotificationManager.getInstance().addNotification(notification);

                if (this.type == NotificationType.ADMIN) {
                    float volume = ClientConfig.INSTANCE.notificationVolume.get().floatValue();
                    Minecraft.getInstance().player.playNotifySound(ModSounds.NOTIFICATION_SOUND.get(), SoundSource.MASTER, volume, 1.0f);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}