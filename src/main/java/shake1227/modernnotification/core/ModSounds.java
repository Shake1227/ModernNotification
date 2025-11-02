package shake1227.modernnotification.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shake1227.modernnotification.ModernNotification;

public class ModSounds {

    // 1. サウンドイベント用の DeferredRegister を作成
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ModernNotification.MOD_ID);

    // 2. カスタムサウンドイベントを登録
    // "notificationsound" は sounds.json で定義するキーと一致させる
    public static final RegistryObject<SoundEvent> NOTIFICATION_SOUND =
            SOUND_EVENTS.register("notificationsound", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ModernNotification.MOD_ID, "notificationsound")));

    // 3. register メソッド (ModernNotification.java から呼び出す)
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}