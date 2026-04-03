package dev.dannytaylor.brick;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.brick.discord.BrickDiscordBot;
import dev.dannytaylor.brick.fluxer.BrickFluxerBot;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;

import javax.annotation.Nonnull;

public class BrickHytale extends JavaPlugin {
    private static BrickHytale instance;
    private static BrickFluxerBot fluxerBot;
    private static BrickDiscordBot discordBot;

    public BrickHytale(@Nonnull JavaPluginInit init) {
        super(init);
        BrickLoggerImpl.bootstrap();
        BrickConfig.bootstrap();
        instance = this;
    }

    public static BrickHytale getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
    }

    @Override
    protected void start() {
        new Thread(() -> fluxerBot = new BrickFluxerBot()).start();
        new Thread(() -> discordBot = new BrickDiscordBot()).start();
    }

    @Override
    protected void shutdown() {
        getFluxer().shutdown();
        getDiscord().shutdown();
        instance = null;
    }

    public static BrickFluxerBot getFluxer() {
        return fluxerBot;
    }

    public static BrickDiscordBot getDiscord() {
        return discordBot;
    }
}