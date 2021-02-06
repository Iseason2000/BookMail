package top.iseason.MailSystem.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.MailSystem.MaiLPlugin;
import top.iseason.MailSystem.Util.Message;
import top.iseason.MailSystem.Util.Package;
import top.iseason.MailSystem.Util.SimpleSubCommand;

import java.util.ArrayList;
import java.util.List;

public class PackageCommand extends SimpleSubCommand {

    public PackageCommand(String command) {
        super(command);
        setUsage("package create/edit/build");
        addSubCommand("create");
        addSubCommand("edit");
        addSubCommand("build");
        setDescription("创建/修改/打包 包裹");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;
        if (args.length != 1) {
            showHelp(player);
            return;
        }
        switch (args[0]) {
            case "create":
                createCommand(player);
                break;
            case "edit":
                editCommand(player);
                break;
            case "build":
                buildCommand(player);
                break;
            default:
                showHelp(player);
        }

    }

    private static void createCommand(Player player) {
        if (!MaiLPlugin.getPackageManager().contains(player)) {
            Package a = new Package(54);
            MaiLPlugin.getPackageManager().addPackage(player, a);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (MaiLPlugin.getPackageManager().contains(player))
                        Message.send(player, ChatColor.YELLOW + "一分钟后将删除包裹，请尽快打包包裹!");
                }
            }.runTaskLater(MaiLPlugin.getInstance(), 4800);//20tick 1秒 此处4分钟
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (MaiLPlugin.getPackageManager().contains(player)) {
                        MaiLPlugin.getPackageManager().removePackage(player);
                        Message.send(player, ChatColor.RED + "未打包的包裹已删除!");
                    }
                }
            }.runTaskLater(MaiLPlugin.getInstance(), 6000);//20tick 1秒 此处5分钟
            Message.send(player, ChatColor.GREEN + "包裹创建成功，输入:" + ChatColor.GOLD + "/BookMail package edit 编辑");
            Message.send(player, ChatColor.YELLOW + "5分钟后将没打包包裹将删除，请尽快打包包裹!");
        } else {
            Message.send(player, ChatColor.YELLOW + "你已经有一个包裹了，请先将其打包发送再创建");
            Message.send(player, ChatColor.GREEN + "输入/BookMail package build " + ChatColor.GOLD + "打包包裹。");
        }
    }

    private static void editCommand(Player player) {
        if (!MaiLPlugin.getPackageManager().contains(player)) {
            Message.send(player, ChatColor.YELLOW + "你还没有包裹，请先创建！");
            Message.send(player, ChatColor.GREEN + "输入/BookMail package create " + ChatColor.GOLD + "创建包裹。");
            return;
        }
        player.openInventory(MaiLPlugin.getPackageManager().getPackage(player).getInventory());
    }

    private static void buildCommand(Player player) {
        if (!MaiLPlugin.getPackageManager().contains(player)) {
            Message.send(player, ChatColor.YELLOW + "你还没有包裹，请先创建！");
            Message.send(player, ChatColor.GREEN + "输入/BookMail package create " + ChatColor.GOLD + "创建包裹。");
            return;
        }
        Package p = MaiLPlugin.getPackageManager().getPackage(player);
        p.update();
        if (p.getSize() == 0) {
            Message.send(player, ChatColor.YELLOW + "你的包裹是空的，请先放入东西！");
            Message.send(player, ChatColor.GREEN + "输入/BookMail package edit " + ChatColor.GOLD + "修改包裹。");
        }
    }

    private static void showHelp(Player player) {
        List<String> helpMessage = new ArrayList<>();
        helpMessage.add("&6&m+-------------+&9&l " + MaiLPlugin.getInstance().getName() + "&e - &a&lPackage &6&m+-------------+");
        helpMessage.add("&d/BookMail" + " &6create" + "&e 创建一个包裹");
        helpMessage.add("&d/BookMail" + " &6edit" + "&e 修改你的包裹");
        helpMessage.add("&d/BookMail" + " &6build" + "&e 将你的包裹打包");
        Message.send(player, helpMessage);
    }

}

