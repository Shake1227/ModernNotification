package shake1227.modernnotification.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shake1227.modernnotification.ModernNotification;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ModernNotification.MOD_ID);

    public static final RegistryObject<SoundEvent> NOTIFICATION_SOUND =
            SOUND_EVENTS.register("notificationsound", () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(ModernNotification.MOD_ID, "notificationsound"), 16.0F));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}