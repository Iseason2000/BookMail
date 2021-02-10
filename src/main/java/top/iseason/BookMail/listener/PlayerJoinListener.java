package top.iseason.BookMail.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Manager.MailManager;
import top.iseason.BookMail.Manager.SqlManager;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.Tools;
import top.iseason.BookMail.myclass.Mail;

import java.sql.SQLException;
import java.util.ArrayList;

public class PlayerJoinListener implements Listener {
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
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                try {
                    SqlManager.updatePlayerLoginTime(name, Tools.getDataAndTime());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }.runTaskAsynchronously(BookMailPlugin.getInstance());
    }
}
