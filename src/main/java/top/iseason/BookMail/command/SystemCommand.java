package top.iseason.BookMail.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Manager.MailManager;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.SimpleSubCommand;

public class SystemCommand extends SimpleSubCommand {
    SystemCommand(String command) {
        super(command);
        setUsage("system");
        addSubCommand("setMailBox");
        addSubCommand("giveMailBox");
        addSubCommand("systemMail");
        addSubCommand("taskList");
        setDescription("系统功能");
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
        if (args.length == 0) return;
        switch (args[0]) {
            case "giveMailBox":
                if (args.length != 2) return;
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) return;
                ItemStack mailBoxItem = BookMailPlugin.getConfigManager().getOpenMailItem();
                if (mailBoxItem == null) return;
                p.getInventory().addItem(mailBoxItem);
                Message.send(player, ChatColor.GREEN + "操作成功!");
                break;
            case "setMailBox":
                if (args.length != 1) return;
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem.getType().isAir()) return;
                BookMailPlugin.getConfigManager().setOpenMailItem(handItem);
                Message.send(player, ChatColor.GREEN + "操作成功!");
                break;
            case "systemMail":
                ItemStack systemMailBox = MailManager.getSystemMailBox();
                if (systemMailBox == null) return;
                OpenMailCommand.openBook(systemMailBox, player);
                break;
            case "taskList":
                ItemStack taskList = MailManager.getOnTimeTask();
                if (taskList == null) return;
                OpenMailCommand.openBook(taskList, player);
                break;
        }
    }
}
