package shake1227.modernnotification.log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;
import shake1227.modernnotification.ModernNotification;
import shake1227.modernnotification.core.NotificationCategory;
import shake1227.modernnotification.core.NotificationType;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class LogManager {

    private static final LogManager INSTANCE = new LogManager();
    private static final int MAX_LOG_SIZE = 50;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private NotificationLog currentLog;
    private String currentLogFile = "";

    private LogManager() {
        this.currentLog = new NotificationLog();
    }

    public static LogManager getInstance() {
        return INSTANCE;
    }

    // ログを追加する
    public void addLog(NotificationData data) {
        if (data.getType() == NotificationType.LEFT) {
            return;
        }
        checkAndLoadLog();

        if (data.getType() == NotificationType.ADMIN) {
            this.currentLog.getAdminNotifications().addFirst(data);
        } else if (data.getType() == NotificationType.TOP_RIGHT) {
            this.currentLog.getTopRightNotifications().addFirst(data);
        }
        this.currentLog.trimLogs(MAX_LOG_SIZE);
        saveLogToFile();
    }

    // ブックマークを切り替える (単一)
    public void toggleBookmark(long id, NotificationType type) {
        checkAndLoadLog();
        Optional<NotificationData> data = findDataById(id, type);
        data.ifPresent(d -> {
            d.setBookmarked(!d.isBookmarked());
            saveLogToFile();
        });
    }

    // 選択されたログをブックマーク/ブックマーク解除する
    public void bookmarkSelected(Collection<Long> adminIds, Collection<Long> topRightIds, boolean bookmark) {
        checkAndLoadLog();
        this.currentLog.getAdminNotifications().stream()
                .filter(d -> adminIds.contains(d.getId()))
                .forEach(d -> d.setBookmarked(bookmark));
        this.currentLog.getTopRightNotifications().stream()
                .filter(d -> topRightIds.contains(d.getId()))
                .forEach(d -> d.setBookmarked(bookmark));
        saveLogToFile();
    }

    // 修正点2: 選択されたログを削除する (ブックマークされたものは除く)
    public int deleteSelected(Collection<Long> adminIds, Collection<Long> topRightIds) {
        checkAndLoadLog();

        // 削除する前に件数を数える
        long adminRemovedCount = this.currentLog.getAdminNotifications().stream()
                .filter(d -> adminIds.contains(d.getId()) && !d.isBookmarked())
                .count();
        long topRightRemovedCount = this.currentLog.getTopRightNotifications().stream()
                .filter(d -> topRightIds.contains(d.getId()) && !d.isBookmarked())
                .count();

        int totalRemoved = (int)adminRemovedCount + (int)topRightRemovedCount;

        if (totalRemoved > 0) {
            // 実際に削除
            this.currentLog.getAdminNotifications().removeIf(d -> adminIds.contains(d.getId()) && !d.isBookmarked());
            this.currentLog.getTopRightNotifications().removeIf(d -> topRightIds.contains(d.getId()) && !d.isBookmarked());
            saveLogToFile();
        }
        return totalRemoved;
    }

    // 修正点2: フィルターされたログを削除する (ブックマークされたものは除く)
    public int deleteFiltered(List<NotificationData> filteredAdmin, List<NotificationData> filteredTopRight) {
        checkAndLoadLog();

        // 削除対象のIDリストを作成
        List<Long> adminIdsToRemove = filteredAdmin.stream()
                .filter(d -> !d.isBookmarked())
                .map(NotificationData::getId)
                .collect(Collectors.toList());

        List<Long> topRightIdsToRemove = filteredTopRight.stream()
                .filter(d -> !d.isBookmarked())
                .map(NotificationData::getId)
                .collect(Collectors.toList());

        // 件数を数える (removeIf は boolean しか返さないため)
        long adminRemovedCount = this.currentLog.getAdminNotifications().stream()
                .filter(d -> adminIdsToRemove.contains(d.getId()))
                .count();
        long topRightRemovedCount = this.currentLog.getTopRightNotifications().stream()
                .filter(d -> topRightIdsToRemove.contains(d.getId()))
                .count();

        int totalRemoved = (int)adminRemovedCount + (int)topRightRemovedCount;

        if (totalRemoved > 0) {
            // 実際に削除
            this.currentLog.getAdminNotifications().removeIf(d -> adminIdsToRemove.contains(d.getId()));
            this.currentLog.getTopRightNotifications().removeIf(d -> topRightIdsToRemove.contains(d.getId()));
            saveLogToFile();
        }
        return totalRemoved;
    }


    public Optional<NotificationData> findDataById(long id, NotificationType type) {
        LinkedList<NotificationData> list = (type == NotificationType.ADMIN)
                ? currentLog.getAdminNotifications()
                : currentLog.getTopRightNotifications();

        return list.stream().filter(d -> d.getId() == id).findFirst();
    }


    // --- 既存のロード/セーブ/パス管理 (変更なし) ---

    private void checkAndLoadLog() {
        String logId = getLogFileIdentifier();
        if (logId == null) {
            this.currentLog = new NotificationLog();
            this.currentLogFile = "";
            return;
        }

        if (logId.equals(this.currentLogFile) && this.currentLog != null) {
            return;
        }

        this.currentLogFile = logId;
        File logFile = getLogFilePath(logId);

        if (logFile.exists()) {
            try (FileReader reader = new FileReader(logFile)) {
                this.currentLog = gson.fromJson(reader, NotificationLog.class);
                if (this.currentLog == null) {
                    this.currentLog = new NotificationLog();
                }
            } catch (IOException e) {
                ModernNotification.LOGGER.error("Failed to load notification log: {}", logFile.getPath(), e);
                this.currentLog = new NotificationLog();
            }
        } else {
            this.currentLog = new NotificationLog();
        }
    }

    private void saveLogToFile() {
        if (this.currentLogFile.isEmpty()) {
            return;
        }

        File logFile = getLogFilePath(this.currentLogFile);
        logFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(logFile)) {
            gson.toJson(this.currentLog, writer);
        } catch (IOException e) {
            ModernNotification.LOGGER.error("Failed to save notification log: {}", logFile.getPath(), e);
        }
    }

    private File getLogFilePath(String logId) {
        File baseDir = new File(Minecraft.getInstance().gameDirectory, "modernnotification_logs");
        return new File(baseDir, logId + ".json");
    }

    @Nullable
    private String getLogFileIdentifier() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return null;
        }

        if (mc.isSingleplayer()) {
            return "sp_" + Objects.requireNonNull(mc.getSingleplayerServer()).getWorldPath(LevelResource.ROOT).getFileName().toString();
        } else {
            if (mc.getCurrentServer() != null) {
                return "mp_" + mc.getCurrentServer().ip.replaceAll(":", "_");
            }
        }

        return null;
    }

    public NotificationLog getLog() {
        checkAndLoadLog();
        return this.currentLog;
    }
}