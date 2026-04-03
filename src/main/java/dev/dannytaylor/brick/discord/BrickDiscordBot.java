package dev.dannytaylor.brick.discord;

import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;

public class BrickDiscordBot {
    public final GatewayDiscordClient bot = DiscordClientBuilder.create(BrickConfig.instance.discordSettings.token.value()).build().login().block();

    public BrickDiscordBot() {
        BrickLoggerImpl.info("Starting Discord Bot...");
        if (this.bot != null) {
            BrickDiscordListener.onReady(this.bot).subscribe();
            BrickDiscordListener.onCommand(this.bot).subscribe();
        }
    }

    public void shutdown() {
        if (this.bot != null) this.bot.logout().block();
    }
}
