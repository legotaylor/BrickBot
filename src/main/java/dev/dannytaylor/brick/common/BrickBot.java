package dev.dannytaylor.brick.common;

import com.j4fluxer.entities.channel.TextChannel;
import com.j4fluxer.entities.guild.Guild;
import com.j4fluxer.entities.message.Message;
import com.j4fluxer.internal.requests.RestAction;
import dev.dannytaylor.brick.BrickMain;
import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.Callable;

public class BrickBot {
    private static Guild getFluxerServer() {
        return BrickMain.getFluxer().bot.getGuildById(BrickConfig.instance.fluxerSettings.guildId.value());
    }

    private static TextChannel getFluxerChannel(String id) {
        return getFluxerServer().getTextChannelById(id);
    }

    private static RestAction<Message> createFluxerMessage(String roleId, String message, MessageType type) {
        return getFluxerChannel(type.getFluxer().getChannelId()).sendMessage(pingRole(roleId) + "\n" + message);
    }

    private static Optional<Mono<discord4j.core.object.entity.Guild>> getDiscordServer() {
        return Optional.of(BrickMain.getDiscord().bot.getGuildById(Snowflake.of(BrickConfig.instance.discordSettings.guildId.value())));
    }

    private static Optional<Mono<GuildChannel>> getDiscordChannel(Long id) {
        Optional<Mono<discord4j.core.object.entity.Guild>> server = getDiscordServer();
        if (server.isPresent()) {
            discord4j.core.object.entity.Guild guild = server.get().block();
            if (guild != null) return Optional.of(guild.getChannelById(Snowflake.of(id)));
        }
        return Optional.empty();
    }

    private static void sendDiscordMessage(String roleId, String message, MessageType type) {
        Optional<Mono<GuildChannel>> channel = getDiscordChannel(type.getDiscord().getChannelId());
        channel.ifPresent(guildChannelMono -> guildChannelMono.ofType(MessageChannel.class).flatMap(chat -> chat.createMessage(pingRole(roleId) + "\n" + message)).subscribe());
    }

    public static void send(String message, String username, MessageType type) {
        String processedMessage = getMessage(message, username);
        createFluxerMessage(type.getFluxer().getRoleId(), processedMessage, type).queue();
        sendDiscordMessage(type.getDiscord().getRoleId().toString(), processedMessage, type);
        BrickLoggerImpl.info(type.name() + ": " + processedMessage);
    }

    private static String getMessage(String message, String username) {
        return message + "\n\n-# sent by " + username;
    }

    private static String pingRole(String roleId) {
        return !roleId.isBlank() ? "<@&" + roleId + ">" : "";
    }

    public enum MessageType {
        ANNOUNCEMENT(new FluxerData(() -> BrickConfig.instance.fluxerSettings.announcementRoleId.value(), () -> BrickConfig.instance.fluxerSettings.announcementChannelId.value()), new DiscordData(() -> BrickConfig.instance.discordSettings.announcementRoleId.value(), () -> BrickConfig.instance.discordSettings.announcementChannelId.value())),
        CONTENT(new FluxerData(() -> BrickConfig.instance.fluxerSettings.contentRoleId.value(), () -> BrickConfig.instance.fluxerSettings.contentChannelId.value()), new DiscordData(() -> BrickConfig.instance.discordSettings.contentRoleId.value(), () -> BrickConfig.instance.discordSettings.contentChannelId.value())),
        SCHEDULE(new FluxerData(() -> BrickConfig.instance.fluxerSettings.scheduleRoleId.value(), () -> BrickConfig.instance.fluxerSettings.scheduleChannelId.value()), new DiscordData(() -> BrickConfig.instance.discordSettings.scheduleRoleId.value(), () -> BrickConfig.instance.discordSettings.scheduleChannelId.value())),
        MOD_TESTER(new FluxerData(() -> BrickConfig.instance.fluxerSettings.modTesterRoleId.value(), () -> BrickConfig.instance.fluxerSettings.modTesterChannelId.value()), new DiscordData(() -> BrickConfig.instance.discordSettings.modTesterRoleId.value(), () -> BrickConfig.instance.discordSettings.modTesterChannelId.value()));

        private final FluxerData fluxer;
        private final DiscordData discord;

        MessageType(FluxerData fluxer, DiscordData discord) {
            this.fluxer = fluxer;
            this.discord = discord;
        }

        public FluxerData getFluxer() {
            return this.fluxer;
        }

        public DiscordData getDiscord() {
            return this.discord;
        }
    }

    public record FluxerData(Callable<String> roleId, Callable<String> channelId) {
        public String getRoleId() {
            try {
                return this.roleId.call();
            } catch (Exception error) {
                BrickLoggerImpl.error("Error caught whilst getting roleId: " + error);
            }
            return "";
        }

        public String getChannelId() {
            try {
                return this.channelId.call();
            } catch (Exception error) {
                BrickLoggerImpl.error("Error caught whilst getting channelId: " + error);
            }
            return "";
        }
    }

    public record DiscordData(Callable<Long> roleId, Callable<Long> channelId) {
        public Long getRoleId() {
            try {
                return this.roleId.call();
            } catch (Exception error) {
                BrickLoggerImpl.error("Error caught whilst getting roleId: " + error);
            }
            return 0L;
        }

        public Long getChannelId() {
            try {
                return this.channelId.call();
            } catch (Exception error) {
                BrickLoggerImpl.error("Error caught whilst getting channelId: " + error);
            }
            return 0L;
        }
    }
}
