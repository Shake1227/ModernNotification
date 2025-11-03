package shake1227.modernnotification.client;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import shake1227.modernnotification.ModernNotification;

public class ClientOnlySetup {
    public static void registerClientEvents(IEventBus modEventBus) {
        ModernNotification.LOGGER.info("Registering client-only MOD event listeners...");
        modEventBus.addListener(ClientOnlySetup::onClientSetup);
        modEventBus.addListener(ClientOnlySetup::onRegisterKeyMappings);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        ModernNotification.LOGGER.info("Starting client setup...");
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        ModernNotification.LOGGER.info("ClientEvents registered.");

        ModernNotification.LOGGER.info("Client setup finished.");
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(Keybindings.OPEN_LOG_KEY);
        ModernNotification.LOGGER.info("Keybindings registered.");
    }
}