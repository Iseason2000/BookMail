package top.iseason.BookMail.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import top.iseason.BookMail.BookMailPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SimpleCommand implements TabCompleter {

    Set<SimpleSubCommand> subCommands = new HashSet<>();

    public void registerSubCommand(SimpleSubCommand subCommand) {
        subCommands.add(subCommand);
    }

    public abstract void registerSubCommands();

    public void sendHelpMessage(CommandSender sender, String[] args) {
        if (args.length > 0) {
            for (SimpleSubCommand subCommand : subCommands) {
                if (subCommand.command.equalsIgnoreCase(args[0])) {
                    int length = args.length;
                    String[] subArgs = new String[length - 1];
                    System.arraycopy(args, 1, subArgs, 0, length - 1);
                    subCommand.onCommand(sender, subArgs);
                    return;
                }
            }
        }
        Message.send(sender, getHelpMessage(sender));
    }

    private List<String> getHelpMessage(CommandSender sender) {

        BookMailPlugin plugin = BookMailPlugin.getInstance();

        List<String> helpMessage = new ArrayList<>();

        helpMessage.add("&6&m+------------------+&9&l " + plugin.getName() + " &6&m+------------------+");
        helpMessage.add("");

        for (SimpleSubCommand subCommand : subCommands) {
            helpMessage.add("&b/BookMail" + " &6" + subCommand.usage + "&e " + subCommand.description);
        }
        helpMessage.add("");

        return helpMessage;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> tabComplete = new ArrayList<>();
            for (SimpleSubCommand subCommand : subCommands) {
                tabComplete.add(subCommand.command);
            }
            tabComplete.removeIf(s -> !s.startsWith(args[0]));
            return tabComplete;
        }
        if (args.length == 2) {
            List<String> tabComplete = new ArrayList<>();
            for (SimpleSubCommand subCommand : subCommands) {
                if (subCommand.command.equals(args[0])) {
                    if (subCommand.subCommand != null) {
                        tabComplete.addAll(subCommand.subCommand);
                    }
                }
            }
            tabComplete.removeIf(s -> !s.startsWith(args[1]));
            return tabComplete;
        }
        return null;
    }
}
