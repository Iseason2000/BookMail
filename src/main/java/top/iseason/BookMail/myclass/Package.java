package top.iseason.BookMail.myclass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Util.ItemTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Package {
    private final int maxSize;
    private int size = 0;
    private final PackageInventory itemList;
    public Package() {
        itemList = new PackageInventory();
        maxSize = BookMailPlugin.getConfigManager().getMaxPackageSize();
    }

    public Boolean update() { //统计包裹所有物品数量（包括潜影盒内）
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
        return size <= maxSize;
    }
    public int getMaxSize() {
        return maxSize;
    }

    public List<ItemStack> getItems(){
        List<ItemStack> items = new ArrayList<>();
        for(ItemStack item : itemList.inventory.getContents()){
            if (item == null) continue;
            items.add(item);
        }
        return items;
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
