package top.iseason.BookMail.command;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.iseason.BookMail.Util.SimpleSubCommand;

import java.lang.reflect.InvocationTargetException;

public class OpenMailCommand extends SimpleSubCommand {
    OpenMailCommand(String command){
        super(command);
        setUsage("open [page]");
        setDescription("打开自己的邮箱");
    }
    @Override
    public void onCommand(CommandSender sender, String[] args){
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用这个命令");
            return;
        }
//        Player player = (Player) sender;
//        ItemStack book = new ItemStack(Material.WRITTEN_BOOK,1);
//        openBook(book, player);
    }
//    @Override
//    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
//        if (args.length == 1) {
//            List<String> list = new ArrayList<>(Arrays.asList("create", "help"));
//            list.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
//            return list;
//        } else {
//            return null;
//        }
//    }

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


