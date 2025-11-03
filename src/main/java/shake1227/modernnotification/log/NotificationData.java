package shake1227.modernnotification.log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.Component;
import shake1227.modernnotification.core.NotificationCategory;
import shake1227.modernnotification.core.NotificationType;
import shake1227.modernnotification.notification.Notification;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationData {

    private final NotificationType type;
    private final NotificationCategory category;
    private final List<String> titleJson;
    private final List<String> messageJson;
    private final long timestamp;
    private boolean bookmarked;
    private long id;
    private int dynamicWidth = 0;

    private NotificationData(NotificationType type, NotificationCategory category, List<String> titleJson, List<String> messageJson, long timestamp, boolean bookmarked) {
        this.type = type;
        this.category = category;
        this.titleJson = titleJson;
        this.messageJson = messageJson;
        this.timestamp = timestamp;
        this.bookmarked = bookmarked;
        this.id = timestamp;
    }

    public NotificationData(Notification notification) {
        this.type = notification.getType();
        this.category = notification.getCategory();
        this.titleJson = notification.getTitle().stream()
                .map(Component.Serializer::toJson)
                .collect(Collectors.toList());
        this.messageJson = notification.getMessage().stream()
                .map(Component.Serializer::toJson)
                .collect(Collectors.toList());
        this.timestamp = System.currentTimeMillis();
        this.bookmarked = false;
        this.id = this.timestamp;
        this.dynamicWidth = notification.getDynamicWidth();
    }

    public List<Component> getTitle() {
        return this.titleJson.stream()
                .map(Component.Serializer::fromJson)
                .collect(Collectors.toList());
    }
    public List<Component> getMessage() {
        return this.messageJson.stream()
                .map(Component.Serializer::fromJson)
                .collect(Collectors.toList());
    }
    public NotificationType getType() {
        return type;
    }
    public NotificationCategory getCategory() {
        return category;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public long getId() {
        return id;
    }
    public boolean isBookmarked() {
        return bookmarked;
    }
    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }
    public String getSearchableText() {
        StringBuilder sb = new StringBuilder();
        getTitle().forEach(c -> sb.append(c.getString()).append(" "));
        getMessage().forEach(c -> sb.append(c.getString()).append(" "));
        return sb.toString().toLowerCase();
    }
    public int getDynamicWidth() {
        return dynamicWidth;
    }
}