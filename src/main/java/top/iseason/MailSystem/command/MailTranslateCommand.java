package top.iseason.MailSystem.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.iseason.MailSystem.Util.BookTranslator;

public class MailTranslateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用这个命令");
            return true;
        }
        Player player = (Player) sender;
        ItemStack handItem = player.getInventory().getItemInMainHand();
        String itemName = handItem.getType().name();
        if (!itemName.equals("WRITTEN_BOOK") && !itemName.equals("BOOK_AND_QUILL")) {
            player.sendMessage(ChatColor.RED+"请先主手拿着成书或书与笔!");
            return true;
        }

        BookTranslator book = new BookTranslator(handItem);
        book.playerTranslate();
        ItemStack newBook = book.Build();
        if (newBook == null) {
            player.sendMessage(ChatColor.RED + "书必须有内容！");
            return true;
        }
        OpenMailCommand.openBook(newBook, player);
        return true;
    }
}
