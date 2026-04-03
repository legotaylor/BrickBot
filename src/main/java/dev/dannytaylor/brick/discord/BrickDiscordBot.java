package dev.dannytaylor.brick.discord;

import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;

public class BrickDiscordBot {
    public GatewayDiscordClient bot;

    public BrickDiscordBot() {
        BrickLoggerImpl.info("Starting Discord Bot...");
        this.bot = DiscordClientBuilder.create(BrickConfig.instance.discordSettings.token.value()).build().login().block();
        if (this.bot != null) {
            BrickDiscordListener.onReady(this.bot).subscribe();
            BrickDiscordListener.onCommand(this.bot).subscribe();
        }
    }

    public void shutdown() {
        this.bot.logout().block();
    }
}
