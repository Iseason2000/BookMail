package top.iseason.BookMail.Manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Util.ItemTranslator;

public class ConfigManager {
    BookMailPlugin plugin;
    FileConfiguration config;
    ItemStack openMailItem;
    int maxPackageCount;
    int maxPackageSize;
    int packageTime;
    boolean isPlayerUse;
    boolean isPlayerPackageUse;

    public ConfigManager(BookMailPlugin plugin) {
        this.plugin = plugin;
        reload();
    }
    public void reload(){
        plugin.reloadConfig();
        config = plugin.getConfig();
        String itemString = config.getString("邮箱物品");
        if (itemString == null)
            openMailItem = null;
        else
            openMailItem = ItemTranslator.zipStringToItem(itemString);
        maxPackageSize = config.getInt("玩家包裹最大物品数");
        maxPackageCount = config.getInt("玩家最大包裹数");
        packageTime = config.getInt("临时包裹时间");
        isPlayerUse = config.getBoolean("玩家是否可以发邮件");
        isPlayerPackageUse = config.getBoolean("玩家是否可以发送包裹");
    }

    public ItemStack getOpenMailItem() {
        return openMailItem;
    }

    public int getMaxPackageSize() {
        return maxPackageSize;
    }

    public void setOpenMailItem(ItemStack openMailItem) {
        this.openMailItem = openMailItem;
        config.set("邮箱物品", ItemTranslator.itemToZipString(openMailItem));
        plugin.saveConfig();
    }

    public int getMaxPackageCount() {
        return maxPackageCount;
    }

    public int getPackageTime() {
        return packageTime;
    }

    public boolean isPlayerUse() {
        return isPlayerUse;
    }
    public boolean isPlayerPackageUse() {
        return isPlayerPackageUse;
    }

}
