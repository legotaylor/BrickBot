package dev.dannytaylor.brick.fluxer;

import com.j4fluxer.fluxer.FluxerImpl;
import com.j4fluxer.fluxer.FluxerBuilder;
import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;

public class BrickFluxerBot {
    public FluxerImpl bot;

    public BrickFluxerBot() {
        BrickLoggerImpl.info("Starting Fluxer Bot...");
        this.bot = (FluxerImpl) FluxerBuilder.create(BrickConfig.instance.fluxerSettings.token.value()).build();
        this.bot.addEventListener(new BrickFluxerListener());
    }

    public void shutdown() {
        // There's currently no logout/shutdown method, but hytale seems to have no issue with it, so i think its fineee.
    }
}
