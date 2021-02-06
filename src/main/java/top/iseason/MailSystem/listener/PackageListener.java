package top.iseason.MailSystem.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import top.iseason.MailSystem.MaiLPlugin;
import top.iseason.MailSystem.Util.Package;

public class PackageListener implements Listener {
    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Package.PackageInventory)) return;
        Player player = (Player) event.getPlayer();
        if (MaiLPlugin.getPackageManager().contains(player)) {
            Package a = MaiLPlugin.getPackageManager().getPackage(player);
            a.update();
        }
    }
}
