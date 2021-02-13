package top.iseason.BookMail.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.Manager.MailManager;
import top.iseason.BookMail.Manager.SqlManager;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.SimpleSubCommand;
import top.iseason.BookMail.myclass.Mail;

import java.sql.SQLException;

public class RemoveMailCommand extends SimpleSubCommand {
    RemoveMailCommand(String command) {
        super(command);
        setUsage("remove [id]");
        setDescription("删除某封邮件");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用这个命令");
            return;
        }
        Player player = (Player) sender;
        if (args.length == 1) {
            int id;
            try {
                id = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    Mail playerMail;
                    try {
                        playerMail = SqlManager.getPlayerMail(player.getName(), id);
                    } catch (SQLException throwables) {
                        Message.send(player, "&c你没有这个邮件!");
                        return;
                    }
                    if (playerMail == null) {
                        Message.send(player, "&c你没有这个邮件!");
                        return;
                    }
                    if (!playerMail.isRead) {
                        Message.send(player, "&e你还没有阅读这封邮件，请先阅读!");
                        return;
                    }
                    if (!playerMail.attached.equals("") && !playerMail.isAccept) {
                        Message.send(player, "&e你还没有领取这封邮件，请先领取!");
                        return;
                    }
                    if (MailManager.removePlayerMail(player.getName(), id)) {
                        Message.send(player, "&a删除成功!");
                    } else Message.send(player, "&c删除失败!");
                }
            }.run();
        }
    }
}
