package dev.dannytaylor.brick.config;

import dev.dannytaylor.brick.data.StaticVariables;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.serializers.TomlSerializer;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueList;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class BrickConfig extends ReflectiveConfig {
    public static BrickConfig instance = ConfigHelper.register(StaticVariables.id, StaticVariables.id, BrickConfig.class);

    public static void bootstrap() {
        BrickLoggerImpl.info("Initialized Config");
    }

    public final FluxerSettings fluxerSettings = new FluxerSettings();

    public static class FluxerSettings extends Section {
        public final TrackedValue<String> token = this.value("REPLACE_THIS_WITH_YOUR_FLUXER_TOKEN");
        public final TrackedValue<String> guildId = this.value("REPLACE_THIS_WITH_YOUR_FLUXER_GUILD_ID");
        public final TrackedValue<ValueList<String>> commandRoleId = this.list("", "REPLACE_THIS_WITH_YOUR_FLUXER_ROLE_ID");

        public final TrackedValue<String> commandPrefix = this.value("!");

        public final TrackedValue<String> announcementCommand = this.value("announce");
        public final TrackedValue<String> announcementRoleId = this.value("REPLACE_THIS_WITH_YOUR_FLUXER_ROLE_ID");
        public final TrackedValue<String> announcementChannelId = this.value("REPLACE_THIS_WITH_YOUR_FLUXER_CHANNEL_ID");

        public final TrackedValue<String> contentCommand = this.value("content");
        public final TrackedValue<String> contentRoleId = this.value("REPLACE_THIS_WITH_YOUR_FLUXER_ROLE_ID");
        public final TrackedValue<String> contentChannelId = this.value("REPLACE_THIS_WITH_YOUR_FLUXER_CHANNEL_ID");

        public final TrackedValue<String> scheduleCommand = this.value("scheduled");
        public final TrackedValue<String> scheduleRoleId = this.value("REPLACE_THIS_WITH_YOUR_FLUXER_ROLE_ID");
        public final TrackedValue<String> scheduleChannelId = this.value("REPLACE_THIS_WITH_YOUR_FLUXER_CHANNEL_ID");

        public final TrackedValue<String> modTesterCommand = this.value("tester");
        public final TrackedValue<String> modTesterRoleId = this.value("REPLACE_THIS_WITH_YOUR_FLUXER_ROLE_ID");
        public final TrackedValue<String> modTesterChannelId = this.value("REPLACE_THIS_WITH_YOUR_FLUXER_CHANNEL_ID");
    }

    public final DiscordSettings discordSettings = new DiscordSettings();

    public static class DiscordSettings extends Section {
        public final TrackedValue<String> token = this.value("REPLACE_THIS_WITH_YOUR_DISCORD_TOKEN");
        public final TrackedValue<Long> applicationId = this.value(0L);
        public final TrackedValue<Long> guildId = this.value(0L);

        public final TrackedValue<ValueList<Long>> commandRoleId = this.list(0L);

        public final TrackedValue<Long> announcementRoleId = this.value(0L);
        public final TrackedValue<Long> announcementChannelId = this.value(0L);

        public final TrackedValue<Long> contentRoleId = this.value(0L);
        public final TrackedValue<Long> contentChannelId = this.value(0L);

        public final TrackedValue<Long> scheduleRoleId = this.value(0L);
        public final TrackedValue<Long> scheduleChannelId = this.value(0L);

        public final TrackedValue<Long> modTesterRoleId = this.value(0L);
        public final TrackedValue<Long> modTesterChannelId = this.value(0L);

        public final TrackedValue<String> status = this.value("Watching for announcements!");
    }

    public final TrackedValue<Boolean> debug = this.value(false);

    public static void reload() {
        try {
            TomlSerializer.INSTANCE.deserialize(instance, Files.newInputStream(new File(StaticVariables.name + "Assets/" + StaticVariables.id + ".toml").toPath()));
        } catch (Exception error) {
            BrickLoggerImpl.error("Error occurred whilst reloading config: " + error);
        }
    }

    public static boolean containsIgnoresCase(List<String> strings, String value) {
        for (String string : strings) {
            if (string.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    public static void toFile() {
        BrickLoggerImpl.info("Saving config to file!");
        instance.save();
    }
}
