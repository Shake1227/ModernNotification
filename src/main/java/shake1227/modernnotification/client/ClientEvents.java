package shake1227.modernnotification.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shake1227.modernnotification.ModernNotification;
// 修正: GUI と Keybindings をインポート
import shake1227.modernnotification.client.gui.NotificationLogScreen;
import shake1227.modernnotification.notification.NotificationManager;

@Mod.EventBusSubscriber(modid = ModernNotification.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                NotificationManager.getInstance().update();

                // 修正: キーが押されたらGUIを開く
                while (Keybindings.OPEN_LOG_KEY.consumeClick()) {
                    // 他の画面が開いていない時だけ開く
                    if (mc.screen == null) {
                        mc.setScreen(new NotificationLogScreen());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        NotificationManager manager = NotificationManager.getInstance();

        manager.getRenderer().render(
                event.getGuiGraphics(),
                manager.getLeftNotifications(),
                manager.getRightNotifications(),
                manager.getAdminNotification(),
                event.getPartialTick()
        );
    }
}