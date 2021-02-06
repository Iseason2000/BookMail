package top.iseason.BookMail.Manager;

import org.bukkit.entity.Player;
import top.iseason.BookMail.Util.Package;

import java.util.HashMap;
import java.util.Map;

public class PackageManager {
    private final Map<String, Package> tempPlayerPackage;
    public PackageManager(){
        tempPlayerPackage = new HashMap<>();
    }
    public void addPackage(Player player ,Package pack){
        tempPlayerPackage.put(player.getName(),pack);
    }
    public void removePackage(Player player){
        tempPlayerPackage.remove(player.getName());
    }
    public Package getPackage(Player player){
        if(tempPlayerPackage.containsKey(player.getName()))
        return tempPlayerPackage.get(player.getName());
        return null;
    }
    public Boolean contains(Player player){
        return tempPlayerPackage.containsKey(player.getName());
    }
}
