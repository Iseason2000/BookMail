package top.iseason.BookMail.Manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.iseason.BookMail.Util.ItemTranslator;
import top.iseason.BookMail.command.PackageCommand;
import top.iseason.BookMail.myclass.Package;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageManager {
    private static Map<String, Package> tempPlayerPackage;

    public PackageManager() {
        tempPlayerPackage = new HashMap<>();
    }

    public static void addPackage(Player player, Package pack) {
        tempPlayerPackage.put(player.getName(), pack);
    }

    public static void removeTempPackage(Player player) {
        tempPlayerPackage.remove(player.getName());
    }

    public static Boolean removePackage(Player player, String cdk, Boolean isOP) {
        try {
            if (PackageCommand.getPackage(player, cdk, isOP))
                SqlManager.removePackage(cdk);
            else return false;
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }
    public static int getPackageCount(String name){
        try {
            return SqlManager.getRecordValueCount(0,"PackageList","拥有者",name);
        } catch (SQLException throwables) {
            return 0;
        }
    }

    public static Package getPackage(Player player) {
        if (tempPlayerPackage.containsKey(player.getName()))
            return tempPlayerPackage.get(player.getName());
        return null;
    }

    public static Boolean buildPackage(Player player, int num) {
        Package playerPackage = tempPlayerPackage.get(player.getName());
        String zipString = ItemTranslator.itemListToZipString(playerPackage.getItems());
        String cdk = SqlManager.addPackage(zipString, num, player.getName());
        if (cdk == null) return false;
        sendCDKMessage(player, cdk);
        return true;
    }

    public static List<ItemStack> getPackageItemListFromSql(String playerName, String cdk, Boolean isOP) {
        String zipString;
        try {
            zipString = SqlManager.getPackageZipString(playerName, cdk, isOP);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        if (zipString == null) return null;
        return ItemTranslator.zipStringToItemList(zipString);
    }

    public static void sendCDKMessage(Player player, String cdk) {
        String command = "tellraw " + player.getName() + " [{\"text\":\"§a包裹创建成功，§eCDK： §6" + cdk
                + "\"},{\"text\":\"【§d点击复制§r】\",\"clickEvent\":{\"action\":\"copy_to_clipboard\",\"value\":\"" +
                cdk + "\"}},{\"text\":\"【§9点击复制模板§r】\",\"clickEvent\":{\"action\":\"copy_to_clipboard\",\"value\":\"(领取包裹){" +
                cdk + "}\"}}]";
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public static Boolean contains(Player player) {
        return tempPlayerPackage.containsKey(player.getName());
    }
}
