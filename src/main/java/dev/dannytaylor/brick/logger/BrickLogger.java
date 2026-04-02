package dev.dannytaylor.brick.logger;

import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.logger.log.Logger;

public class BrickLogger extends Logger {
    public void debug(String message) {
        if (BrickConfig.instance.debug.value()) super.debug(message);
    }

    public void debug(StringBuilder message) {
        if (BrickConfig.instance.debug.value()) super.debug(message);
    }
}
