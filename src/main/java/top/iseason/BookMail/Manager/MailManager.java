package top.iseason.BookMail.Manager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.iseason.BookMail.Util.BookTranslator;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.myclass.Mail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MailManager {
    //发送邮件给n个人，返回发送失败的玩家名字(不存在表或者数据库操作异常)
    public static List<String> sendMailtoPlayers(Mail mail, String[] playerNames) {
        List<String> noPlayerMailList = new ArrayList<>();
        for (String name : playerNames) {
            try {
                if (!SqlManager.addPlayerMail(name, mail)) {
                    noPlayerMailList.add(name);
                }
            } catch (SQLException throwables) {
                noPlayerMailList.add(name);
                throwables.printStackTrace();
            }
        }
        return noPlayerMailList;
    }

    public static Boolean sendSystemMail(Mail mail) {
        try {
            SqlManager.addSystemMail(mail);
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    //    public static Mail getSystemMail(){
//
//    }
    public static Mail getMailInHand(Player player) {
        ItemStack bookItem = player.getInventory().getItemInMainHand();
        if (bookItem.getType() != Material.WRITTEN_BOOK) {
            Message.send(player, ChatColor.YELLOW + "你需要拿着成书才能发送邮件！");
            return null;
        }
        BookTranslator book = new BookTranslator(bookItem, false);
        book.TranslateContent();
        String content = book.getZipString();
        return new Mail("", book.getTitle(), content, book.getCDK(), book.getAuthor());
    }
}
