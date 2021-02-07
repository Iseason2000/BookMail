package top.iseason.BookMail.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.iseason.BookMail.Manager.SqlManager;
import top.iseason.BookMail.Util.Message;

import java.sql.SQLException;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        try {
            String name = event.getPlayer().getName();
            if(SqlManager.createPlayerMailBoxTable(name)){
                Message.sendLog(ChatColor.GOLD +"已为"+ChatColor.GREEN+name+ChatColor.GOLD+"创建邮箱!");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
