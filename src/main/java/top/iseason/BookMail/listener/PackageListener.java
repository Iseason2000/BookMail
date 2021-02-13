package top.iseason.BookMail.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import top.iseason.BookMail.Manager.PackageManager;
import top.iseason.BookMail.myclass.Package;

public class PackageListener implements Listener {
    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Package.PackageInventory)) return;
        Player player = (Player) event.getPlayer();
        if (PackageManager.contains(player)) {
            Package a = PackageManager.getPackage(player);
            if(a==null)return;
            a.update();
        }
    }
}
