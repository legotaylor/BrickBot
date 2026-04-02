package dev.dannytaylor.brick.fluxer;

import com.j4fluxer.fluxer.Fluxer;
import com.j4fluxer.fluxer.FluxerBuilder;
import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;

public class BrickFluxerBot {
    public Fluxer bot;

    public BrickFluxerBot() {
        BrickLoggerImpl.info("Starting Fluxer Bot...");
        this.bot = FluxerBuilder.create(BrickConfig.instance.fluxerSettings.token.value()).build();
        this.bot.addEventListener(new BrickFluxerListener());
    }
}
