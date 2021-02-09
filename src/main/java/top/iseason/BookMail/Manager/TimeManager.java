package top.iseason.BookMail.Manager;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.Util.Message;
import top.iseason.BookMail.Util.Tools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeManager extends BukkitRunnable {
    Map<LocalDateTime, String> taskList;

    public TimeManager() {
        taskList = new HashMap<>();
        setNextTasks();
        runTaskTimerAsynchronously(BookMailPlugin.getInstance(), 0, 20);
    }

    @Override
    public void run() {
//        if (taskList.isEmpty()) this.cancel();
        LocalDateTime now = LocalDateTime.now();
        for (LocalDateTime taskTime : taskList.keySet()) {
            if (!taskTime.isBefore(now)) continue;
            try {
                SqlManager.removeTask(taskList.get(taskTime));
                //todo:发送对应的邮件
                setNextTasks();
            } catch (SQLException ignored) {
            }
            taskList.remove(taskTime);
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
                    } else if (taskTime.equals(time)) {
                        taskList.put(taskTime, groupID);
                    }
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
        if (type.contains("day:")) {
            Matcher matcher = Pattern.compile("day:(\\d*)").matcher(type);
            if (matcher.matches()) {
                int day = Integer.parseInt(matcher.group(1));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalTime time = LocalTime.parse(timeString, formatter);
                long nextPlusDay = period.getDays() % day; //距离下一次的天数
                return LocalDateTime.of(now.plusDays(nextPlusDay).toLocalDate(), time);
            }
        }
        if (type.contains("month:")) {
            Matcher matcher = Pattern.compile("month:(\\d*)").matcher(type);
            if (matcher.matches()) {
                int month = Integer.parseInt(matcher.group(1));
                LocalDateTime time = Tools.formatDataTimeString("2020-1-".concat(timeString), "yyyy-MM-dd HH:mm:ss");
                long nextPlusMonth = period.getMonths() % month; //距离下一次的月
                LocalDateTime newTime = now.plusMonths(nextPlusMonth);
                return LocalDateTime.of(newTime.getYear(), newTime.getMonth(), time.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond());
            }
        }
        return null;
    }

    public static Boolean addOnceOnTimeTask(String groupID, String groupArgs, String type, String sendTime) {

        try {
            SqlManager.addTask(groupID, groupArgs, type, sendTime, Tools.getDataAndTime());
            BookMailPlugin.getTimeManager().setNextTasks();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }

    }
}
