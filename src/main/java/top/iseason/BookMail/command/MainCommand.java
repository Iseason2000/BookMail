package top.iseason.BookMail.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.iseason.BookMail.Util.SimpleCommand;

public class MainCommand extends SimpleCommand implements CommandExecutor {
    public MainCommand() {
        registerSubCommands();
    }

    @Override
    public void registerSubCommands() {
        registerSubCommand(new OpenMailCommand("open"));
        registerSubCommand(new PackageCommand("package"));
        registerSubCommand(new MailTranslateCommand("translate"));
        registerSubCommand(new MailSendCommand("send"));
        registerSubCommand(new MailSendGroupCommand("sendGroup"));
        registerSubCommand(new MailSendOnTimeCommand("sendOnTime"));
        registerSubCommand(new MailBoxCommand("mailboxItem"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        sendHelpMessage(sender, args);
        return true;
    }
}
