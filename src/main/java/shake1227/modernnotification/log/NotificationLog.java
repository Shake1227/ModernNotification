package shake1227.modernnotification.log;

import java.util.LinkedList;

public class NotificationLog {

    private LinkedList<NotificationData> adminNotifications;
    private LinkedList<NotificationData> topRightNotifications;

    public NotificationLog() {
        this.adminNotifications = new LinkedList<>();
        this.topRightNotifications = new LinkedList<>();
    }

    public LinkedList<NotificationData> getAdminNotifications() {
        return adminNotifications;
    }

    public LinkedList<NotificationData> getTopRightNotifications() {
        return topRightNotifications;
    }

    public void trimLogs(int maxSize) {
        trimList(adminNotifications, maxSize);
        trimList(topRightNotifications, maxSize);
    }

    private void trimList(LinkedList<NotificationData> list, int maxSize) {
        while (list.size() > maxSize) {
            boolean removed = false;
            for (int i = list.size() - 1; i >= 0; i--) {
                if (!list.get(i).isBookmarked()) {
                    list.remove(i);
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                break;
            }
        }
    }
}