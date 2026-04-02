package dev.dannytaylor.brick;

import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.brick.discord.BrickDiscordBot;
import dev.dannytaylor.brick.fluxer.BrickFluxerBot;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;

public class BrickMain {
    private static BrickFluxerBot fluxerBot;
    private static BrickDiscordBot discordBot;
    public static volatile boolean running = true;

    static void main(String[] args) {
        BrickLoggerImpl.bootstrap();
        BrickConfig.bootstrap();
        new Thread(() -> fluxerBot = new BrickFluxerBot()).start();
        new Thread(() -> discordBot = new BrickDiscordBot()).start();
    }

    public static BrickFluxerBot getFluxer() {
        return fluxerBot;
    }

    public static BrickDiscordBot getDiscord() {
        return discordBot;
    }
}