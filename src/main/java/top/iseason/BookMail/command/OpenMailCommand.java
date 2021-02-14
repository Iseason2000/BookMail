package top.iseason.BookMail.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.Manager.MailManager;
import top.iseason.BookMail.Manager.SqlManager;
import top.iseason.BookMail.Util.ItemTranslator;
import top.iseason.BookMail.Util.SimpleSubCommand;
import top.iseason.BookMail.myclass.Mail;

import java.sql.SQLException;

public class OpenMailCommand extends SimpleSubCommand {
    OpenMailCommand(String command) {
        super(command);
        setUsage("open [page]");
        setDescription("打开自己的邮箱");
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
                    try {
                        Mail playerMail = SqlManager.getPlayerMail(player.getName(), id);
                        SqlManager.setPlayerMailIsRead(player.getName(), id);
                        if (playerMail == null) return;
                        String content = playerMail.content;
                        player.openBook(ItemTranslator.zipStringToItem(content));
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }.run();
            return;
        }
        openMailBox(player);
    }

    public static void openMailBox(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack bookItem = MailManager.getPlayerMailBox(player.getName());
                if(bookItem!=null)
                player.openBook(bookItem);
            }
        }.run();
    }



}


