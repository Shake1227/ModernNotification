package shake1227.modernnotification;

import com.mojang.logging.LogUtils;
// 修正: KeyMapping をインポート
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import shake1227.modernnotification.client.ClientEvents;
// 修正: Keybindings をインポート
import shake1227.modernnotification.client.Keybindings;
import shake1227.modernnotification.command.NotificationCommand;
import shake1227.modernnotification.config.ClientConfig;
import shake1227.modernnotification.core.ModSounds;
import shake1227.modernnotification.network.PacketHandler;

@Mod(ModernNotification.MOD_ID)
public class ModernNotification {
    public static final String MOD_ID = "modernnotification";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModernNotification() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        // 修正: キー登録イベントリスナーを追加
        modEventBus.addListener(this::onRegisterKeyMappings);

        ModSounds.SOUND_EVENTS.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "modernnotification-client.toml");

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("ModernNotification MOD constructor loaded.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting common setup...");
        event.enqueueWork(() -> {
            PacketHandler.register();
            LOGGER.info("PacketHandler registered via enqueueWork.");
        });
        LOGGER.info("Common setup finished.");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Starting client setup...");

        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        LOGGER.info("ClientEvents registered.");

        LOGGER.info("Client setup finished.");
    }

    // 修正: キーバインド登録イベント
    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(Keybindings.OPEN_LOG_KEY);
        LOGGER.info("Keybindings registered.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering commands...");
        NotificationCommand.register(event.getDispatcher());
        LOGGER.info("Commands registered.");
    }
}