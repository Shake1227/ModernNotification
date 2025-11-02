package shake1227.modernnotification.notification;

import net.minecraft.network.chat.Component;
import shake1227.modernnotification.config.ClientConfig;
import shake1227.modernnotification.core.NotificationCategory;
import shake1227.modernnotification.core.NotificationType;
import shake1227.modernnotification.util.MathUtils;

import java.util.Collections;
import java.util.List;

public class Notification {

    public enum NotificationState {
        ENTERING,
        DISPLAYING,
        EXITING
    }

    private final NotificationType type;
    private final NotificationCategory category;
    private final List<Component> title;
    private final List<Component> message;
    private final int totalTicks;

    private int ticksExisted;
    private NotificationState state;

    private float currentY;
    private float targetY;
    private float previousY;

    private float animationProgress;
    private float previousAnimationProgress;

    public Notification(NotificationType type, NotificationCategory category, List<Component> title, List<Component> message, int durationSeconds) {
        this.type = type;
        this.category = category;
        this.title = title != null ? title : Collections.emptyList();
        this.message = message;
        this.totalTicks = durationSeconds > 0 ? durationSeconds * 20 : ClientConfig.INSTANCE.defaultDuration.get() * 20;

        this.ticksExisted = 0;
        this.state = NotificationState.ENTERING;

        this.currentY = 0;
        this.targetY = 0;
        this.previousY = 0;

        this.animationProgress = 0;
        this.previousAnimationProgress = 0;
    }

    public void update() {
        this.ticksExisted++;
        this.previousY = this.currentY;
        this.currentY = MathUtils.lerp(this.currentY, this.targetY, 0.2f);

        this.previousAnimationProgress = this.animationProgress;

        int animationDuration = 10;
        // 修正点: アニメーションの進行度（t）を計算
        float t = (float) this.ticksExisted / animationDuration;

        switch (this.state) {
            case ENTERING:
                if (this.ticksExisted >= animationDuration) {
                    this.state = NotificationState.DISPLAYING;
                    this.animationProgress = 1.0f;
                    this.ticksExisted = 0;
                } else {
                    // 修正点: t をイージング関数に渡す
                    this.animationProgress = t; // イージングはRenderer側で行うか、ここで行う
                    // Renderer側で対応するため、ここでは線形のままにする
                    // ※NotificationRenderer側で easeInOutCubic を適用するよう修正しました
                }
                break;
            case DISPLAYING:
                if (this.ticksExisted >= this.totalTicks) {
                    this.state = NotificationState.EXITING;
                    this.animationProgress = 1.0f;
                    this.ticksExisted = 0;
                } else {
                    this.animationProgress = 1.0f;
                }
                break;
            case EXITING:
                if (this.ticksExisted >= animationDuration) {
                    this.animationProgress = 0.0f;
                } else {
                    // 修正点: t をイージング関数に渡す
                    this.animationProgress = 1.0f - t; // Renderer側で対応
                }
                break;
        }
    }

    public boolean isFinished() {
        return this.state == NotificationState.EXITING && this.animationProgress == 0.0f;
    }

    public void startExiting() {
        if (this.state != NotificationState.EXITING) {
            this.state = NotificationState.EXITING;
            this.ticksExisted = 0;
            this.animationProgress = 1.0f;
            this.previousAnimationProgress = 1.0f;
        }
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public List<Component> getTitle() {
        return title;
    }

    public List<Component> getMessage() {
        return message;
    }

    public NotificationState getState() {
        return state;
    }

    public float getProgress() {
        if (this.state == NotificationState.DISPLAYING) {
            return 1.0f - (float) this.ticksExisted / this.totalTicks;
        } else if (this.state == NotificationState.ENTERING) {
            return 1.0f;
        }
        return 0.0f;
    }

    public float getProgress(float partialTicks) {
        float currentProgress = getProgress();
        float previousProgress = 1.0f;
        if (this.state == NotificationState.DISPLAYING && this.ticksExisted > 0) {
            previousProgress = 1.0f - (float) (this.ticksExisted - 1) / this.totalTicks;
        }
        return MathUtils.lerp(previousProgress, currentProgress, partialTicks);
    }


    public float getAnimationProgress(float partialTicks) {
        return MathUtils.lerp(this.previousAnimationProgress, this.animationProgress, partialTicks);
    }

    public float getCurrentY(float partialTicks) {
        return MathUtils.lerp(this.previousY, this.currentY, partialTicks);
    }

    public void setTargetY(float targetY) {
        this.targetY = targetY;
    }

    // 修正点: 前回のビルドエラー解消
    public float getPreviousY() {
        return this.previousY;
    }

    // 修正点: 前回のビルドエラー解消
    public float getTargetY() {
        return this.targetY;
    }
}