package top.iseason.BookMail.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.text.DecimalFormat;
import java.util.List;

public class Message {

    public static void send(CommandSender sender, List<String> message) {

        if (message == null || message.isEmpty()) return;

        for (String string : message) {
            sender.sendMessage(toColor(string));
        }
    }

    public static void send(CommandSender sender, String[] message) {

        if (message == null || message.length == 0) return;

        sender.sendMessage(toColor(message));
    }

    public static void send(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return;
        sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.AQUA + "BookMail" + ChatColor.GOLD + "]" + ChatColor.GRAY + ": " + ChatColor.RESET + toColor(message));
    }

    public static void sendConsole(String message) {
        if (message == null || message.isEmpty()) return;

        ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
        consoleSender.sendMessage(toColor(message));
    }

    public static void sendLog(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Book" + ChatColor.AQUA + "Mail " + ChatColor.GREEN + "| " + ChatColor.RESET + toColor(message));
    }

    public static void sendConsole(String[] message) {

        if (message == null || message.length == 0) return;

        ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
        consoleSender.sendMessage(toColor(message));
    }

    public static void sendConsole(List<String> message) {

        if (message == null || message.size() == 0) return;

        for (String string : message) {
            sendConsole(string);
        }

    }

    public static String toColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String[] toColor(String[] strings) {

        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            strings[i] = (toColor(string));
        }

        return strings;
    }

    public static List<String> toColor(List<String> strings) {

        for (int i = 0; i < strings.size(); i++) {
            String string = strings.get(i);
            strings.set(i, toColor(string));
        }

        return strings;
    }

    public static String formatMoney(Number number) {
        DecimalFormat format = new DecimalFormat(",###.##");
        return format.format(number);
    }

    public static String withoutColor(String string) {
        return ChatColor.stripColor(string);
    }

    public static String replace(String message, String... replace) {

        if (replace.length % 2 != 0) {
            throw new IllegalArgumentException("replace 参数应为2的倍数！");
        }

        for (int count = 0; count < replace.length; count += 2) {

            String oldChar = replace[count];

            if (!message.contains(oldChar)) {
                continue;
            }

            String newChar = replace[count + 1];
            message = message.replace(oldChar, newChar);
        }

        return message;
    }

    public static List<String> replace(List<String> strings, String... replace) {

        for (int i = 0; i < strings.size(); i++) {
            String string = strings.get(i);
            strings.set(i, replace(string, replace));
        }

        return strings;
    }

    public static String[] replace(String[] strings, String... replace) {

        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            strings[i] = replace(string, replace);
        }

        return strings;
    }
}
