package dev.dannytaylor.brick.fluxer;

import com.j4fluxer.entities.OnlineStatus;
import com.j4fluxer.entities.member.Member;
import com.j4fluxer.events.message.GuildMessageReceivedEvent;
import com.j4fluxer.events.session.ReadyEvent;
import com.j4fluxer.hooks.ListenerAdapter;
import dev.dannytaylor.brick.common.BrickBot;
import dev.dannytaylor.brick.config.BrickConfig;
import dev.dannytaylor.brick.logger.BrickLoggerImpl;

import java.util.List;

public class BrickFluxerListener extends ListenerAdapter {
    @Override
    public void onReady(ReadyEvent event) {
        BrickLoggerImpl.info("Fluxer Ready!");
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String message = event.getMessage().getContent();
        if (message.startsWith(BrickConfig.instance.fluxerSettings.commandPrefix.value())) {
            Member member = event.getMember();
            String username = member.getUser().getUsername() + "#" + member.getUser().getDiscriminator();
            if (hasRole(member.getRoleIds())) {
                parseCommand(event, BrickConfig.instance.fluxerSettings.announcementCommand.value(), getCommand(message), username, BrickBot.MessageType.ANNOUNCEMENT);
                parseCommand(event, BrickConfig.instance.fluxerSettings.contentCommand.value(), getCommand(message), username, BrickBot.MessageType.CONTENT);
                parseCommand(event, BrickConfig.instance.fluxerSettings.scheduleCommand.value(), getCommand(message), username, BrickBot.MessageType.SCHEDULE);
                parseCommand(event, BrickConfig.instance.fluxerSettings.modTesterCommand.value(), getCommand(message), username, BrickBot.MessageType.MOD_TESTER);
            }
        }
    }

    public static void parseCommand(GuildMessageReceivedEvent event, String command, String[] args, String username, BrickBot.MessageType type) {
        if (args[0].substring(BrickConfig.instance.fluxerSettings.commandPrefix.value().length()).equalsIgnoreCase(command)) {
            if (args.length > 1) {
                StringBuilder message = new StringBuilder();
                for (int i = 1; i < args.length; i++) message.append(args[i]).append(" ");
                BrickBot.send(message.toString(), username, type, BrickBot.Platform.FLUXER);
            } else {
                BrickLoggerImpl.warn(command + " command failed due to lack of message.");
                event.getMessage().reply("You must include an announcement!").queue();
            }
        }
    }

    public static String[] getCommand(String message) {
        return message.trim().split("\\s+");
    }

    public static boolean hasRole(List<String> memberRoles) {
        for (String roleId : memberRoles) if (BrickConfig.instance.fluxerSettings.commandRoleId.value().contains(roleId)) return true;
        return false;
    }
}