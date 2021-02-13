package top.iseason.BookMail.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Manager.MailManager;
import top.iseason.BookMail.Manager.SqlManager;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.Tools;
import top.iseason.BookMail.command.OpenMailCommand;
import top.iseason.BookMail.myclass.Mail;

import java.sql.SQLException;
import java.util.ArrayList;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        String name = event.getPlayer().getName();
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (SqlManager.createPlayerMailBoxTable(name)) {
                        Message.sendLog(ChatColor.GOLD + "已为新用户" + ChatColor.GREEN + name + ChatColor.GOLD + "创建邮箱!");
                        ArrayList<Mail> newMails = SqlManager.getSystemMails("new");
                        if (newMails.isEmpty()) return;
                        for (Mail mail : newMails) {
                            MailManager.sendMailtoPlayer(mail, name);
                        }
                        ItemStack mailBoxItem = BookMailPlugin.getConfigManager().getOpenMailItem();
                        if (mailBoxItem == null) return;
                        event.getPlayer().getInventory().addItem(mailBoxItem);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                try {
                    SqlManager.updatePlayerLoginTime(name, Tools.getDataAndTime());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                try {
                    int unRead = SqlManager.getRecordValueCount(1, name, "已阅读", 0);
                    if (unRead > 0) {
                        Message.send(event.getPlayer(), "&e你有 &c" + unRead + "&e 封 未读邮件! 输入:&b/bookmail open 查看");
                    } else {
                        Message.send(event.getPlayer(), "&a没有未读邮件!");
                    }
                } catch (SQLException ignored) {
                }
            }
        }.runTaskAsynchronously(BookMailPlugin.getInstance());
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (!item.equals(BookMailPlugin.getConfigManager().getOpenMailItem())) return;
        OpenMailCommand.openMailBox(event.getPlayer());

    }
}
