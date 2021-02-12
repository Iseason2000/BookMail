package top.iseason.BookMail.Manager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.iseason.BookMail.Util.ItemTranslator;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.myclass.BookTranslator;
import top.iseason.BookMail.myclass.Mail;
import top.iseason.BookMail.myclass.Task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public static Boolean sendMailtoPlayer(Mail mail, String playerName) {
        try {
            SqlManager.addPlayerMail(playerName, mail);
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
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

    public static ItemStack getPlayerMailBox(String playerName) {
        try {
            ArrayList<Mail> playerMails = SqlManager.getPlayerMails(playerName);
            ArrayList<String> mailStringList = new ArrayList<>();
            for (Mail mail : playerMails) {
                String theme = mail.theme;
                String attach = "";
                if (mail.attached.length() != 0) attach = "§e有附件✉\\\\n";
                String titleInfo = mail.theme + "\\\\n" + attach + "§b发件人: §6" + mail.sender + "\\\\n" + "§b时间: §a" + mail.time;
                int noColorLength = ChatColor.stripColor(theme).length();
                if (noColorLength > 9) {
                    int extraWordCount = noColorLength - 9;
                    theme = theme.substring(0, theme.length() - extraWordCount).concat("...");
                }
                String mailTitle = "§0《".concat(theme).concat("§0》§r");
                if (!mail.isRead) mailTitle = "§6❀".concat(mailTitle);
                String mailPart1 = "{\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\""
                        + titleInfo + "\"}},\"text\":\"" + mailTitle + "\\\\n\"},";
                String mailPart2 = "{\"text\":\"§7---------\"},";
                String mailPart3 = "{\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/bookmail remove " + mail.ID + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"§c删除该邮件\"}},\"text\":\"§4[删除] \"},";
                String mailPart4 = "{\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/bookmail open " + mail.ID + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"§a阅读该邮件\"}},\"text\":\"§2[打开]\\\\n\\\\n§r\"}";
                String mailString = mailPart1 + mailPart2 + mailPart3 + mailPart4;
                mailStringList.add(mailString);
            }
            return getMailBoxItemStack(mailStringList);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static ItemStack getSystemMailBox() {
        try {
            ArrayList<Mail> systemMails = SqlManager.getSystemMails();
            ArrayList<String> mailStringList = new ArrayList<>();
            for (Mail mail : systemMails) {
                String theme = mail.theme;
                String titleInfo = mail.theme + "\\\\n§b类型: §c" + mail.type + "\\\\n§b发件人: §6"
                        + mail.sender + "\\\\n§b附件: §5" + mail.attached + "\\\\n§b阅读数: §e" + mail.readCount
                        + " §b领取数: §e" + mail.acceptCount
                        + "\\\\n§b时间: §a" + mail.time;
                int noColorLength = ChatColor.stripColor(theme).length();
                if (noColorLength > 10) {
                    int extraWordCount = noColorLength - 10;
                    theme = theme.substring(0, theme.length() - extraWordCount).concat("...");
                }
                String mailTitle = "§0《".concat(theme).concat("§0》§r");
                String mailPart1 = "{\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\""
                        + titleInfo + "\"}},\"text\":\"" + mailTitle + "\\\\n\"},";
                String mailPart2 = "{\"text\":\"§7---------\"},";
                String mailPart3 = "{\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/bookmail remove " + mail.ID + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"§c删除该邮件\"}},\"text\":\"§4[删除] \"},";
                String mailPart4 = "{\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/bookmail open " + mail.ID + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"§a阅读该邮件\"}},\"text\":\"§2[打开]\\\\n\\\\n§r\"}";
                String mailString = mailPart1 + mailPart2 + mailPart3 + mailPart4;
                mailStringList.add(mailString);
            }
            return getMailBoxItemStack(mailStringList);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public static ItemStack getOnTimeTask() {
        try {
            List<Task> taskList = SqlManager.getTaskList();
            if (taskList.isEmpty()) return null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ArrayList<String> taskStringList = new ArrayList<>();
            String theme;
            for (Task task : taskList) {
                theme = SqlManager.getSystemRecord("SystemMail", task.groupID, "主题");
                if (theme == null)
                    theme = "";
                int noColorLength = ChatColor.stripColor(theme).length();
                if (noColorLength > 10) {
                    int extraWordCount = noColorLength - 10;
                    theme = theme.substring(0, theme.length() - extraWordCount).concat("...");
                }
                LocalDateTime nextTime = TimeManager.getNextTime(task.type, task.sendTime, task.addTime);
                if (nextTime == null) continue;
                String nextString = nextTime.format(formatter);
                String titleInfo = theme + "\\\\n§b类型: §c" + task.type + "\\\\n§b群发参数: §6"
                        + task.groupArg + "\\\\n§b发送规则: §5" + task.sendTime + "\\\\n§b添加时间: §e" + task.addTime
                        + "\\\\n§3下次时间: §6" + nextString;
                String mailTitle = "§0《".concat(theme).concat("§0》§r");
                String mailPart1 = "{\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\""
                        + titleInfo + "\"}},\"text\":\"" + mailTitle + "\\\\n\"},";
                String mailPart2 = "{\"text\":\"§7--------------\"},";
                String mailPart3 = "{\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/bookmail open "
                        + task.groupID + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"§e点击取消任务\"}},\"text\":\"§4[取消]\\\\n\\\\n§r\"}";
                String mailString = mailPart1 + mailPart2 + mailPart3;
                taskStringList.add(mailString);
            }
            return getMailBoxItemStack(taskStringList);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    private static ItemStack getMailBoxItemStack(ArrayList<String> mailStringList) {
        StringBuilder pageContent = new StringBuilder();
        StringBuilder allContent = new StringBuilder();
        int mailCount = 0;
        int size = mailStringList.size();
        for (String mailString : mailStringList) {
            mailCount++;
            if (mailCount % 5 == 0 || mailCount == size) {
                pageContent.append(mailString);
                if (mailCount == size)
                    allContent.append("'{\"extra\":[").append(pageContent).append("],\"text\":\"\"}'");
                else
                    allContent.append("'{\"extra\":[").append(pageContent).append("],\"text\":\"\"}',");
                pageContent = new StringBuilder();
            } else {
                pageContent.append(mailString).append(",");
            }
        }
        String nbtString = "{id:\"minecraft:written_book\",tag:{pages:[" + allContent.toString() + "],resolved:1b,author:\"BookMail\",title:\"BookMailBox\"},Count:1b}";
        return ItemTranslator.nbtStringToItem(nbtString);
    }
}
