package top.iseason.MailSystem.command;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.iseason.MailSystem.Util.ItemTranslator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OpenMailCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用这个命令");
            return true;
        }
        Player player = (Player) sender;
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack old = player.getInventory().getItem(slot);
        if (old != null && old.getType() == Material.WRITTEN_BOOK)
        openBook(old,player);
        System.out.println(ItemTranslator.itemToNBTString(old));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("create", "help"));
            list.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return list;
        } else {
            return null;
        }
    }

    public static void openBook(ItemStack book, Player p) { //使玩家打开某本书(署名的)
        int slot = p.getInventory().getHeldItemSlot();
        ItemStack old = p.getInventory().getItem(slot);
        p.getInventory().setItem(slot, book);
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer pc = pm.createPacket(PacketType.Play.Server.OPEN_BOOK);
        try {
            pm.sendServerPacket(p, pc);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        p.getInventory().setItem(slot, old);
    }


}


