package top.iseason.BookMail.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Manager.MailManager;
import top.iseason.BookMail.Manager.PackageManager;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.SimpleSubCommand;
import top.iseason.BookMail.myclass.Package;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class PackageCommand extends SimpleSubCommand {

    public PackageCommand(String command) {
        super(command);
        setUsage("package ");
        addSubCommand("create");
        addSubCommand("edit");
        addSubCommand("build");
        addSubCommand("list");
        addSubCommand("remove");
        setDescription("列出包裹命令说明。");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;
        if (!BookMailPlugin.getConfigManager().isPlayerPackageUse() && !player.isOp()) {
            Message.send(player, "&c你没有使用该命令的权限!");
            return;
        }
        if (args.length == 0) {
            showHelp(player);
            return;
        }
        switch (args[0]) {
            case "create":
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOp())
                            if (PackageManager.getPackageCount(player.getName()) >= BookMailPlugin.getConfigManager().getMaxPackageCount()) {
                                Message.send(player, "&e你的包裹已达上限，请先删除");
                                return;
                            }
                        createCommand(player);
                        player.openInventory(Objects.requireNonNull(PackageManager.getPackage(player)).getInventory());
                    }
                }.run();
                break;
            case "edit":
                editCommand(player);
                break;
            case "build":
                int num = 1;
                if (args.length == 2) {
                    try {
                        num = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        Message.send(player, ChatColor.RED + "非法的整形变量");
                    }
                }
                buildCommand(player, num);
                break;
            case "get":
                if (args.length == 2)
                    getPackage(player, args[1], false);
                break;
            case "list":
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ItemStack packageList = MailManager.getPackageList(player.getName());
                        if (packageList == null) {
                            Message.send(player, "&e你没有包裹!");
                            return;
                        }
                        player.openBook(packageList);
                    }
                }.run();
                break;
            case "remove":
                if (args.length == 2)
                    if (PackageManager.removePackage(player, args[1], player.isOp())) {
                        Message.send(player, "&a删除成功!");
                    } else Message.send(player, "&c删除失败!你没有这个包裹。");
                break;
            default:
                showHelp(player);
        }
    }

    private static void createCommand(Player player) {
        if (!PackageManager.contains(player)) {
            Package a = new Package();
            PackageManager.addPackage(player, a);
            int m = BookMailPlugin.getConfigManager().getPackageTime();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (PackageManager.contains(player))
                        Message.send(player, ChatColor.YELLOW + "20秒后将删除包裹，请尽快打包包裹!");
                }
            }.runTaskLater(BookMailPlugin.getInstance(), m * 1200 - 400);//20tick 1秒 此处4分钟
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (PackageManager.contains(player)) {
                        PackageManager.removeTempPackage(player);
                        Message.send(player, ChatColor.RED + "未打包的包裹已删除!");
                    }
                }
            }.runTaskLater(BookMailPlugin.getInstance(), m * 1200);//20tick 1秒 此处5分钟
            Message.send(player, ChatColor.GREEN + "包裹创建成功，输入:" + ChatColor.GOLD + "/BookMail package edit " + ChatColor.RED + "再次编辑");
            Message.send(player, ChatColor.YELLOW + String.valueOf(m) + "分钟后没打包的包裹将自动删除，请尽快打包包裹!");
        } else {
            Message.send(player, ChatColor.YELLOW + "你已经有一个包裹了，请先将其打包发送再创建");
            Message.send(player, ChatColor.GREEN + "输入/BookMail package build " + ChatColor.GOLD + "打包包裹。");
        }
    }

    private static void editCommand(Player player) {
        if (!PackageManager.contains(player)) {
            Message.send(player, ChatColor.YELLOW + "你还没有包裹，请先创建！");
            Message.send(player, ChatColor.GREEN + "输入/BookMail package create " + ChatColor.GOLD + "创建包裹。");
            return;
        }
        player.openInventory(Objects.requireNonNull(PackageManager.getPackage(player)).getInventory());
    }

    private static void buildCommand(Player player, int num) {
        if (!PackageManager.contains(player)) {
            Message.send(player, ChatColor.YELLOW + "你还没有包裹，请先创建！");
            Message.send(player, ChatColor.GREEN + "输入/BookMail package create " + ChatColor.GOLD + "创建包裹。");
            return;
        }
        Package p = PackageManager.getPackage(player);
        if (p == null) return;
        if (p.getSize() == 0) {
            Message.send(player, ChatColor.YELLOW + "你的包裹是空的，请先放入东西！");
            Message.send(player, ChatColor.GREEN + "输入/BookMail package edit " + ChatColor.GOLD + "修改包裹。");
            return;
        }
        if (!p.update() && !player.isOp()) {
            Message.send(player, "&e包裹的物品超出上限:&c" + p.getSize() + "&e/" + p.getMaxSize() + "&e个");
            return;
        }
        if (!PackageManager.buildPackage(player, num)) {
            Message.send(player, ChatColor.RED + "创建包裹失败，请重试或减少物品！");
            return;
        }
        PackageManager.removeTempPackage(player);
    }

    public static Boolean getPackage(Player player, String cdk, Boolean isOP) {
        List<ItemStack> itemList = PackageManager.getPackageItemListFromSql(player.getName(), cdk, isOP);
        if (itemList == null) return false;
        for (ItemStack item : itemList) {
            HashMap<Integer, ItemStack> map = player.getInventory().addItem(item);
            if (map.isEmpty()) continue;
            map.forEach((k, v) -> player.getWorld().dropItem(player.getLocation(), v));
        }
        return true;
    }

    private static void showHelp(Player player) {
        List<String> helpMessage = new ArrayList<>();
        helpMessage.add("&6&m+-------------+&9&l " + BookMailPlugin.getInstance().getName() + "&e - &a&lPackage &6&m+-------------+");
        helpMessage.add(" ");
        helpMessage.add("&b/BookMail &dpackage" + " &6create" + "&e 创建一个包裹");
        helpMessage.add("&b/BookMail &dpackage" + " &6edit" + "&e 修改你的包裹");
        helpMessage.add("&b/BookMail &dpackage" + " &6build" + "&e 将你的包裹打包");
        helpMessage.add("&b/BookMail &dpackage" + " &6list" + "&e 查看你的包裹");
        Message.send(player, helpMessage);
    }

}

