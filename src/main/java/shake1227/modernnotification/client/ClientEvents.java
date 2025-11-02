package shake1227.modernnotification.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shake1227.modernnotification.ModernNotification;
import shake1227.modernnotification.notification.NotificationManager;

@Mod.EventBusSubscriber(modid = ModernNotification.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (Minecraft.getInstance().player != null) {
                NotificationManager.getInstance().update();
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

