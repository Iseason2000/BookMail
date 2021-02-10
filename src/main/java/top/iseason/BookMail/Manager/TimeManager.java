package top.iseason.BookMail.Manager;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.Tools;
import top.iseason.BookMail.command.MailSendGroupCommand;
import top.iseason.BookMail.command.MailSendOnTimeCommand;
import top.iseason.BookMail.myclass.Mail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimeManager extends BukkitRunnable {
    Map<LocalDateTime, String> taskList;

    public TimeManager() {
        taskList = new HashMap<>();
        runTaskTimerAsynchronously(BookMailPlugin.getInstance(), 0, 20);
    }

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        for (LocalDateTime taskTime : taskList.keySet()) {
            if (!taskTime.isBefore(now)) continue;
            sendGroupMailWithGroupID(taskList.get(taskTime)); //发送对应的邮件
            taskList.remove(taskTime);
        }
        if (taskList.isEmpty()) {
            setNextTasks();
        }
    }

    public void setNextTasks() {
        try {
            ResultSet taskSet = SqlManager.getTaskList();
            String groupID = null;
            LocalDateTime time = null;
            while (taskSet.next()) {
                if (groupID == null) {
                    groupID = taskSet.getString(2);
                    String type = taskSet.getString(4);
                    String sendTimeString = taskSet.getString(5);
                    String taskTimeString = taskSet.getString(6);
                    time = getNextTime(type, sendTimeString, taskTimeString);
                    taskList.put(time, groupID);
                    continue;
                }
                String group = taskSet.getString(2);
                String type = taskSet.getString(4);
                String sendTimeString = taskSet.getString(5);
                String taskTimeString = taskSet.getString(6);
                LocalDateTime taskTime = getNextTime(type, sendTimeString, taskTimeString);
                if (taskTime != null && time != null)
                    if (taskTime.isBefore(time)) {
                        taskList.remove(time);
                        time = taskTime;
                        groupID = group;
                        taskList.put(taskTime, groupID);
                    } else if (taskTime.isEqual(time)) {
                        taskList.put(taskTime, groupID);
                    }
            }
            if (taskList.isEmpty()) {
                Message.sendLog("&d定时任务: &a已无定时任务！&6");
                this.cancel();
            } else {
                if (this.isCancelled()) {
                    BookMailPlugin.setTimeManager(new TimeManager());
                    return;
                }
                Set<LocalDateTime> localDateTimes = taskList.keySet();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                Message.sendLog("&d定时任务: &a下次定时任务将在&6 " + localDateTimes.iterator().next().format(formatter) + "&a 运行!");
            }
        } catch (SQLException throwables) {
            Message.sendLog(ChatColor.RED + "数据库连接异常，定时任务列表获取失败！");
        }
    }

    private static LocalDateTime getNextTime(String type, String timeString, String taskTimeString) {
        if (type.equals("once")) {
            return Tools.formatDataTimeString(timeString, "yyyy-MM-dd-HH:mm:ss");
        }
        LocalDateTime taskTime = Tools.formatDataTimeString(taskTimeString, "yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        Period period = Period.between(taskTime.toLocalDate(), now.toLocalDate());
        String[] args = MailSendOnTimeCommand.splitPeriodType(timeString);
        if (args != null)
            if (args[0].equals("day")) {
                int day = Integer.parseInt(args[1]);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalTime time = LocalTime.parse(args[2], formatter);
                long nextPlusDay = period.getDays() % day; //距离下一次的天数
                if (nextPlusDay == 0) {
                    if (time.isBefore(now.toLocalTime())) nextPlusDay++;
                }
                return LocalDateTime.of(now.plusDays(nextPlusDay).toLocalDate(), time);
            }
        if (args != null)
            if (args[0].equals("month")) {
                int month = Integer.parseInt(args[1]);
                long nextPlusMonth = period.getMonths() % month; //距离下一次的月
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalTime time = LocalTime.parse(args[3], formatter);
                if (nextPlusMonth == 0) {
                    if (Integer.parseInt(args[2]) < now.toLocalDate().getDayOfMonth()) nextPlusMonth++;
                    else if (time.isBefore(now.toLocalTime())) nextPlusMonth++;
                }
                LocalDateTime newTime = now.plusMonths(nextPlusMonth);
                return LocalDateTime.of(newTime.toLocalDate(), time);
            }
        return null;
    }

    public static Boolean addOnTimeTask(String groupID, String groupArgs, String type, String sendTime) {
        try {
            SqlManager.addTask(groupID, groupArgs, type, sendTime, Tools.getDataAndTime());
            BookMailPlugin.getTimeManager().setNextTasks();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }

    }

    public static void sendGroupMailWithGroupID(String groupID) {
        try {
            Mail systemMail = SqlManager.getSystemMail(groupID);
            systemMail.isAccept = false;
            systemMail.isRead = false;
            systemMail.theme = "";
            systemMail.content = "";
            systemMail.attached = "";
            systemMail.time = Tools.getDataAndTime();
            String groupMailType = SqlManager.getTaskString(groupID, "群发参数");
            String taskType = SqlManager.getTaskString(groupID, "类型");
            String[] players = null;
            String mailTypeString = null;
            ArrayList<String> mailType = MailSendOnTimeCommand.tansMailType(groupMailType);
            if (mailType == null) return;
            switch (mailType.get(0)) {
                case "online":
                    players = MailSendGroupCommand.getOnlinePlayer();
                    mailTypeString = "在线";
                    break;
                case "offline":
                    players = MailSendGroupCommand.getOfflinePlayer();
                    mailTypeString = "离线";
                    break;
                case "registered":
                    players = MailSendGroupCommand.getAllPlayer();
                    mailTypeString = "注册";
                    break;
                case "loginTime":
                    LocalDateTime dateTime1 = null;
                    LocalDateTime dateTime2 = null;
                    if (mailType.size() >= 2) {
                        String arg1 = mailType.get(1);
                        dateTime1 = Tools.formatSimpleTimeMinus(arg1);
                        if (dateTime1 == null) {
                            dateTime1 = Tools.formatDataTimeString(arg1, "yyyy-MM-dd-HH:mm:ss");
                        }
                    }
                    if (mailType.size() == 3) {
                        String arg2 = mailType.get(2);
                        dateTime2 = Tools.formatSimpleTimeMinus(arg2);
                        if (dateTime2 == null) {
                            dateTime2 = Tools.formatDataTimeString(arg2, "yyyy-MM-dd-HH:mm:ss");
                        }
                    }
                    players = MailSendGroupCommand.getPlayerAfterLoginTime(dateTime1, dateTime2);
                    mailTypeString = "注册时间";
                    break;
            }
            String type;
            if (taskType.equals("once")) {
                SqlManager.removeTask(groupID);
                type = "一次性";
            } else {
                type = "周期性";
            }
            if (players == null) {
                Message.sendLog("&c定时发送失败，没有匹配的玩家！");
                return;
            }
            List<String> failureList = MailManager.sendMailtoPlayers(systemMail, players);
            int mailCount = players.length;
            int failureCount = failureList.size();

            Message.sendLog("&d定时任务: &a已自动发送 &c" + type + " &6" + mailTypeString + " &a邮件 &9" + (mailCount - failureCount) + "&a/&6" + mailCount + "&a封。");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
