package shake1227.modernnotification.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.sounds.SoundSource;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import shake1227.modernnotification.ModernNotification;
import shake1227.modernnotification.client.NotificationRenderer;
import shake1227.modernnotification.config.ClientConfig;
import shake1227.modernnotification.config.ServerConfig;
import shake1227.modernnotification.core.NotificationCategory;
import shake1227.modernnotification.core.NotificationType;
import shake1227.modernnotification.core.ModSounds;
import shake1227.modernnotification.log.LogManager;
import shake1227.modernnotification.log.NotificationData;
import shake1227.modernnotification.log.NotificationLog;
import shake1227.modernnotification.notification.Notification;
import shake1227.modernnotification.notification.NotificationManager;
import shake1227.modernnotification.util.ColorUtils;
import shake1227.modernnotification.util.MathUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NotificationLogScreen extends Screen {

    private EditBox searchBox;
    private double scrollAmountAdmin = 0;
    private double scrollAmountTopRight = 0;
    private FilterType filterType = FilterType.ALL;
    private NotificationCategory filterCategory = null;
    private boolean filterBookmarked = false;
    private String filterText = "";
    private List<NotificationData> filteredAdminLogs = new ArrayList<>();
    private List<NotificationData> filteredTopRightLogs = new ArrayList<>();
    private boolean selectionMode = false;
    private final Set<Long> selectedAdminIds = new HashSet<>();
    private final Set<Long> selectedTopRightIds = new HashSet<>();
    private Button selectButton;
    private Button deleteFilteredButton;
    private Button bookmarkSelectedButton;
    private Button unbookmarkSelectedButton;
    private Button deleteSelectedButton;
    private Screen contextMenu = null;
    private static final int RIGHT_WIDTH = 160;
    private static final int ADMIN_WIDTH = 180;
    private static final int ENTRY_PADDING = 5;
    private int contentY;
    private static final int Y_PADDING = 22;

    public NotificationLogScreen() {
        super(Component.translatable("gui.modernnotification.log.title"));
    }

    @Override
    protected void init() {
        super.init();
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();

        int y = 30;

        double currentValue = ClientConfig.INSTANCE.notificationVolume.get();
        int sliderWidth = 150;
        int sliderX = 10;

        AbstractSliderButton volumeSlider = new AbstractSliderButton(sliderX, y, sliderWidth, 20, Component.empty(), currentValue) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable("gui.modernnotification.log.volume").append(Component.literal(": " + (int)(this.value * 100) + "%")));
            }

            @Override
            protected void applyValue() {
                ClientConfig.INSTANCE.notificationVolume.set(this.value);
            }

            @Override
            public void onRelease(double mouseX, double mouseY) {
                ClientConfig.SPEC.save();
                if (minecraft != null && minecraft.player != null) {
                    float volume = ClientConfig.INSTANCE.notificationVolume.get().floatValue();
                    minecraft.player.playNotifySound(ModSounds.NOTIFICATION_SOUND.get(), SoundSource.MASTER, volume, 1.0f);
                }
            }
        };
        this.addRenderableWidget(volumeSlider);

        int searchBoxWidth = 200;
        int searchBoxX = Math.max(sliderX + sliderWidth + 10, (this.width / 2) - (searchBoxWidth / 2));
        this.searchBox = new EditBox(this.font, searchBoxX, y, searchBoxWidth, 18, Component.translatable("gui.modernnotification.log.search_hint"));
        this.searchBox.setHint(Component.translatable("gui.modernnotification.log.search_hint"));
        this.searchBox.setValue(this.filterText);
        this.searchBox.setResponder(this::onSearchTextUpdate);
        this.addRenderableWidget(this.searchBox);

        y += Y_PADDING;

        if (this.selectionMode) {
            int buttonWidth = 100;
            int x1 = (this.width / 2) - buttonWidth - 5;
            int x2 = (this.width / 2) + 5;

            this.selectButton = Button.builder(Component.translatable("gui.modernnotification.log.cancel"), this::toggleSelectionMode)
                    .pos((this.width / 2) - (buttonWidth/2), y).size(buttonWidth, 20).build();
            this.addRenderableWidget(this.selectButton);

            y += Y_PADDING; // Y=74

            int selectedCount = this.selectedAdminIds.size() + this.selectedTopRightIds.size();

            this.bookmarkSelectedButton = Button.builder(Component.translatable("gui.modernnotification.log.bookmark_selected"), this::onBookmarkSelected)
                    .pos(x1, y).size(buttonWidth, 20).build();
            this.bookmarkSelectedButton.active = selectedCount > 0;
            this.addRenderableWidget(this.bookmarkSelectedButton);

            this.deleteSelectedButton = Button.builder(Component.translatable("gui.modernnotification.log.delete_selected"), this::onDeleteSelected)
                    .pos(x2, y).size(buttonWidth, 20).build();
            this.deleteSelectedButton.active = selectedCount > 0;
            this.addRenderableWidget(this.deleteSelectedButton);

            y += Y_PADDING;

            this.unbookmarkSelectedButton = Button.builder(Component.translatable("gui.modernnotification.log.unbookmark_selected"), this::onUnbookmarkSelected)
                    .pos((this.width / 2) - (buttonWidth/2), y).size(buttonWidth, 20).build();
            this.unbookmarkSelectedButton.active = selectedCount > 0;
            this.addRenderableWidget(this.unbookmarkSelectedButton);

            y += Y_PADDING;

        }
        else {
            this.selectButton = Button.builder(Component.translatable("gui.modernnotification.log.select"), this::toggleSelectionMode)
                    .pos((this.width / 2) - 105, y).size(100, 20).build();
            this.addRenderableWidget(this.selectButton);

            this.deleteFilteredButton = Button.builder(Component.translatable("gui.modernnotification.log.delete_filtered"), this::onDeleteFiltered)
                    .pos((this.width / 2) + 5, y).size(100, 20).build();
            this.addRenderableWidget(this.deleteFilteredButton);

            y += Y_PADDING;

            int filterButtonWidth = 60;
            int totalFilterWidth = (filterButtonWidth * 3) + (5 * 2);
            int filterX = (this.width - totalFilterWidth) / 2;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.modernnotification.log.filter_all"), b -> setFilterType(FilterType.ALL))
                    .pos(filterX, y).size(filterButtonWidth, 20).build());
            filterX += filterButtonWidth + 5;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.modernnotification.log.filter_admin"), b -> setFilterType(FilterType.ADMIN))
                    .pos(filterX, y).size(filterButtonWidth, 20).build());
            filterX += filterButtonWidth + 5;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.modernnotification.log.filter_top_right"), b -> setFilterType(FilterType.TOP_RIGHT))
                    .pos(filterX, y).size(filterButtonWidth, 20).build());

            y += Y_PADDING;

            int catButtonWidth = 60;
            totalFilterWidth = (catButtonWidth * 5) + (5 * 4);
            filterX = (this.width - totalFilterWidth) / 2;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.modernnotification.log.filter_cat_all"), b -> setFilterCategory(null))
                    .pos(filterX, y).size(catButtonWidth, 20).build());
            filterX += catButtonWidth + 5;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.modernnotification.log.filter_sys"), b -> setFilterCategory(NotificationCategory.SYSTEM))
                    .pos(filterX, y).size(catButtonWidth, 20).build());
            filterX += catButtonWidth + 5;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.modernnotification.log.filter_suc"), b -> setFilterCategory(NotificationCategory.SUCCESS))
                    .pos(filterX, y).size(catButtonWidth, 20).build());
            filterX += catButtonWidth + 5;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.modernnotification.log.filter_warn"), b -> setFilterCategory(NotificationCategory.WARNING))
                    .pos(filterX, y).size(catButtonWidth, 20).build());
            filterX += catButtonWidth + 5;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.modernnotification.log.filter_fail"), b -> setFilterCategory(NotificationCategory.FAILURE))
                    .pos(filterX, y).size(catButtonWidth, 20).build());

            y += Y_PADDING;

            int bookmarkButtonWidth = 125;
            filterX = (this.width - bookmarkButtonWidth) / 2;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.modernnotification.log.filter_bookmarked"), b -> toggleBookmarkFilter())
                    .pos(filterX, y).size(bookmarkButtonWidth, 20).build());

            y += Y_PADDING;
        }

        this.contentY = y;
        applyFilters();
    }

    private void onSearchTextUpdate(String text) {
        this.filterText = text.toLowerCase();
        applyFilters();
    }
    private void setFilterType(FilterType type) {
        this.filterType = type;
        applyFilters();
    }
    private void setFilterCategory(NotificationCategory category) {
        this.filterCategory = category;
        applyFilters();
    }
    private void toggleBookmarkFilter() {
        this.filterBookmarked = !this.filterBookmarked;
        applyFilters();
    }
    private void applyFilters() {
        NotificationLog log = LogManager.getInstance().getLog();
        String text = this.filterText;
        Stream<NotificationData> adminStream = log.getAdminNotifications().stream();
        Stream<NotificationData> topRightStream = log.getTopRightNotifications().stream();
        if (this.filterBookmarked) {
            adminStream = adminStream.filter(NotificationData::isBookmarked);
            topRightStream = topRightStream.filter(NotificationData::isBookmarked);
        }
        if (this.filterCategory != null) {
            topRightStream = topRightStream.filter(d -> d.getCategory() == this.filterCategory);
        }
        if (!text.isEmpty()) {
            adminStream = adminStream.filter(d -> d.getSearchableText().contains(text));
            topRightStream = topRightStream.filter(d -> d.getSearchableText().contains(text));
        }
        this.filteredAdminLogs = adminStream.collect(Collectors.toList());
        this.filteredTopRightLogs = topRightStream.collect(Collectors.toList());
        if (this.deleteFilteredButton != null) {
            this.deleteFilteredButton.active = !this.filteredAdminLogs.isEmpty() || !this.filteredTopRightLogs.isEmpty();
        }
        this.scrollAmountAdmin = 0;
        this.scrollAmountTopRight = 0;
    }

    private void sendFailureNotification(Component message) {
        Notification notification = new Notification(
                NotificationType.LEFT,
                NotificationCategory.FAILURE,
                null,
                List.of(message),
                ServerConfig.INSTANCE.defaultDuration.get()
        );
        NotificationManager.getInstance().getRenderer().calculateDynamicWidth(notification);
        NotificationManager.getInstance().addNotification(notification);
    }

    private void toggleSelectionMode(Button b) {
        this.selectionMode = !this.selectionMode;
        this.selectedAdminIds.clear();
        this.selectedTopRightIds.clear();
        this.rebuildWidgets();
    }

    private void onBookmarkSelected(Button b) {
        LogManager.getInstance().bookmarkSelected(this.selectedAdminIds, this.selectedTopRightIds, true);
        toggleSelectionMode(b);
        applyFilters();
    }

    private void onUnbookmarkSelected(Button b) {
        LogManager.getInstance().bookmarkSelected(this.selectedAdminIds, this.selectedTopRightIds, false);
        toggleSelectionMode(b);
        applyFilters();
    }

    private void onDeleteSelected(Button b) {
        int count = (int) this.selectedAdminIds.stream()
                .map(id -> LogManager.getInstance().findDataById(id, NotificationType.ADMIN))
                .filter(opt -> opt.isPresent() && !opt.get().isBookmarked())
                .count();
        count += (int) this.selectedTopRightIds.stream()
                .map(id -> LogManager.getInstance().findDataById(id, NotificationType.TOP_RIGHT))
                .filter(opt -> opt.isPresent() && !opt.get().isBookmarked())
                .count();

        if (count == 0) {
            sendFailureNotification(Component.translatable("gui.modernnotification.log.no_selection"));
            return;
        }

        this.minecraft.setScreen(new ConfirmScreen(
                this::confirmDeleteSelected,
                Component.translatable("gui.modernnotification.log.confirm_delete_all_selected"),
                Component.translatable("gui.modernnotification.log.confirm_delete_message", count)
        ));
    }

    private void onDeleteFiltered(Button b) {
        List<NotificationData> adminsToDelete = (filterType == FilterType.ALL || filterType == FilterType.ADMIN) ? this.filteredAdminLogs : List.of();
        List<NotificationData> topRightsToDelete = (filterType == FilterType.ALL || filterType == FilterType.TOP_RIGHT) ? this.filteredTopRightLogs : List.of();

        int count = (int) adminsToDelete.stream().filter(d -> !d.isBookmarked()).count();
        count += (int) topRightsToDelete.stream().filter(d -> !d.isBookmarked()).count();

        if (count == 0) {
            sendFailureNotification(Component.translatable("gui.modernnotification.log.empty"));
            return;
        }

        this.minecraft.setScreen(new ConfirmScreen(
                (confirmed) -> this.confirmDeleteFiltered(confirmed, adminsToDelete, topRightsToDelete),
                Component.translatable("gui.modernnotification.log.confirm_delete_all_filtered"),
                Component.translatable("gui.modernnotification.log.confirm_delete_message", count)
        ));
    }

    private void confirmDeleteSelected(boolean confirmed) {
        this.minecraft.setScreen(this);
        if (confirmed) {
            LogManager.getInstance().deleteSelected(this.selectedAdminIds, this.selectedTopRightIds);
            this.selectedAdminIds.clear();
            this.selectedTopRightIds.clear();
        }
        this.rebuildWidgets();
    }

    private void confirmDeleteFiltered(boolean confirmed, List<NotificationData> adminsToDelete, List<NotificationData> topRightsToDelete) {
        this.minecraft.setScreen(this);
        if (confirmed) {
            LogManager.getInstance().deleteFiltered(adminsToDelete, topRightsToDelete);
        }
        this.applyFilters();
    }

    private void confirmDeleteSingle(boolean confirmed, NotificationData data) {
        this.minecraft.setScreen(this);
        if (confirmed) {
            Set<Long> idSet = Set.of(data.getId());
            if (data.getType() == NotificationType.ADMIN) {
                LogManager.getInstance().deleteSelected(idSet, Set.of());
            } else {
                LogManager.getInstance().deleteSelected(Set.of(), idSet);
            }
        }
        this.applyFilters();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.contextMenu != null) {
            return false;
        }
        if (mouseY < this.contentY) {
            return false;
        }
        if (this.filterType == FilterType.ALL) {
            if (mouseX < this.width / 2.0) {
                this.scrollAmountAdmin = clampScroll(this.scrollAmountAdmin - delta * 20, getMaxScroll(this.filteredAdminLogs, NotificationType.ADMIN));
            } else {
                this.scrollAmountTopRight = clampScroll(this.scrollAmountTopRight - delta * 20, getMaxScroll(this.filteredTopRightLogs, NotificationType.TOP_RIGHT));
            }
        } else if (this.filterType == FilterType.ADMIN) {
            this.scrollAmountAdmin = clampScroll(this.scrollAmountAdmin - delta * 20, getMaxScroll(this.filteredAdminLogs, NotificationType.ADMIN));
        } else if (this.filterType == FilterType.TOP_RIGHT) {
            this.scrollAmountTopRight = clampScroll(this.scrollAmountTopRight - delta * 20, getMaxScroll(this.filteredTopRightLogs, NotificationType.TOP_RIGHT));
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.contextMenu != null) {
            this.contextMenu.mouseClicked(mouseX, mouseY, button);
            this.contextMenu = null;
            return true;
        }
        if (mouseY < this.contentY) {
            return false;
        }
        int topOffset = this.contentY;
        double currentYAdmin = topOffset - this.scrollAmountAdmin;
        double currentYTopRight = topOffset - this.scrollAmountTopRight;

        if (this.filterType == FilterType.ALL || this.filterType == FilterType.ADMIN) {
            int laneX = (this.filterType == FilterType.ALL) ? (this.width / 4 - ADMIN_WIDTH / 2) : (this.width / 2 - ADMIN_WIDTH / 2);
            for (NotificationData data : this.filteredAdminLogs) {
                int width = (data.getDynamicWidth() > 0) ? data.getDynamicWidth() : ADMIN_WIDTH;
                int height = getEntryHeight(data);

                if (mouseY >= currentYAdmin && mouseY <= currentYAdmin + height) {
                    if (mouseX >= laneX && mouseX <= laneX + width) {
                        if (button == 1) {
                            openContextMenu(data);
                            return true;
                        }
                        if (button == 0) {
                            double bookmarkX1 = laneX + width - 15;
                            double bookmarkY1 = currentYAdmin + 5;
                            if (mouseX >= bookmarkX1 && mouseX <= bookmarkX1 + 10 && mouseY >= bookmarkY1 && mouseY <= bookmarkY1 + 10) {
                                LogManager.getInstance().toggleBookmark(data.getId(), NotificationType.ADMIN);
                                applyFilters();
                                return true;
                            }
                            if (!this.selectionMode) {
                                toggleSelectionMode(null);
                            }
                            toggleSelection(data.getId(), NotificationType.ADMIN);
                            return true;
                        }
                    }
                }
                currentYAdmin += height + ENTRY_PADDING;
            }
        }

        if (this.filterType == FilterType.ALL || this.filterType == FilterType.TOP_RIGHT) {
            int laneX = (this.filterType == FilterType.ALL) ? (this.width * 3 / 4 - RIGHT_WIDTH / 2) : (this.width / 2 - RIGHT_WIDTH / 2);
            for (NotificationData data : this.filteredTopRightLogs) {
                int width = (data.getDynamicWidth() > 0) ? data.getDynamicWidth() : RIGHT_WIDTH;
                int height = getEntryHeight(data);

                if (mouseY >= currentYTopRight && mouseY <= currentYTopRight + height) {
                    if (mouseX >= laneX && mouseX <= laneX + width) {
                        if (button == 1) {
                            openContextMenu(data);
                            return true;
                        }
                        if (button == 0) {
                            double bookmarkX1 = laneX + width - 15;
                            double bookmarkY1 = currentYTopRight + 5;
                            if (mouseX >= bookmarkX1 && mouseX <= bookmarkX1 + 10 && mouseY >= bookmarkY1 && mouseY <= bookmarkY1 + 10) {
                                LogManager.getInstance().toggleBookmark(data.getId(), NotificationType.TOP_RIGHT);
                                applyFilters();
                                return true;
                            }
                            if (!this.selectionMode) {
                                toggleSelectionMode(null);
                            }
                            toggleSelection(data.getId(), NotificationType.TOP_RIGHT);
                            return true;
                        }
                    }
                }
                currentYTopRight += height + ENTRY_PADDING;
            }
        }
        return false;
    }

    private void openContextMenu(NotificationData data) {
        if (data.isBookmarked()) return;
        ConfirmScreen contextMenu = new ConfirmScreen(
                (confirmed) -> this.confirmDeleteSingle(confirmed, data),
                Component.literal("Delete this log?"),
                Component.literal(data.getMessage().get(0).getString().substring(0, Math.min(20, data.getMessage().get(0).getString().length())) + "..."),
                Component.literal("Delete"),
                Component.literal("Cancel")
        );
        this.minecraft.setScreen(contextMenu);
    }


    private void toggleSelection(long id, NotificationType type) {
        Set<Long> selection = (type == NotificationType.ADMIN) ? this.selectedAdminIds : this.selectedTopRightIds;
        if (selection.contains(id)) {
            selection.remove(id);
        } else {
            selection.add(id);
        }
        int selectedCount = this.selectedAdminIds.size() + this.selectedTopRightIds.size();
        if (this.bookmarkSelectedButton != null) {
            this.bookmarkSelectedButton.active = selectedCount > 0;
            this.unbookmarkSelectedButton.active = selectedCount > 0;
            this.deleteSelectedButton.active = selectedCount > 0;
        }
    }
    private double clampScroll(double scroll, double maxScroll) {
        return Math.max(0, Math.min(scroll, maxScroll));
    }
    private double getMaxScroll(List<NotificationData> list, NotificationType type) {
        int listHeight = 0;
        for (NotificationData data : list) {
            listHeight += getEntryHeight(data) + ENTRY_PADDING;
        }
        int viewHeight = this.height - this.contentY;
        return Math.max(0, listHeight - viewHeight);
    }
    private int getEntryHeight(NotificationData data) {
        Font font = Minecraft.getInstance().font;
        if (data.getType() == NotificationType.TOP_RIGHT) {
            int titleHeight = (int) ((font.lineHeight * 1.0f + 1) * data.getTitle().size());
            int msgHeight = (int) ((font.lineHeight * 0.9f + 1) * data.getMessage().size());
            int textHeight = titleHeight + (titleHeight > 0 && msgHeight > 0 ? 1 : 0) + msgHeight;
            return Math.max(16 + 4 + 4, textHeight + 4 + 4);
        } else if (data.getType() == NotificationType.ADMIN) {
            int titleHeight = (int) ((font.lineHeight * 0.9f + 2) * data.getTitle().size());
            int msgHeight = (int) ((font.lineHeight * 0.8f + 2) * data.getMessage().size());
            return 6 + titleHeight + (titleHeight > 0 && msgHeight > 0 ? 2 : 0) + msgHeight + 6;
        }
        return 0;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.enableScissor(0, this.contentY, this.width, this.height);
        int topOffset = this.contentY;
        if (this.filterType == FilterType.ALL) {
            renderLane(guiGraphics, this.filteredAdminLogs, this.width / 4 - ADMIN_WIDTH / 2, topOffset, (int) this.scrollAmountAdmin);
            renderLane(guiGraphics, this.filteredTopRightLogs, this.width * 3 / 4 - RIGHT_WIDTH / 2, topOffset, (int) this.scrollAmountTopRight);
        } else if (this.filterType == FilterType.ADMIN) {
            renderLane(guiGraphics, this.filteredAdminLogs, this.width / 2 - ADMIN_WIDTH / 2, topOffset, (int) this.scrollAmountAdmin);
        } else if (this.filterType == FilterType.TOP_RIGHT) {
            renderLane(guiGraphics, this.filteredTopRightLogs, this.width / 2 - RIGHT_WIDTH / 2, topOffset, (int) this.scrollAmountTopRight);
        }
        guiGraphics.disableScissor();
        guiGraphics.fill(0, 0, this.width, this.contentY - 5, 0xA0000000);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (this.filteredAdminLogs.isEmpty() && this.filteredTopRightLogs.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.modernnotification.log.empty"), this.width / 2, this.height / 2, 0xAAAAAA);
        }
    }

    private void renderLane(GuiGraphics guiGraphics, List<NotificationData> list, int x, int topY, int scrollY) {
        int currentY = topY - scrollY;
        for (NotificationData data : list) {
            int height = getEntryHeight(data);
            if (currentY + height < topY || currentY > this.height) {
                currentY += height + ENTRY_PADDING;
                continue;
            }
            boolean isAdmin = data.getType() == NotificationType.ADMIN;
            boolean isSelected = (isAdmin ? this.selectedAdminIds.contains(data.getId()) : this.selectedTopRightIds.contains(data.getId()));

            if (isSelected) {
                int width = (isAdmin ? ((data.getDynamicWidth() > 0) ? data.getDynamicWidth() : ADMIN_WIDTH) : ((data.getDynamicWidth() > 0) ? data.getDynamicWidth() : RIGHT_WIDTH));
                guiGraphics.fill(x - 2, currentY - 2, x + width + 2, currentY + height + 2, 0x80FFFFFF);
            }
            if (isAdmin) {
                renderAdminEntry(guiGraphics, data, x, currentY);
            } else {
                renderTopRightEntry(guiGraphics, data, x, currentY);
            }
            currentY += height + ENTRY_PADDING;
        }
    }

    private void renderAdminEntry(GuiGraphics guiGraphics, NotificationData data, float x, float y) {
        Font font = this.font;
        int width = (data.getDynamicWidth() > 0) ? data.getDynamicWidth() : ADMIN_WIDTH;
        int height = getEntryHeight(data);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 100);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int bgColor1 = ColorUtils.parseColor(ServerConfig.INSTANCE.adminGradientStart.get());
        int bgColor2 = ColorUtils.parseColor(ServerConfig.INSTANCE.adminGradientEnd.get());
        guiGraphics.fillGradient(0, 0, width, height, bgColor1, bgColor2);
        RenderSystem.disableBlend();

        int textY = 6;
        List<Component> titleLines = data.getTitle();
        List<Component> msgLines = data.getMessage();

        if (!titleLines.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.9f, 0.9f, 1.0f);
            for (Component line : titleLines) {
                int textWidth = font.width(line);
                float textX = (width / 0.9f - textWidth) / 2.0f;
                guiGraphics.drawString(font, line, (int) textX, (int) (textY / 0.9f), 0xFFFFFF, true);
                textY += (font.lineHeight * 0.9f + 2);
            }
            guiGraphics.pose().popPose();
        }

        if (!titleLines.isEmpty() && !msgLines.isEmpty()) {
            textY += 2;
        }

        if (!msgLines.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.8f, 0.8f, 1.0f);
            for (Component line : msgLines) {
                int textWidth = font.width(line);
                float textX = (width / 0.8f - textWidth) / 2.0f;
                guiGraphics.drawString(font, line, (int) textX, (int) (textY / 0.8f), 0xFFFFFF, true);
                textY += (font.lineHeight * 0.8f + 2);
            }
            guiGraphics.pose().popPose();
        }
        renderBookmark(guiGraphics, width - 15, 5, data.isBookmarked());
        guiGraphics.pose().popPose();
    }

    private void renderTopRightEntry(GuiGraphics guiGraphics, NotificationData data, float x, float y) {
        Font font = this.font;
        int width = (data.getDynamicWidth() > 0) ? data.getDynamicWidth() : RIGHT_WIDTH;
        int height = getEntryHeight(data);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 100);

        int bgColor1 = ColorUtils.parseColor(ServerConfig.INSTANCE.backgroundColorTop.get());
        int bgColor2 = ColorUtils.parseColor(ServerConfig.INSTANCE.backgroundColorBottom.get());
        guiGraphics.fillGradient(0, 0, width, height, bgColor1, bgColor2);

        int iconX = 5;
        int iconY = (height - 16) / 2;
        String iconText = data.getCategory().getIcon();
        int iconTextWidth = font.width(iconText);
        guiGraphics.drawString(font, iconText, iconX + (16 - iconTextWidth) / 2 + 1, iconY + (16 - font.lineHeight) / 2, data.getCategory().getColor(), true);

        int textX = iconX + 16 + 4;
        List<Component> titleLines = data.getTitle();
        List<Component> msgLines = data.getMessage();

        int titleHeight = (int) ((font.lineHeight * 1.0f + 1) * titleLines.size());
        int msgHeight = (int) ((font.lineHeight * 0.9f + 1) * msgLines.size());
        int textHeight = titleHeight + (titleHeight > 0 && msgHeight > 0 ? 1 : 0) + msgHeight;
        int textY = 4 + (height - 4 - 4 - textHeight) / 2;

        if (!titleLines.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(1.0f, 1.0f, 1.0f);
            for (Component line : titleLines) {
                guiGraphics.drawString(font, line, (int) (textX / 1.0f), (int) (textY / 1.0f), 0xFFFFFF, true);
                textY += (font.lineHeight * 1.0f + 1);
            }
            guiGraphics.pose().popPose();
        }

        if (!titleLines.isEmpty() && !msgLines.isEmpty()) {
            textY += 1;
        }

        if (!msgLines.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.9f, 0.9f, 1.0f);
            for (Component line : msgLines) {
                guiGraphics.drawString(font, line, (int) (textX / 0.9f), (int) (textY / 0.9f), 0xCCCCCC, false);
                textY += (font.lineHeight * 0.9f + 1);
            }
            guiGraphics.pose().popPose();
        }
        renderBookmark(guiGraphics, width - 15, 5, data.isBookmarked());
        guiGraphics.pose().popPose();
    }

    private void renderBookmark(GuiGraphics guiGraphics, int x, int y, boolean bookmarked) {
        String star = bookmarked ? "★" : "☆";
        int color = bookmarked ? 0xFFFF00 : 0xAAAAAA;
        guiGraphics.drawString(this.font, star, x, y, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.filterText = "";
        super.onClose();
    }

    private enum FilterType {
        ALL,
        ADMIN,
        TOP_RIGHT
    }
}