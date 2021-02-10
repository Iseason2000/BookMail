package top.iseason.BookMail.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Manager.MailManager;
import top.iseason.BookMail.Manager.TimeManager;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.SimpleSubCommand;
import top.iseason.BookMail.Util.Tools;
import top.iseason.BookMail.myclass.Mail;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MailSendOnTimeCommand extends SimpleSubCommand {
    MailSendOnTimeCommand(String command) {
        super(command);
        setUsage("sendOnTime");
        addSubCommand("once");
        addSubCommand("period");
        setDescription("查看定时邮件帮助命令 [需要OP]");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用这个命令");
            return;
        }
        Player player = (Player) sender;
        if (!player.isOp()) {
            Message.send(player, ChatColor.RED + "你没有此命令的使用权限！");
        }
        int argsLength = args.length;
        if (argsLength != 3) {
            showHelp(player);
            return;
        }
        if (args[0].equals("once")) {
            sendOnce(player, args);
        } else if (args[0].equals("period")) {
            sendPeriod(player, args);
        }
    }

    public static void sendOnce(Player player, String[] args) {
        if (!checkOnceType(args[1])) {
            Message.send(player, ChatColor.RED + "参数错误!");
            return;
        }
        if (!checkMailType(args[2])) {
            Message.send(player, ChatColor.RED + "邮件类型错误!");
            return;
        }
        Mail mail = MailManager.getMailInHand(player);
        if (mail == null) return;
        mail.groupID = Tools.getCDK(5);
        mail.type = "once-" + args[2];
        if (!MailManager.sendSystemMail(mail)) {
            Message.send(player, ChatColor.RED + "发送失败！数据库异常。");
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
                String time = tarnsOnceType(args[1]).format(formatter);
                if (TimeManager.addOnTimeTask(mail.groupID, args[2], "once", time))
                    Message.send(player, "&a已添加 &6" + time + " &a的定时邮件！");
                else
                    Message.send(player, "&c邮件发送失败！");
            }
        }.runTaskAsynchronously(BookMailPlugin.getInstance());
    }

    public static void sendPeriod(Player player, String[] args) {
        if (!checkPeriodType(args[1])) {
            Message.send(player, ChatColor.RED + "参数错误!");
            return;
        }
        if (!checkMailType(args[2])) {
            Message.send(player, ChatColor.RED + "邮件类型错误!");
            return;
        }
        Mail mail = MailManager.getMailInHand(player);
        if (mail == null) return;
        mail.groupID = Tools.getCDK(5);
        mail.type = "once-" + args[2];
        if (!MailManager.sendSystemMail(mail)) {
            Message.send(player, ChatColor.RED + "发送失败！数据库异常。");
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if (TimeManager.addOnTimeTask(mail.groupID, args[2], "period", args[1])) {
                    String[] taskArgs = splitPeriodType(args[1]);
                    if(taskArgs !=null)
                    if (taskArgs[0].contains("day")) {
                        Message.send(player, "&a已添加 每 &b" + taskArgs[1] + " &6天&e" + taskArgs[2] + " &a的定时邮件！");
                    } else {
                        Message.send(player, "&a已添加 每 &b" + taskArgs[1] + " &a个&6月&c " + taskArgs[2] + " &a号&e " + taskArgs[3] + " &a 的定时邮件！");
                    }
                } else
                    Message.send(player, "&c邮件发送失败！");
            }
        }.runTaskAsynchronously(BookMailPlugin.getInstance());
    }

    private static Boolean checkMailType(String mailTypeString) {
        return tansMailType(mailTypeString) != null;
    }

    private static Boolean checkPeriodType(String arg) {
        return splitPeriodType(arg) != null;
    }

    public static String[] splitPeriodType(String arg) {

        Matcher matcher1 = Pattern.compile("(day):(\\d+)\\+(.+)").matcher(arg);
        if (matcher1.matches()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            try {
                LocalTime.parse(matcher1.group(3), formatter);
                String[] args = new String[3];
                args[0] = matcher1.group(1);
                args[1] = matcher1.group(2);
                args[2] = matcher1.group(3);
                return args;
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        Matcher matcher2 = Pattern.compile("(month):(\\d+)\\+(\\d+)-(.+)").matcher(arg);
        if (matcher2.matches()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            try {
                LocalTime.parse(matcher2.group(4), formatter);
                String[] args = new String[4];
                args[0] = matcher2.group(1);
                args[1] = matcher2.group(2);
                args[2] = matcher2.group(3);
                args[3] = matcher2.group(4);
                return args;
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }

    private static Boolean checkOnceType(String arg) {
        return tarnsOnceType(arg) != null;
    }

    public static LocalDateTime tarnsOnceType(String arg) {
        LocalDateTime time = Tools.formatSimpleTimePlus(arg);
        if (time != null) return time;
        time = Tools.formatDataTimeString(arg, "yyyy-MM-dd-HH:mm:ss");
        return time;
    }

    public static ArrayList<String> tansMailType(String mailTypeString) {
        ArrayList<String> mailType = new ArrayList<>();
        switch (mailTypeString) {
            case "online":
            case "offline":
            case "registered":
                mailType.add(mailTypeString);
                return mailType;
            default:
                Matcher matcher = Pattern.compile("loginTime\\[(.*)]").matcher(mailTypeString);
                if (!matcher.matches()) return null;
                mailType.add("loginTime");
                String args = matcher.group(1);
                if (args.contains(",")) {
                    String[] arg = args.split(",");
                    for (String a : arg) {
                        if (Tools.formatSimpleTimeMinus(a) == null && Tools.formatDataTimeString(a, "yyyy-MM-dd-HH:mm:ss") == null)
                            return null;
                    }
                    mailType.add(arg[0]);
                    mailType.add(arg[1]);
                } else {
                    if (Tools.formatSimpleTimeMinus(args) == null && Tools.formatDataTimeString(args, "yyyy-MM-dd-HH:mm:ss") == null)
                        return null;
                    mailType.add(args);
                }
                return mailType;
        }
    }

    private static void showHelp(Player player) {
        List<String> helpMessage = new ArrayList<>();
        helpMessage.add("&6&m+-------------+&9&l " + BookMailPlugin.getInstance().getName() + "&e - &a&lSendOnTime &6&m+-------------+");
        helpMessage.add(" ");
        helpMessage.add("&b/BookMail sendOnTime" + " &6once    &c[参数] &a[邮件类型] " + "&e添加&6一次性&e定时邮件");
        helpMessage.add("&b/BookMail sendOnTime" + " &6period  &c[参数] &a[邮件类型] " + "&e添加&6周期性&e定时邮件");
        helpMessage.add("&a参数说明: ");
        helpMessage.add("&6一次性&a-> &c具体时间&a 如:&e2021-02-12-12:00:00");
        helpMessage.add("&6周期性&a-> &c周期+时间点&a 如:&eday:2+12:00:00 &a为 &6每2天中午12点");
        helpMessage.add("&6周期性&a-> &c周期+时间点&a 如:&emonth:1+2-12:00:00 &a为 &6每1个月2号中午12点");
        helpMessage.add("&a邮件类型说明: ");
        helpMessage.add("&6可选&a: &c online/offline/registered/loginTime[参数]");
        helpMessage.add("&6online/offline/register&a: &c对应群发类型");
        helpMessage.add("&6loginTime&a: &c参数&a对应群发的参数:&9[1h]&a或&9[1d,2d]&a，如：&eloginTime[1d,2d]");
        helpMessage.add(" ");
        Message.send(player, helpMessage);
    }

}
