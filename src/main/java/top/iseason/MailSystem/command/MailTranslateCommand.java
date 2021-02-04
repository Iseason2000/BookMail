package top.iseason.MailSystem.command;

import org.apache.commons.lang.NullArgumentException;
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
        Player player = (Player)sender;
        ItemStack handItem = player.getInventory().getItemInMainHand();
        String itemName = handItem.getType().name();
        if (!itemName.equals("WRITTEN_BOOK") && !itemName.equals("BOOK_AND_QUILL"))
            throw new NullArgumentException("物品必须为成书或书与笔!");
        BookTranslator book = new BookTranslator(handItem);
        ItemStack newBook = book.Build();
        if(newBook == null){
            player.sendMessage(ChatColor.RED+"书必须有内容！");
            return true;
        }
        player.getInventory().addItem(newBook);
        return true;
    }
}
