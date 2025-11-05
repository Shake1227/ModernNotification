package shake1227.modernnotification.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import shake1227.modernnotification.config.ServerConfig;
import shake1227.modernnotification.core.NotificationCategory;
import shake1227.modernnotification.core.NotificationType;
import shake1227.modernnotification.network.PacketHandler;
import shake1227.modernnotification.network.S2CNotificationPacket;
import shake1227.modernnotification.util.TextFormattingUtils;

import java.util.Collection;
import java.util.List;
public class ModernNotificationAPI {
    private static void sendPacketToPlayers(Iterable<ServerPlayer> players, S2CNotificationPacket packet) {
        for (ServerPlayer player : players) {
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
    private static int getDefaultDuration() {
        return ServerConfig.INSTANCE.defaultDuration.get();
    }
    public static void sendLeftNotification(ServerPlayer player, NotificationCategory category, String message) {
        sendLeftNotification(player, category, message, -1);
    }
    public static void sendLeftNotification(ServerPlayer player, NotificationCategory category, String message, int durationSeconds) {
        sendLeftNotification(List.of(player), category, message, durationSeconds);
    }
    public static void sendLeftNotification(Collection<ServerPlayer> players, NotificationCategory category, String message) {
        sendLeftNotification(players, category, message, -1);
    }
    public static void sendLeftNotification(Collection<ServerPlayer> players, NotificationCategory category, String message, int durationSeconds) {
        List<Component> messageComponent = TextFormattingUtils.parseLegacyText(message);
        int duration = (durationSeconds > 0) ? durationSeconds : getDefaultDuration();

        S2CNotificationPacket packet = new S2CNotificationPacket(
                NotificationType.LEFT,
                category,
                null,
                messageComponent,
                duration
        );
        sendPacketToPlayers(players, packet);
    }
    public static void sendTopRightNotification(ServerPlayer player, NotificationCategory category, String title, String message) {
        sendTopRightNotification(player, category, title, message, -1);
    }
    public static void sendTopRightNotification(ServerPlayer player, NotificationCategory category, String title, String message, int durationSeconds) {
        sendTopRightNotification(List.of(player), category, title, message, durationSeconds);
    }
    public static void sendTopRightNotification(Collection<ServerPlayer> players, NotificationCategory category, String title, String message) {
        sendTopRightNotification(players, category, title, message, -1);
    }
    public static void sendTopRightNotification(Collection<ServerPlayer> players, NotificationCategory category, String title, String message, int durationSeconds) {
        List<Component> titleComponent = TextFormattingUtils.parseLegacyText(title);
        List<Component> messageComponent = TextFormattingUtils.parseLegacyText(message);
        int duration = (durationSeconds > 0) ? durationSeconds : getDefaultDuration();

        S2CNotificationPacket packet = new S2CNotificationPacket(
                NotificationType.TOP_RIGHT,
                category,
                titleComponent,
                messageComponent,
                duration
        );
        sendPacketToPlayers(players, packet);
    }
    public static void sendAdminNotification(ServerPlayer player, String title, String message) {
        sendAdminNotification(player, title, message, -1);
    }
    public static void sendAdminNotification(ServerPlayer player, String title, String message, int durationSeconds) {
        sendAdminNotification(List.of(player), title, message, durationSeconds);
    }
    public static void sendAdminNotification(Collection<ServerPlayer> players, String title, String message) {
        sendAdminNotification(players, title, message, -1);
    }

    public static void sendAdminNotification(Collection<ServerPlayer> players, String title, String message, int durationSeconds) {
        List<Component> titleComponent = TextFormattingUtils.parseLegacyText(title);
        List<Component> messageComponent = TextFormattingUtils.parseLegacyText(message);
        int duration = (durationSeconds > 0) ? durationSeconds : getDefaultDuration();

        S2CNotificationPacket packet = new S2CNotificationPacket(
                NotificationType.ADMIN,
                NotificationCategory.SYSTEM,
                titleComponent,
                messageComponent,
                duration
        );
        sendPacketToPlayers(players, packet);
    }
}