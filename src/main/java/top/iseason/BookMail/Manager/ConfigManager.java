package top.iseason.BookMail.Manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Util.ItemTranslator;

public class ConfigManager {
    BookMailPlugin plugin;
    FileConfiguration config;
    ItemStack openMailItem;
    public ConfigManager(BookMailPlugin plugin){
        this.plugin = plugin;
        config = plugin.getConfig();
        String itemString = config.getString("邮箱物品");
        if(itemString==null)
            openMailItem=null;
        else
            openMailItem = ItemTranslator.zipStringToItem(itemString);
    }
    public ItemStack getOpenMailItem() {
        return openMailItem;
    }

    public void setOpenMailItem(ItemStack openMailItem) {
        this.openMailItem = openMailItem;
        config.set("邮箱物品",ItemTranslator.itemToZipString(openMailItem));
        plugin.saveConfig();
    }

}
