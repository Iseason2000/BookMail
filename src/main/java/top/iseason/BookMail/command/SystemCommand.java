package top.iseason.BookMail.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Manager.MailManager;
import top.iseason.BookMail.Manager.SqlManager;
import top.iseason.BookMail.Manager.TimeManager;
import top.iseason.BookMail.Util.ItemTranslator;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.SimpleSubCommand;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SystemCommand extends SimpleSubCommand {
    SystemCommand(String command) {
        super(command);
        setUsage("system");
        addSubCommand("setMailBox");
        addSubCommand("giveMailBox");
        addSubCommand("systemMail");
        addSubCommand("taskList");
        addSubCommand("packageList");
        addSubCommand("reload");
        addSubCommand("createMailboxes");
        setDescription("查看系统功能 [OP]");
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
            case "giveMailBox":
                if (args.length != 2) return;
                giveMailBox(player, args);
                break;
            case "setMailBox":
                if (args.length != 1) return;
                setMailBox(player);
                break;
            case "systemMail":
                openSystemMailbox(player, args);
                break;
            case "taskList":
                openTaskList(player, args);
                break;
            case "packageList":
                ItemStack packageList = MailManager.getPackageList(null);
                if (packageList == null) {
                    Message.send(player, "&c没有任何包裹!");
                    return;
                }
                player.openBook(packageList);
                break;
            case "createMailboxes":
                if (args.length != 2) return;
                createMailBoxes(player, args);
                break;
            case "reload":
                BookMailPlugin.getConfigManager().reload();
                Message.send(player, "&a配置已重载!");
                break;
            default:
                showHelp(player);
                break;
        }


    }

    private static void giveMailBox(Player player, String[] args) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) return;
                ItemStack mailBoxItem = BookMailPlugin.getConfigManager().getOpenMailItem();
                if (mailBoxItem == null) return;
                p.getInventory().addItem(mailBoxItem);
                Message.send(player, ChatColor.GREEN + "操作成功!");
            }
        }.run();
    }

    private static void setMailBox(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem.getType() == Material.AIR) return;
                BookMailPlugin.getConfigManager().setOpenMailItem(handItem);
                Message.send(player, ChatColor.GREEN + "操作成功!");
            }
        }.run();
    }

    private static void openSystemMailbox(Player player, String[] args) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (args.length == 1) {
                    ItemStack systemMailBox = MailManager.getSystemMailBox();
                    if (systemMailBox == null) return;
                    player.openBook(systemMailBox);
                    return;
                }
                if (args.length == 3) {
                    if (args[1].equals("open")) {
                        try {
                            ItemStack item = ItemTranslator.zipStringToItem(SqlManager.getSystemMailContent(args[2]));
                            player.openBook(item);
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        return;
                    }
                    if (args[1].equals("remove")) {
                        try {
                            if (SqlManager.removeSystemMail(args[2])) {
                                Message.send(player, "&a 删除成功!");
                                return;
                            }
                            Message.send(player, "&c 删除失败，系统邮件不存在!");
                        } catch (SQLException throwables) {
                            Message.send(player, "&c 删除失败，数据库异常!");
                        }
                    }
                }
            }
        }.run();
    }

    private static void openTaskList(Player player, String[] args) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (args.length == 1) {
                    ItemStack taskList = MailManager.getOnTimeTask();
                    if (taskList == null) {
                        Message.send(player, "&e 没有任何定时任务!");
                        return;
                    }
                    player.openBook(taskList);
                    return;
                }
                if (args.length == 3) {
                    if (args[1].equals("remove")) {
                        if (TimeManager.removeTask(args[2])) {
                            Message.send(player, "&a 删除成功!");
                            return;
                        }
                        Message.send(player, "&c 删除失败，任务不存在或数据库异常!");
                    }
                }
            }
        }.run();
    }

    private static void createMailBoxes(Player player, String[] args) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String[] players;
                switch (args[1]) {
                    case "online":
                        players = MailSendGroupCommand.getOnlinePlayer();
                        break;
                    case "offline":
                        players = MailSendGroupCommand.getOfflinePlayer();
                        break;
                    case "all":
                        players = MailSendGroupCommand.getAllPlayer();
                        break;
                    default:
                        Message.send(player, "&c参数错误!");
                        return;
                }
                List<String> success = new ArrayList<>();
                for (String player : players) {
                    try {
                        if (SqlManager.createPlayerMailBoxTable(player)) success.add(player);
                    } catch (SQLException ignored) {
                    }
                }
                Message.send(player, "&a已为以下玩家创建邮箱:&8");
                Message.send(player, "&8" + success.toString());
            }
        }.runTaskAsynchronously(BookMailPlugin.getInstance());
    }

    private static void showHelp(Player player) {
        List<String> helpMessage = new ArrayList<>();
        helpMessage.add("&6&m+-------------+&9&l " + BookMailPlugin.getInstance().getName() + "&e - &a&lSystem &6&m+-------------+");
        helpMessage.add(" ");
        helpMessage.add("&b/BookMail &dsystem &6setMailBox&e    将手上的物品设置为邮箱物品(右键空气)");
        helpMessage.add("&b/BookMail &dsystem &6giveMailBox&e   给予某个玩家邮箱物品");
        helpMessage.add("&b/BookMail &dsystem &6systemMail&e    打开&9系统邮箱&6管理界面");
        helpMessage.add("&b/BookMail &dsystem &6taskList&e       打开&c定时任务&6管理界面");
        helpMessage.add("&b/BookMail &dsystem &6packageList&e  打开&d包裹&6管理界面");
        helpMessage.add("&b/BookMail &dsystem &6reload&e  &d重载配置");
        helpMessage.add("&b/BookMail &dsystem &6createMailboxes [online/offline/all]");
        helpMessage.add("                                  &e为指定玩家组创建邮箱[在线/离线/全部]");
        helpMessage.add(" ");
        Message.send(player, helpMessage);
    }
}
