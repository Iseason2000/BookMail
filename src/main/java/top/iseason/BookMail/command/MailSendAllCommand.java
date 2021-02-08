package top.iseason.BookMail.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Manager.MailManager;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.SimpleSubCommand;
import top.iseason.BookMail.Util.Tools;
import top.iseason.BookMail.myclass.Mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class MailSendAllCommand extends SimpleSubCommand {
    MailSendAllCommand(String command) {
        super(command);
        setUsage("sendAll ");
        addSubCommand("online");
        addSubCommand("offline");
        addSubCommand("registered");
        addSubCommand("new");
        addSubCommand("loginTime");
        setDescription("打开批量发送帮助信息");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用这个命令");
            return;
        }
        Player player = (Player) sender;
        if (!player.isOp()) {
            Message.send(player, ChatColor.RED + "你没有此命令的使用权限！");
        }
        if (args.length == 0) {
            showHelp(player);
            return;
        }
        switch (args[0]) {
            case "online":
                sendOnlineCommand(player);
                break;
            case "offline":
                sendOfflineCommand(player);
                break;
            case "registered":
                sendAllCommand(player);
                break;
            case "new":
                sendNewCommand(player);
                break;
            case "loginTime":
                if (args.length == 1){
                    List<String> helpMessage = new ArrayList<>();
                    helpMessage.add("&6&m+--------+&9&l " + BookMailPlugin.getInstance().getName() + "&e -> &a&lSendAll &e-> &e&lLoginTime&6&m+--------+");
                    helpMessage.add("&a支持的参数： &fx&6s&fx&6m&fx&6h&fx&6d&f (&fx&a为整数&f) &dyyyy-MM-dd-HH:mm:ss,&e例子:");
                    helpMessage.add( "--&61d2h &b表示&61&b天又&62&b小时之内登录过的玩家");
                    helpMessage.add( "--&62021-02-08-22:09:04 表示&a这个时间点之后&b登录过的玩家");
                    helpMessage.add("--&62021-02-08-22:09:04 &62021-02-08-22:09:04");
                    helpMessage.add( "-&b表示&a两个时间段之间&b登录过的玩家");
                    Message.send(player,helpMessage);
                }
                break;
            default:
                showHelp(player);
        }
    }

    public static void sendOnlineCommand(Player player) {
        Mail mail = MailManager.getMailInHand(player);
        if (mail == null) return;
        mail.type = "online";
        new BukkitRunnable() {
            @Override
            public void run() {
                groupSend(player, mail, getOnlinePlayer());
            }
        }.runTaskAsynchronously(BookMailPlugin.getInstance());
    }

    public static void sendOfflineCommand(Player player) {
        Mail mail = MailManager.getMailInHand(player);
        if (mail == null) return;
        mail.type = "offline";
        new BukkitRunnable() {
            @Override
            public void run() {
                groupSend(player, mail, getOfflinePlayer());
            }
        }.runTaskAsynchronously(BookMailPlugin.getInstance());
    }

    public static void sendAllCommand(Player player) {
        Mail mail = MailManager.getMailInHand(player);
        if (mail == null) return;
        mail.type = "all";
        new BukkitRunnable() {
            @Override
            public void run() {
                groupSend(player, mail, getAllPlayer());
            }
        }.runTaskAsynchronously(BookMailPlugin.getInstance());
    }

    public static void sendNewCommand(Player player) {
        Mail mail = MailManager.getMailInHand(player);
        if (mail == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                mail.type = "new";
                mail.groupID = Tools.getCDK(5);
                if (!MailManager.sendSystemMail(mail)) {
                    Message.send(player, ChatColor.RED + "发送失败！数据库异常。");
                    return;
                }
                Message.send(player, ChatColor.GREEN + "已添加新玩家欢迎邮件！");
            }
        }.runTaskAsynchronously(BookMailPlugin.getInstance());
    }

    private static void showHelp(Player player) {
        List<String> helpMessage = new ArrayList<>();
        helpMessage.add("&6&m+-------------+&9&l " + BookMailPlugin.getInstance().getName() + "&e - &a&lSendAll &6&m+-------------+");
        helpMessage.add(" ");
        helpMessage.add("&d/BookMail sendAll" + " &6online      " + "&e 向所有&6在线&e玩家的发送邮件");
        helpMessage.add("&d/BookMail sendAll" + " &6offline     " + "&e 向所有&9离线&e玩家的发送邮件");
        helpMessage.add("&d/BookMail sendAll" + " &6registered" + "&e 向所有&a注册&e的玩家发送邮件");
        helpMessage.add("&d/BookMail sendAll" + " &6new         " + "&e 向所有&b新注册&e的玩家发送邮件");
        helpMessage.add("&d/BookMail sendAll" + " &6loginTime   " + "&e 向指定&b最后登录时间&e的玩家发送邮件");
        helpMessage.add(" ");
        Message.send(player, helpMessage);
    }

    public static void groupSend(Player sender, Mail mail, String[] players) {
        if (mail == null) return;
        mail.groupID = Tools.getCDK(5);
        if (!MailManager.sendSystemMail(mail)) {
            Message.send(sender, ChatColor.RED + "发送失败！数据库异常。");
            return;
        }
        Mail lightMail = new Mail();
        lightMail.groupID = mail.groupID;
        lightMail.isAccept = false;
        lightMail.isRead = false;
        lightMail.sender = mail.sender;
        lightMail.time = mail.time;
        MailSendCommand.sendMail(sender, lightMail, players);
    }

    public static String[] getAllPlayer() {
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        int count = offlinePlayers.length;
        String[] players = new String[count];
        for (int n = 0; n < count; n++) {
            players[n] = offlinePlayers[n].getName();
        }
        return players;
    }

    public static String[] getOfflinePlayer() {
        String[] allPlayer = getAllPlayer();
        String[] onlinePlayer = getOnlinePlayer();
        ArrayList<String> allPlayerList = new ArrayList<>(Arrays.asList(allPlayer));
        ArrayList<String> onlineList = new ArrayList<>(Arrays.asList(onlinePlayer));
        allPlayerList.removeAll(onlineList);
        return allPlayerList.toArray(new String[0]);
    }

    public static String[] getOnlinePlayer() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int count = onlinePlayers.size();
        String[] players = new String[count];
        int n = 0;
        for (Player onlinePlayer : onlinePlayers) {
            players[n++] = onlinePlayer.getName();
        }
        return players;
    }
}
