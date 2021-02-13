package top.iseason.BookMail.Util;

import org.bukkit.command.CommandSender;
import top.iseason.BookMail.BookMailPlugin;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleSubCommand {

    String command;
    String usage;
    String description;
    String permission;
    List<String> subCommand;

    public SimpleSubCommand(String subCommand) {
        this.command = subCommand;
        this.permission = BookMailPlugin.getInstance().getName() + "." + subCommand;
    }

    public abstract void onCommand(CommandSender sender, String[] args);

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public void addSubCommand(String subCommand) {
        if (this.subCommand == null) this.subCommand = new ArrayList<>();
        this.subCommand.add(subCommand);
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public void setPermission(String permission) {
//        this.permission = permission;
//    }

}
