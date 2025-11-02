package shake1227.modernnotification.log;

import java.util.LinkedList;

// 保存するログデータの全体構造
public class NotificationLog {

    // LinkedList を使い、先頭/末尾の操作を高速化
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

    // 古いログを（ブックマークされていなければ）削除する
    public void trimLogs(int maxSize) {
        trimList(adminNotifications, maxSize);
        trimList(topRightNotifications, maxSize);
    }

    private void trimList(LinkedList<NotificationData> list, int maxSize) {
        while (list.size() > maxSize) {
            boolean removed = false;
            // 末尾（一番古い）からブックマークされていないものを探して削除
            for (int i = list.size() - 1; i >= 0; i--) {
                if (!list.get(i).isBookmarked()) {
                    list.remove(i);
                    removed = true;
                    break;
                }
            }
            // すべてブックマークされていて消せない場合、ループを抜ける
            if (!removed) {
                break;
            }
        }
    }
}