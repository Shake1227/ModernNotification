package shake1227.modernnotification.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import shake1227.modernnotification.config.ClientConfig;
import shake1227.modernnotification.core.NotificationCategory;
import shake1227.modernnotification.core.NotificationType;
import shake1227.modernnotification.notification.Notification;
import shake1227.modernnotification.util.ColorUtils;
import shake1227.modernnotification.util.MathUtils;

import java.util.List;

public class NotificationRenderer {

    private static final float LEFT_ALPHA = 0.8f;
    private static final float LEFT_CORNER_RADIUS = 5.0f;

    private static final int LEFT_PADDING = 5;
    private static final int RIGHT_PADDING = 5;
    private static final int TOP_PADDING = 4;
    private static final int BOTTOM_PADDING = 4;
    private static final int ICON_SIZE = 8;
    private static final int ICON_MARGIN_RIGHT = 4;
    private static final int LEFT_WIDTH = 130;

    private static final int RIGHT_ICON_AREA_SIZE = 16;
    private static final int RIGHT_WIDTH = 160;

    private static final int ADMIN_TOP_PADDING = 5;
    private static final float ADMIN_Y_POS = 10.0f;
    private static final int ADMIN_WIDTH = 220;
    private static final int ADMIN_PADDING_X = 8;
    private static final int ADMIN_PADDING_Y = 6;
    private static final float ADMIN_TITLE_SCALE = 1.0f;
    private static final float ADMIN_MESSAGE_SCALE = 0.9f;
    private static final int ADMIN_LINE_SPACING = 2;

    public void render(GuiGraphics guiGraphics, List<Notification> leftNotifications, List<Notification> rightNotifications, Notification adminNotification, float partialTicks) {
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        for (Notification notification : leftNotifications) {
            float yPos = MathUtils.lerp(notification.getPreviousY(), notification.getCurrentY(partialTicks), partialTicks);
            renderLeftNotification(guiGraphics, notification, 5, screenHeight / 2.0f - 20 + yPos, partialTicks);
        }

        for (Notification notification : rightNotifications) {
            float yPos = MathUtils.lerp(notification.getPreviousY(), notification.getCurrentY(partialTicks), partialTicks);
            renderRightNotification(guiGraphics, notification, screenWidth - RIGHT_WIDTH - 5, 10 + yPos, partialTicks);
        }

        if (adminNotification != null) {
            renderAdminNotification(guiGraphics, adminNotification, (screenWidth - ADMIN_WIDTH) / 2.0f, ADMIN_Y_POS, partialTicks);
        }
    }

    public int getHeight(Notification notification) {
        Font font = Minecraft.getInstance().font;
        if (notification.getType() == NotificationType.LEFT) {
            int textWidth = LEFT_WIDTH - LEFT_PADDING - RIGHT_PADDING - ICON_SIZE - ICON_MARGIN_RIGHT;
            List<Component> lines = notification.getMessage();
            float scale = 0.9f;
            int lineHeight = (int)(font.lineHeight * scale) + 1;
            return Math.max(ICON_SIZE + TOP_PADDING + BOTTOM_PADDING, (lines.size() * lineHeight) + TOP_PADDING + BOTTOM_PADDING);

        } else if (notification.getType() == NotificationType.TOP_RIGHT) {
            int textHeight = (font.lineHeight + 1) + font.lineHeight;
            return Math.max(RIGHT_ICON_AREA_SIZE + TOP_PADDING + BOTTOM_PADDING, textHeight + TOP_PADDING + BOTTOM_PADDING);

        } else if (notification.getType() == NotificationType.ADMIN) {
            List<Component> titleLines = notification.getTitle();
            List<Component> msgLines = notification.getMessage();

            int titleHeight = 0;
            if (!titleLines.isEmpty()) {
                titleHeight = (int)((font.lineHeight * ADMIN_TITLE_SCALE + ADMIN_LINE_SPACING) * titleLines.size());
            }

            int msgHeight = 0;
            if (!msgLines.isEmpty()) {
                msgHeight = (int)((font.lineHeight * ADMIN_MESSAGE_SCALE + ADMIN_LINE_SPACING) * msgLines.size());
            }

            return ADMIN_PADDING_Y + titleHeight + (titleHeight > 0 && msgHeight > 0 ? ADMIN_LINE_SPACING : 0) + msgHeight + ADMIN_PADDING_Y;
        }
        return 0;
    }

    private int getWidth(Notification notification) {
        if (notification.getType() == NotificationType.LEFT) {
            return LEFT_WIDTH;
        } else if (notification.getType() == NotificationType.TOP_RIGHT) {
            return RIGHT_WIDTH;
        } else if (notification.getType() == NotificationType.ADMIN) {
            return ADMIN_WIDTH;
        }
        return 0;
    }

    private void renderLeftNotification(GuiGraphics guiGraphics, Notification notification, float x, float y, float partialTicks) {
        Font font = Minecraft.getInstance().font;
        int width = getWidth(notification);
        int height = getHeight(notification);

        float smoothProgress = notification.getAnimationProgress(partialTicks);
        float startX = x;
        if (notification.getState() == Notification.NotificationState.ENTERING) {
            startX = (int) (x - (width + 5) * (1.0f - smoothProgress));
        } else if (notification.getState() == Notification.NotificationState.EXITING) {
            startX = (int) (x - (width + 5) * (1.0f - smoothProgress));
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(startX, y, 0);

        int bgColor1 = ColorUtils.parseColor(ClientConfig.INSTANCE.backgroundColorTop.get());
        int bgColor2 = ColorUtils.parseColor(ClientConfig.INSTANCE.backgroundColorBottom.get());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawRoundedRectGradient(guiGraphics, 0, 0, width, height, LEFT_CORNER_RADIUS, bgColor1, bgColor2, LEFT_ALPHA);
        RenderSystem.disableBlend();

        int iconX = LEFT_PADDING;
        int iconY = TOP_PADDING + (height - TOP_PADDING - BOTTOM_PADDING - ICON_SIZE) / 2;
        String iconText = notification.getCategory().getIcon();
        guiGraphics.drawString(font, iconText, iconX + 1, iconY + 1, notification.getCategory().getColor(), true);

        int textX = iconX + ICON_SIZE + ICON_MARGIN_RIGHT;
        List<Component> lines = notification.getMessage();
        float scale = 0.9f;
        int lineHeight = (int)(font.lineHeight * scale) + 1;
        int textY = TOP_PADDING + (height - TOP_PADDING - BOTTOM_PADDING - (lines.size() * lineHeight)) / 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);
        for (Component line : lines) {
            guiGraphics.drawString(font, line, (int)(textX / scale), (int)(textY / scale), 0xFFFFFF, false);
            textY += lineHeight;
        }
        guiGraphics.pose().popPose();

        renderTimerBar(guiGraphics, 0, height, width, 2, notification, partialTicks);

        guiGraphics.pose().popPose();
    }

    private void renderRightNotification(GuiGraphics guiGraphics, Notification notification, float x, float y, float partialTicks) {
        Font font = Minecraft.getInstance().font;
        int width = getWidth(notification);
        int height = getHeight(notification);

        float smoothProgress = notification.getAnimationProgress(partialTicks);
        float startX = x;
        if (notification.getState() == Notification.NotificationState.ENTERING) {
            startX = (int) (x + (width + 5) * (1.0f - smoothProgress));
        } else if (notification.getState() == Notification.NotificationState.EXITING) {
            startX = (int) (x + (width + 5) * (1.0f - smoothProgress));
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(startX, y, 0);

        int bgColor1 = ColorUtils.parseColor(ClientConfig.INSTANCE.backgroundColorTop.get());
        int bgColor2 = ColorUtils.parseColor(ClientConfig.INSTANCE.backgroundColorBottom.get());
        guiGraphics.fillGradient(0, 0, width, height, bgColor1, bgColor2);

        int iconX = LEFT_PADDING;
        int iconY = (height - RIGHT_ICON_AREA_SIZE) / 2;
        String iconText = notification.getCategory().getIcon();
        int iconTextWidth = font.width(iconText);
        guiGraphics.drawString(font, iconText, iconX + (RIGHT_ICON_AREA_SIZE - iconTextWidth) / 2 + 1, iconY + (RIGHT_ICON_AREA_SIZE - font.lineHeight) / 2, notification.getCategory().getColor(), true);

        int textX = iconX + RIGHT_ICON_AREA_SIZE + ICON_MARGIN_RIGHT;
        int textY = TOP_PADDING + (height - TOP_PADDING - BOTTOM_PADDING - (font.lineHeight * 2 + 1)) / 2;

        List<Component> titleLines = notification.getTitle();
        List<Component> msgLines = notification.getMessage();

        if (!titleLines.isEmpty()) {
            guiGraphics.drawString(font, titleLines.get(0), textX, textY, 0xFFFFFF, true);
            textY += font.lineHeight + 1;
        }

        if (!msgLines.isEmpty()) {
            guiGraphics.drawString(font, msgLines.get(0), textX, textY, 0xCCCCCC, false);
        }

        renderTimerBar(guiGraphics, 0, height, width, 2, notification, partialTicks);

        guiGraphics.pose().popPose();
    }

    private void renderAdminNotification(GuiGraphics guiGraphics, Notification notification, float x, float y, float partialTicks) {
        Font font = Minecraft.getInstance().font;
        int width = getWidth(notification);
        int height = getHeight(notification);

        float smoothProgress = notification.getAnimationProgress(partialTicks);
        float startY = y;
        if (notification.getState() == Notification.NotificationState.ENTERING) {
            startY = (int) (y - (height + 5) * (1.0f - smoothProgress));
        } else if (notification.getState() == Notification.NotificationState.EXITING) {
            startY = (int) (y - (height + 5) * (1.0f - smoothProgress));
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, startY, 100);

        RenderSystem.disableBlend();
        int bgColor1 = ColorUtils.parseColor(ClientConfig.INSTANCE.adminGradientStart.get());
        int bgColor2 = ColorUtils.parseColor(ClientConfig.INSTANCE.adminGradientEnd.get());
        guiGraphics.fillGradient(0, 0, width, height, bgColor1, bgColor2);
        RenderSystem.enableBlend();

        int textX = ADMIN_PADDING_X;
        int textY = ADMIN_PADDING_Y;
        int textWidth = width - ADMIN_PADDING_X * 2;

        List<Component> titleLines = notification.getTitle();
        List<Component> msgLines = notification.getMessage();

        if (!titleLines.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(ADMIN_TITLE_SCALE, ADMIN_TITLE_SCALE, 1.0f);
            for (Component line : titleLines) {
                guiGraphics.drawString(font, line, (int)(textX / ADMIN_TITLE_SCALE), (int)(textY / ADMIN_TITLE_SCALE), 0xFFFFFF, true);
                textY += (font.lineHeight * ADMIN_TITLE_SCALE + ADMIN_LINE_SPACING);
            }
            guiGraphics.pose().popPose();
        }

        if (!titleLines.isEmpty() && !msgLines.isEmpty()) {
            textY += ADMIN_LINE_SPACING;
        }

        if (!msgLines.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(ADMIN_MESSAGE_SCALE, ADMIN_MESSAGE_SCALE, 1.0f);
            for (Component line : msgLines) {
                guiGraphics.drawString(font, line, (int)(textX / ADMIN_MESSAGE_SCALE), (int)(textY / ADMIN_MESSAGE_SCALE), 0xFFFFFF, true);
                textY += (font.lineHeight * ADMIN_MESSAGE_SCALE + ADMIN_LINE_SPACING);
            }
            guiGraphics.pose().popPose();
        }

        guiGraphics.pose().popPose();
    }

    private void renderTimerBar(GuiGraphics guiGraphics, int x, int y, int width, int height, Notification notification, float partialTicks) {
        float progress = 1.0f;
        if (notification.getState() == Notification.NotificationState.DISPLAYING) {
            progress = notification.getProgress(partialTicks);
        } else if (notification.getState() == Notification.NotificationState.EXITING) {
            progress = 0.0f;
        }

        if (progress <= 0) return;

        int color1 = notification.getCategory().getGradientStartColor();
        int color2 = notification.getCategory().getGradientEndColor();
        int animatedWidth = (int) (width * progress);

        guiGraphics.fillGradient(x, y, x + animatedWidth, y + height, color1, color2);
    }

    private void drawRoundedRectGradient(GuiGraphics guiGraphics, float x, float y, float width, float height, float radius, int color1, int color2, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = guiGraphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        float r1 = (float)(color1 >> 16 & 255) / 255.0F;
        float g1 = (float)(color1 >> 8 & 255) / 255.0F;
        float b1 = (float)(color1 & 255) / 255.0F;

        float r2 = (float)(color2 >> 16 & 255) / 255.0F;
        float g2 = (float)(color2 >> 8 & 255) / 255.0F;
        float b2 = (float)(color2 & 255) / 255.0F;

        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        bufferbuilder.vertex(matrix, x + radius, y + radius, 0).color(r1, g1, b1, alpha).endVertex();
        int segments = 20;

        drawCircleFan(bufferbuilder, matrix, x + width - radius, y + radius, radius, 270, 360, segments, r1, g1, b1, alpha);
        drawCircleFan(bufferbuilder, matrix, x + width - radius, y + height - radius, radius, 0, 90, segments, r2, g2, b2, alpha);
        drawCircleFan(bufferbuilder, matrix, x + radius, y + height - radius, radius, 90, 180, segments, r2, g2, b2, alpha);
        drawCircleFan(bufferbuilder, matrix, x + radius, y + radius, radius, 180, 270, segments, r1, g1, b1, alpha);

        tesselator.end();
        RenderSystem.disableBlend();
    }

    private void drawCircleFan(BufferBuilder bufferbuilder, Matrix4f matrix, float cx, float cy, float radius, int startAngle, int endAngle, int segments, float r, float g, float b, float a) {
        float angleStep = (float) Math.toRadians((double)(endAngle - startAngle) / segments);
        float startRad = (float) Math.toRadians(startAngle);

        for (int i = 0; i <= segments; i++) {
            float angle = startRad + (angleStep * i);
            float dx = (float) (Math.cos(angle) * radius);
            float dy = (float) (Math.sin(angle) * radius);
            bufferbuilder.vertex(matrix, cx + dx, cy + dy, 0).color(r, g, b, a).endVertex();
        }
    }
}

