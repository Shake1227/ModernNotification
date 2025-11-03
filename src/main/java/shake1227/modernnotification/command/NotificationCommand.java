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
import shake1227.modernnotification.config.ServerConfig;
import shake1227.modernnotification.core.NotificationCategory;
import shake1227.modernnotification.core.NotificationType;
import shake1227.modernnotification.network.PacketHandler;
import shake1227.modernnotification.network.S2CNotificationPacket;
import shake1227.modernnotification.util.TextFormattingUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
                                        .then(Commands.argument("message", StringArgumentType.string())
                                                .executes(ctx -> executeLeft(
                                                        ctx,
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        StringArgumentType.getString(ctx, "message"),
                                                        -1
                                                ))
                                                .then(Commands.argument("duration", IntegerArgumentType.integer(1))
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
                                                .then(Commands.argument("message", StringArgumentType.string())
                                                        .executes(ctx -> executeTopRight(
                                                                ctx,
                                                                EntityArgument.getPlayers(ctx, "targets"),
                                                                StringArgumentType.getString(ctx, "title"),
                                                                StringArgumentType.getString(ctx, "message"),
                                                                -1
                                                        ))
                                                        .then(Commands.argument("duration", IntegerArgumentType.integer(1))
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
                                        .then(Commands.argument("message", StringArgumentType.string())
                                                .executes(ctx -> executeAdmin(
                                                        ctx,
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        StringArgumentType.getString(ctx, "title"),
                                                        StringArgumentType.getString(ctx, "message"),
                                                        -1
                                                ))
                                                .then(Commands.argument("duration", IntegerArgumentType.integer(1))
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

    private static void sendFailureNotification(CommandContext<CommandSourceStack> ctx, String errorMessage) {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
            S2CNotificationPacket packet = new S2CNotificationPacket(
                    NotificationType.LEFT,
                    NotificationCategory.FAILURE,
                    null,
                    Collections.singletonList(Component.literal(errorMessage)),
                    ServerConfig.INSTANCE.defaultDuration.get()
            );
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
        } else {
            ctx.getSource().sendFailure(Component.literal(errorMessage));
        }
    }


    private static NotificationCategory getCategoryFromString(CommandContext<CommandSourceStack> ctx, String argName) throws CommandSyntaxException {
        String categoryName = StringArgumentType.getString(ctx, argName);
        for (NotificationCategory category : NotificationCategory.values()) {
            if (category.getSerializedName().equalsIgnoreCase(categoryName)) {
                return category;
            }
        }
        sendFailureNotification(ctx, "Invalid category: " + categoryName);
        return null;
    }

    private static int executeLeft(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, String message, int duration) throws CommandSyntaxException {
        NotificationCategory category = getCategoryFromString(ctx, "category");
        if (category == null) return 0;
        int finalDuration = duration > 0 ? duration : ServerConfig.INSTANCE.defaultDuration.get();
        List<Component> messageComponentList = TextFormattingUtils.parseLegacyText(message);

        ModernNotification.LOGGER.info("Executing 'left' command...");
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
        int finalDuration = duration > 0 ? duration : ServerConfig.INSTANCE.defaultDuration.get();
        List<Component> titleComponentList = TextFormattingUtils.parseLegacyText(title);
        List<Component> messageComponentList = TextFormattingUtils.parseLegacyText(message);

        ModernNotification.LOGGER.info("Executing 'topright' command...");
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
        int finalDuration = duration > 0 ? duration : ServerConfig.INSTANCE.defaultDuration.get();
        List<Component> titleComponentList = TextFormattingUtils.parseLegacyText(title);
        List<Component> messageComponentList = TextFormattingUtils.parseLegacyText(message);

        ModernNotification.LOGGER.info("Executing 'admin' command...");
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