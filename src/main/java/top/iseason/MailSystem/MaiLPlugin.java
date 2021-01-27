package top.iseason.MailSystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import top.iseason.MailSystem.Manager.DataManager;
import top.iseason.MailSystem.Util.MailData;

import java.sql.SQLException;

import static top.iseason.MailSystem.Util.LogSender.sendLog;

public class MaiLPlugin extends JavaPlugin implements Listener {
    private static MaiLPlugin plugin;

    public static MaiLPlugin getInstance() {
        return plugin;
    }

    public void onEnable() { //启用插件
        plugin = this;
        sendLog(ChatColor.AQUA + "███╗   ███╗ █████╗ ██╗██╗     ");
        sendLog(ChatColor.AQUA + "████╗ ████║██╔══██╗██║██║     ");
        sendLog(ChatColor.AQUA + "██╔████╔██║███████║██║██║     ");
        sendLog(ChatColor.AQUA + "██║╚██╔╝██║██╔══██║██║██║     ");
        sendLog(ChatColor.AQUA + "██║ ╚═╝ ██║██║  ██║██║███████╗");
        sendLog(ChatColor.AQUA + "╚═╝     ╚═╝╚═╝  ╚═╝╚═╝╚══════╝");
        saveDefaultConfig();
        try {
            DataManager.initSqilte();
            sendLog(ChatColor.GREEN + "数据库连接成功!");
            DataManager.createPlayerMailBoxTable("no1127");
        } catch (ClassNotFoundException | SQLException e) {
            sendLog(ChatColor.RED + "数据库连接失败!");
            e.printStackTrace();
        }
        MailData newMail = new MailData(0,"测试","测试内容","测试物品","Iseason");
        try {
            DataManager.addPlayerMail("no1127",newMail);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
//
//
//        steelSafesListFile = new File(getInstance().getDataFolder(), "steelSafes.yml");
//        steelSafesOwner = YamlConfiguration.loadConfiguration(steelSafesListFile);
//        ownerListFile = new File(getInstance().getDataFolder(), "owners.yml");
//        ownerList = YamlConfiguration.loadConfiguration(ownerListFile);
//        Bukkit.getPluginManager().registerEvents(new BreakingProtection(), this);//这里类是监听器, 将当前对象注册监听器
//        Bukkit.getPluginManager().registerEvents(new OpenCheck(), this);
//        Bukkit.getPluginManager().registerEvents(new MoveItemEvent(), this);
//        Bukkit.getPluginCommand("steelsafe").setExecutor(new CreateCommand());
//        Bukkit.getPluginCommand("steelsafereremove").setExecutor(new RemoveCommand());
//        Bukkit.getPluginCommand("steelsafekey").setExecutor(new OpenWithKey());
//        Bukkit.getPluginCommand("steelsafereload").setExecutor(new steelsafereload());
//        Bukkit.getPluginCommand("steelsafeshowkey").setExecutor(new ShowMyChest());

    }

    public void onDisable() {
        try {
            DataManager.disableSqilte();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


}