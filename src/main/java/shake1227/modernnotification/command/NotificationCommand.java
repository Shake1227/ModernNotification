package shake1227.modernnotification.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import shake1227.modernnotification.ModernNotification;
import shake1227.modernnotification.config.ClientConfig;
import shake1227.modernnotification.core.NotificationCategory;
import shake1227.modernnotification.core.NotificationType;
import shake1227.modernnotification.network.PacketHandler;
import shake1227.modernnotification.network.S2CNotificationPacket;
import shake1227.modernnotification.util.TextFormattingUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NotificationCommand {

    private static final SuggestionProvider<CommandSourceStack> CATEGORY_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(NotificationCategory.values()).map(NotificationCategory::getSerializedName),
                    builder
            );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("leftnotification")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("category", StringArgumentType.string()).suggests(CATEGORY_SUGGESTIONS)
                                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                                .executes(ctx -> executeLeft(
                                                        ctx,
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        StringArgumentType.getString(ctx, "message"),
                                                        -1
                                                ))
                                        )
                                )
                                .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("category", StringArgumentType.string()).suggests(CATEGORY_SUGGESTIONS)
                                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                                        .executes(ctx -> executeLeft(
                                                                ctx,
                                                                EntityArgument.getPlayers(ctx, "targets"),
                                                                StringArgumentType.getString(ctx, "message"),
                                                                IntegerArgumentType.getInteger(ctx, "duration")
                                                        ))
                                                )
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("topnotification")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("category", StringArgumentType.string()).suggests(CATEGORY_SUGGESTIONS)
                                        .then(Commands.argument("title", StringArgumentType.string())
                                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                                        .executes(ctx -> executeTopRight(
                                                                ctx,
                                                                EntityArgument.getPlayers(ctx, "targets"),
                                                                StringArgumentType.getString(ctx, "title"),
                                                                StringArgumentType.getString(ctx, "message"),
                                                                -1
                                                        ))
                                                )
                                        )
                                )
                                .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("category", StringArgumentType.string()).suggests(CATEGORY_SUGGESTIONS)
                                                .then(Commands.argument("title", StringArgumentType.string())
                                                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                                                .executes(ctx -> executeTopRight(
                                                                        ctx,
                                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                                        StringArgumentType.getString(ctx, "title"),
                                                                        StringArgumentType.getString(ctx, "message"),
                                                                        IntegerArgumentType.getInteger(ctx, "duration")
                                                                ))
                                                        )
                                                )
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("adminnotification")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("title", StringArgumentType.string())
                                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                                .executes(ctx -> executeAdmin(
                                                        ctx,
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        StringArgumentType.getString(ctx, "title"),
                                                        StringArgumentType.getString(ctx, "message"),
                                                        -1
                                                ))
                                        )
                                )
                                .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("title", StringArgumentType.string())
                                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                                        .executes(ctx -> executeAdmin(
                                                                ctx,
                                                                EntityArgument.getPlayers(ctx, "targets"),
                                                                StringArgumentType.getString(ctx, "title"),
                                                                StringArgumentType.getString(ctx, "message"),
                                                                IntegerArgumentType.getInteger(ctx, "duration")
                                                        ))
                                                )
                                        )
                                )
                        )
        );
    }

    private static String cleanString(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static NotificationCategory getCategoryFromString(CommandContext<CommandSourceStack> ctx, String argName) throws CommandSyntaxException {
        String categoryName = StringArgumentType.getString(ctx, argName);
        for (NotificationCategory category : NotificationCategory.values()) {
            if (category.getSerializedName().equalsIgnoreCase(categoryName)) {
                return category;
            }
        }
        ctx.getSource().sendFailure(Component.literal("Invalid category: " + categoryName));
        return null;
    }

    private static int executeLeft(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, String message, int duration) throws CommandSyntaxException {
        NotificationCategory category = getCategoryFromString(ctx, "category");
        if (category == null) return 0;

        int finalDuration = duration > 0 ? duration : ClientConfig.INSTANCE.defaultDuration.get();

        List<Component> messageComponentList = TextFormattingUtils.parseLegacyText(cleanString(message));

        ModernNotification.LOGGER.info("Executing 'left' command...");
        ModernNotification.LOGGER.info("Type: LEFT, Category: {}, Msg: {}, Duration: {}", category, message, finalDuration);

        S2CNotificationPacket packet = new S2CNotificationPacket(
                NotificationType.LEFT,
                category,
                null,
                messageComponentList,
                finalDuration
        );

        sendPacketToPlayers(targets, packet);
        return 1;
    }

    private static int executeTopRight(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, String title, String message, int duration) throws CommandSyntaxException {
        NotificationCategory category = getCategoryFromString(ctx, "category");
        if (category == null) return 0;

        int finalDuration = duration > 0 ? duration : ClientConfig.INSTANCE.defaultDuration.get();

        List<Component> titleComponentList = TextFormattingUtils.parseLegacyText(cleanString(title));
        List<Component> messageComponentList = TextFormattingUtils.parseLegacyText(cleanString(message));

        ModernNotification.LOGGER.info("Executing 'topright' command...");
        ModernNotification.LOGGER.info("Type: TOP_RIGHT, Category: {}, Title: {}, Msg: {}, Duration: {}", category, title, message, finalDuration);

        S2CNotificationPacket packet = new S2CNotificationPacket(
                NotificationType.TOP_RIGHT,
                category,
                titleComponentList,
                messageComponentList,
                finalDuration
        );

        sendPacketToPlayers(targets, packet);
        return 1;
    }

    private static int executeAdmin(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, String title, String message, int duration) throws CommandSyntaxException {
        int finalDuration = duration > 0 ? duration : ClientConfig.INSTANCE.defaultDuration.get();

        List<Component> titleComponentList = TextFormattingUtils.parseLegacyText(cleanString(title));
        List<Component> messageComponentList = TextFormattingUtils.parseLegacyText(cleanString(message));

        ModernNotification.LOGGER.info("Executing 'admin' command...");
        ModernNotification.LOGGER.info("Type: ADMIN, Title: {}, Msg: {}, Duration: {}", title, message, finalDuration);

        S2CNotificationPacket packet = new S2CNotificationPacket(
                NotificationType.ADMIN,
                NotificationCategory.SYSTEM,
                titleComponentList,
                messageComponentList,
                finalDuration
        );

        sendPacketToPlayers(targets, packet);
        return 1;
    }

    private static void sendPacketToPlayers(Iterable<ServerPlayer> players, S2CNotificationPacket packet) {
        ModernNotification.LOGGER.info("Sending packet to players...");
        for (ServerPlayer player : players) {
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
        ModernNotification.LOGGER.info("Packet sent to players.");
    }
}

