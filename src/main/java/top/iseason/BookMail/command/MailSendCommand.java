package top.iseason.BookMail.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.iseason.BookMail.myclass.Mail;
import top.iseason.BookMail.Manager.MailManager;
import top.iseason.BookMail.Util.BookTranslator;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.SimpleSubCommand;

import java.util.Calendar;
import java.util.List;

public class MailSendCommand extends SimpleSubCommand {
    MailSendCommand(String command) {
        super(command);
        setUsage("send [玩家]");
        setDescription("将手上的成书转为邮件并发送");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用这个命令");
            return;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            Message.send(player, ChatColor.YELLOW + "请填写需要发送的人！");
            Message.send(player, ChatColor.GREEN + "/bookmail send [玩家]");
            return;
        }
        if (!player.isOp() && args.length > 1) return;
        ItemStack bookItem = player.getInventory().getItemInMainHand();
        if (bookItem.getType() != Material.WRITTEN_BOOK) {
            Message.send(player, ChatColor.YELLOW + "你需要拿着成书才能发送邮件！");
            return;
        }
        BookTranslator book = new BookTranslator(bookItem, false);
        book.TranslateContent();
        String content = book.getZipString();
        Mail mail = new Mail(0, book.getTitle(), content, book.getCDK(), book.getAuthor());
        List<String> failureList = MailManager.sendMailtoPlayers(mail, args);
        if (failureList.size() > 0) {
            StringBuilder failureNames = new StringBuilder();
            for (String name : failureList) {
                failureNames.append(" ").append(name);
            }
            Message.send(player, ChatColor.YELLOW + "邮件已发送，但有 " + ChatColor.RED + failureList.size() + ChatColor.YELLOW + " 人发送失败!" + ChatColor.LIGHT_PURPLE + "他们分别是:");
            Message.send(player, ChatColor.YELLOW + failureNames.toString());
        } else {
            Message.send(player, ChatColor.GREEN + "邮件全部发送成功!");
        }
        if (!player.isOp()) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getAmount() == 1) {
                int slot = player.getInventory().getHeldItemSlot();
                player.getInventory().setItem(slot, null);
            }
            item.setAmount(item.getAmount() - 1);

        }
    }
}
