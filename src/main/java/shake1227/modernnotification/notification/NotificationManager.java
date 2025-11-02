package shake1227.modernnotification.notification;

import shake1227.modernnotification.client.NotificationRenderer;
import shake1227.modernnotification.core.NotificationType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationManager {

    private static final NotificationManager INSTANCE = new NotificationManager();
    private final List<Notification> notifications = new ArrayList<>();
    private Notification adminNotification = null;
    private final NotificationRenderer renderer;

    private NotificationManager() {
        this.renderer = new NotificationRenderer();
    }

    public static NotificationManager getInstance() {
        return INSTANCE;
    }

    public void addNotification(Notification notification) {
        if (notification.getType() == NotificationType.ADMIN) {
            if (this.adminNotification != null) {
                this.adminNotification.startExiting();
            }
            this.adminNotification = notification;
        } else {
            this.notifications.add(notification);
        }
        recalculateTargetY();
    }

    public void update() {
        boolean needsRecalculate = false;

        notifications.removeIf(notification -> {
            if (notification.isFinished()) {
                return true;
            }
            notification.update();
            return false;
        });

        if (adminNotification != null) {
            if (adminNotification.isFinished()) {
                adminNotification = null;
                needsRecalculate = true;
            } else {
                adminNotification.update();
            }
        }

        if (needsRecalculate) {
            recalculateTargetY();
        }

        if (notifications.stream().anyMatch(n -> n.getCurrentY(1.0f) != n.getTargetY())) {
            recalculateTargetY();
        }
    }

    private void recalculateTargetY() {
        float leftY = 0;
        float rightY = 0;

        for (Notification notification : notifications) {
            int height = renderer.getHeight(notification);
            if (notification.getType() == NotificationType.LEFT) {
                notification.setTargetY(leftY);
                leftY += height + 5;
            } else {
                notification.setTargetY(rightY);
                rightY += height + 5;
            }
        }
    }

    public List<Notification> getLeftNotifications() {
        return notifications.stream()
                .filter(n -> n.getType() == NotificationType.LEFT)
                .collect(Collectors.toList());
    }

    public List<Notification> getRightNotifications() {
        return notifications.stream()
                .filter(n -> n.getType() == NotificationType.TOP_RIGHT)
                .collect(Collectors.toList());
    }

    public Notification getAdminNotification() {
        return adminNotification;
    }

    public NotificationRenderer getRenderer() {
        return renderer;
    }
}

