package dev.dannytaylor.brick.fluxer;

import com.j4fluxer.fluxer.FluxerBuilder;
import com.j4fluxer.fluxer.FluxerImpl;
import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;

public class BrickFluxerBot {
    public final FluxerImpl bot = (FluxerImpl) FluxerBuilder.create(BrickConfig.instance.fluxerSettings.token.value()).build();

    public BrickFluxerBot() {
        BrickLoggerImpl.info("Starting Fluxer Bot...");
        this.bot.addEventListener(new BrickFluxerListener());
    }

    public void shutdown() {
        // There's currently no logout/shutdown method, but hytale seems to have no issue with it, so i think its fineee.
    }
}
