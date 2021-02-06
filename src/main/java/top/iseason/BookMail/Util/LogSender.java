package top.iseason.BookMail.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class LogSender {
    public static void sendLog(String message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW+"Mail"+ChatColor.GOLD+"System "+ChatColor.GREEN+"| "+ChatColor.RESET+message);
    }
}
