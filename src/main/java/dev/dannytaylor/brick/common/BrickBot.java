package dev.dannytaylor.brick.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.j4fluxer.entities.channel.TextChannel;
import com.j4fluxer.entities.guild.Guild;
import com.j4fluxer.entities.message.Message;
import com.j4fluxer.internal.requests.Requester;
import com.j4fluxer.internal.requests.RestAction;
import dev.dannytaylor.brick.BrickHytale;
import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.brick.fluxer.BrickFluxerBot;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrickBot {
    private static Guild getFluxerServer() {
        return BrickHytale.getFluxer().bot.getGuildById(BrickConfig.instance.fluxerSettings.guildId.value());
    }

    private static RestAction<Map<String, String>> getFluxerEmojis(Guild guild) {
        if (guild != null) {
            Requester requester = BrickHytale.getFluxer().bot.getRequester();
            return new RestAction<>(requester, BrickFluxerBot.GET_GUILD_EMOJIS.compile(guild.getId())) {
                @Override
                protected Map<String, String> handleResponse(String jsonStr) throws Exception {
                    Map<String, String> emojiMap = new HashMap<>();
                    JsonNode array = mapper.readTree(jsonStr);
                    if (array.isArray()) {
                        for (JsonNode node : array) {
                            if (node.has("id") && node.has("name"))
                                emojiMap.put(node.get("name").asText(), node.get("id").asText());
                        }
                    }
                    return emojiMap;
                }
            };
        }
        return null;
    }

    private static String parseFluxerEmojis(Guild guild, String message) {
        if (guild != null) {
            Pattern pattern = Pattern.compile(":(\\w+):");
            Matcher matcher = pattern.matcher(message);
            StringBuilder builder = new StringBuilder();
            while (matcher.find()) {
                String name = matcher.group(1);
                Map<String, String> emojis = new HashMap<>();
                getFluxerEmojis(guild).queue(emojis::putAll);
                String id = emojis.get(name);
                if (id != null) matcher.appendReplacement(builder, "<:" + name + ":" + id + ">");
                else matcher.appendReplacement(builder, matcher.group());
            }
            matcher.appendTail(builder);
            return builder.toString();
        }
        return message;
    }

    private static TextChannel getFluxerChannel(String id) {
        return getFluxerServer().getTextChannelById(id);
    }

    private static RestAction<Message> createFluxerMessage(String roleId, String message, MessageType type) {
        return getFluxerChannel(type.getFluxer().getChannelId()).sendMessage(pingRole(roleId) + "\n" + parseFluxerEmojis(getFluxerServer(), message));
    }

    private static Optional<Mono<discord4j.core.object.entity.Guild>> getDiscordServer() {
        return Optional.of(BrickHytale.getDiscord().bot.getGuildById(Snowflake.of(BrickConfig.instance.discordSettings.guildId.value())));
    }

    private static Optional<Mono<GuildChannel>> getDiscordChannel(Long id) {
        Optional<Mono<discord4j.core.object.entity.Guild>> server = getDiscordServer();
        if (server.isPresent()) {
            discord4j.core.object.entity.Guild guild = server.get().block();
            if (guild != null) return Optional.of(guild.getChannelById(Snowflake.of(id)));
        }
        return Optional.empty();
    }

    private static Map<String, String> getDiscordEmojis(Mono<discord4j.core.object.entity.Guild> guildMono) {
        HashMap<String, String> emojiMap = new HashMap<>();
        if (guildMono != null) {
            discord4j.core.object.entity.Guild guild = guildMono.block();
            if (guild != null) guild.getEmojis().subscribe(emoji -> emojiMap.put(emoji.getName(), emoji.getId().asString()));
        }
        return emojiMap;
    }

    private static String parseDiscordEmojis(Mono<discord4j.core.object.entity.Guild> guild, String message) {
        if (guild != null) {
            Pattern pattern = Pattern.compile(":(\\w+):");
            Matcher matcher = pattern.matcher(message);
            StringBuilder builder = new StringBuilder();
            while (matcher.find()) {
                String name = matcher.group(1);
                String id = getDiscordEmojis(guild).get(name);
                if (id != null) matcher.appendReplacement(builder, "<:" + name + ":" + id + ">");
                else matcher.appendReplacement(builder, matcher.group());
            }
            matcher.appendTail(builder);
            return builder.toString();
        }
        return message;
    }

    private static void sendDiscordMessage(String roleId, String message, MessageType type) {
        Optional<Mono<GuildChannel>> channel = getDiscordChannel(type.getDiscord().getChannelId());
        channel.ifPresent(guildChannelMono -> guildChannelMono.ofType(MessageChannel.class).flatMap(chat -> chat.createMessage(pingRole(roleId) + "\n" + parseDiscordEmojis(getDiscordServer().orElse(null), message))).subscribe());
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
