package top.iseason.BookMail.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Manager.SqlManager;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.Tools;

import java.sql.SQLException;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        String name = event.getPlayer().getName();
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (SqlManager.createPlayerMailBoxTable(name)) {
                        Message.sendLog(ChatColor.GOLD + "已为" + ChatColor.GREEN + name + ChatColor.GOLD + "创建邮箱!");
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
