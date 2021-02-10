package top.iseason.BookMail.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.myclass.BookTranslator;
import top.iseason.BookMail.Util.SimpleSubCommand;

public class MailTranslateCommand extends SimpleSubCommand {
    MailTranslateCommand(String command) {
        super(command);
        setUsage("translate");
        setDescription("将手上的书转为邮件并预览");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用这个命令");
            return;
        }
        Player player = (Player) sender;
        ItemStack handItem = player.getInventory().getItemInMainHand().clone();
        new BukkitRunnable() {
            @Override
            public void run() {
                String itemName = handItem.getType().name();
                if (!itemName.equals("WRITTEN_BOOK") && !itemName.equals("BOOK_AND_QUILL")) {
                    player.sendMessage(ChatColor.RED + "请先主手拿着成书或书与笔!");
                    return;
                }
                BookTranslator book = new BookTranslator(handItem, true);
                book.TranslateContent();
                ItemStack newBook = book.Build();
                if (newBook == null) {
                    player.sendMessage(ChatColor.RED + "书必须有内容！");
                    return;
                }
                OpenMailCommand.openBook(newBook, player);
            }
        }.runTaskAsynchronously(BookMailPlugin.getInstance());
    }
}
