package dev.dannytaylor.brick.discord;

import dev.dannytaylor.brick.common.BrickBot;
import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;

public class BrickDiscordListener {
    private static ApplicationCommandRequest createAnnounceCommand() {
        return ApplicationCommandRequest.builder()
                .name("announce")
                .description("Send an announcement")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("type")
                        .description("Type of announcement")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("announcement")
                                .value(BrickBot.MessageType.ANNOUNCEMENT.name())
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("content")
                                .value(BrickBot.MessageType.CONTENT.name())
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("schedule")
                                .value(BrickBot.MessageType.SCHEDULE.name())
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("mod tester")
                                .value(BrickBot.MessageType.MOD_TESTER.name())
                                .build())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("message")
                        .description("Message content")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    public static Flux<Void> onReady(GatewayDiscordClient bot) {
        return bot.on(ReadyEvent.class, event -> {
            String status = BrickConfig.instance.discordSettings.status.value();
            if (!status.isBlank()) bot.updatePresence(ClientPresence.online(ClientActivity.custom(status))).subscribe();

            return registerCommand(
                    bot,
                    BrickConfig.instance.discordSettings.applicationId.value(),
                    BrickConfig.instance.discordSettings.guildId.value(),
                    createAnnounceCommand())
                    .doOnNext(cmd -> BrickLoggerImpl.info("Registered discord command: " + cmd.name()))
                    .doOnError(err -> BrickLoggerImpl.error("Failed to register discord command: " + err))
                    .then();
        });
    }

    public static Flux<Void> onCommand(GatewayDiscordClient bot) {
        return bot.on(ApplicationCommandInteractionEvent.class, event -> {
            if (!event.getCommandName().equals("announce")) return Mono.empty();
            User user = event.getInteraction().getUser();
            Member member = user.asMember(Snowflake.of(BrickConfig.instance.discordSettings.guildId.value())).block();
            if (member != null && hasRole(member.getRoleIds())) {
                Optional<ApplicationCommandInteraction> interaction = event.getInteraction().getCommandInteraction();
                if (interaction.isPresent()) {
                    String typeStr = null;
                    String message = null;

                    for (ApplicationCommandInteractionOption option : interaction.get().getOptions()) {
                        if (option.getName().equals("type")) typeStr = option.getValue().map(ApplicationCommandInteractionOptionValue::asString).orElse(null);
                        if (option.getName().equals("message")) message = option.getValue().map(ApplicationCommandInteractionOptionValue::asString).orElse(null);
                    }

                    if (typeStr != null && message != null) {
                        BrickBot.MessageType type;

                        try {
                            type = BrickBot.MessageType.valueOf(typeStr);
                        } catch (IllegalArgumentException e) {
                            return event.reply().withContent("Invalid type!").withEphemeral(true);
                        }

                        BrickBot.send(message, "@" + user.getUsername(), type, BrickBot.Platform.DISCORD);
                        return event.reply().withContent("Sent announcement!").withEphemeral(true);
                    } else return event.reply().withContent("Missing arguments!").withEphemeral(true);
                } else return event.reply().withContent("Command interaction could not be found!").withEphemeral(true);
            } else return event.reply().withContent("You don't have permission to do that!").withEphemeral(true);
        });
    }

    private static Mono<ApplicationCommandData> registerCommand(GatewayDiscordClient bot, long applicationId, long guildId, ApplicationCommandRequest commandRequest) {
        return bot.getRestClient().getApplicationService().createGuildApplicationCommand(applicationId, guildId, commandRequest);
    }

    public static boolean hasRole(Set<Snowflake> memberRoles) {
        for (Snowflake roleId : memberRoles) if (BrickConfig.instance.discordSettings.commandRoleId.value().contains(roleId.asLong())) return true;
        return false;
    }
}
