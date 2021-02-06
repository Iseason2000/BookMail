package top.iseason.BookMail.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Util.Package;

public class PackageListener implements Listener {
    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Package.PackageInventory)) return;
        Player player = (Player) event.getPlayer();
        if (BookMailPlugin.getPackageManager().contains(player)) {
            Package a = BookMailPlugin.getPackageManager().getPackage(player);
            a.update();
        }
    }
}
