package top.iseason.MailSystem.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Package {
    private int maxSize;
    private int size = 0;
    private PackageInventory itemList;

    public Package(int maxSize) {
        this.maxSize = maxSize;
        itemList = new PackageInventory();
    }

    public void update() {
        int count = 0;
        for (ItemStack item : itemList.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType().name().contains("SHULKER_BOX")) {
                String nbt = ItemTranslator.itemToNBTString(item);
                Matcher m = Pattern.compile("Slot:?").matcher(nbt);
                while (m.find()) {
                    count++;
                }
            }
            count++;
        }
        size = count;
    }

    public int getSize() {
        return size;
    }
    public Inventory getInventory(){
        return itemList.getInventory();
    }

    public static class PackageInventory implements InventoryHolder {
        private final Inventory inventory = Bukkit.createInventory(this, 54, ChatColor.GOLD + "邮件" + ChatColor.LIGHT_PURPLE + "暂存箱");

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }

//    public Inventory getInventory(){
//
//    }


}
