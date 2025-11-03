package shake1227.modernnotification;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import shake1227.modernnotification.client.ClientOnlySetup;
import shake1227.modernnotification.command.NotificationCommand;
import shake1227.modernnotification.config.ClientConfig;
import shake1227.modernnotification.config.ServerConfig;
import shake1227.modernnotification.core.ModSounds;
import shake1227.modernnotification.network.PacketHandler;

@Mod(ModernNotification.MOD_ID)
public class ModernNotification {
    public static final String MOD_ID = "modernnotification";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModernNotification() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "modernnotification-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, "modernnotification-server.toml");

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("ModernNotification MOD constructor loaded.");

        DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
            ClientOnlySetup.registerClientEvents(modEventBus);
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting common setup...");
        event.enqueueWork(() -> {
            PacketHandler.register();
            LOGGER.info("PacketHandler registered via enqueueWork.");
        });
        LOGGER.info("Common setup finished.");
    }


    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering commands...");
        NotificationCommand.register(event.getDispatcher());
        LOGGER.info("Commands registered.");
    }
}