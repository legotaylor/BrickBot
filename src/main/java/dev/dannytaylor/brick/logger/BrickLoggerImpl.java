package dev.dannytaylor.brick.logger;

import com.hypixel.hytale.logger.HytaleLogger;

import java.util.logging.Level;

public class BrickLoggerImpl {
    private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();

    public static void bootstrap() {
    }

    public static void info(String message) {
        logger.at(Level.INFO).log(message);
    }

    public static void warn(String message) {
        logger.at(Level.WARNING).log(message);
    }

    public static void error(String message) {
        logger.at(Level.SEVERE).log(message);
    }
}
