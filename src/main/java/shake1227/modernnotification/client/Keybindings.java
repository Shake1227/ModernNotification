package shake1227.modernnotification.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class Keybindings {

    public static final String KEY_CATEGORY_MODERNNOTIFICATION = "key.modernnotification.category";
    public static final String KEY_OPEN_LOG = "key.modernnotification.open_log";

    public static final KeyMapping OPEN_LOG_KEY = new KeyMapping(
            KEY_OPEN_LOG,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT, // デフォルト: 右シフト
            KEY_CATEGORY_MODERNNOTIFICATION
    );
}