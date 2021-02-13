package top.iseason.BookMail.Manager;

import org.bukkit.ChatColor;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.myclass.Mail;
import top.iseason.BookMail.Util.Tools;
import top.iseason.BookMail.myclass.Task;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static top.iseason.BookMail.Util.Message.sendLog;

public class SqlManager {
    private static Connection mailBoxesConnection = null;
    private static Statement mailBoxesStmt = null;
    private static Connection systemConnection = null;
    private static Statement systemStmt = null;

    public static void initSqilte() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        File playerMailBoxesFile = new File(BookMailPlugin.getInstance().getDataFolder(), "mailboxes.db");
        File systemFile = new File(BookMailPlugin.getInstance().getDataFolder(), "system.db");
        if (!playerMailBoxesFile.exists())
            sendLog(ChatColor.YELLOW + "未找到玩家邮箱数据库，已创建新数据库!");
        mailBoxesConnection = DriverManager.getConnection("jdbc:sqlite:" + playerMailBoxesFile.getAbsolutePath().replace("\\", "/"));
        mailBoxesStmt = mailBoxesConnection.createStatement();
        mailBoxesConnection.setAutoCommit(false);
        if (!systemFile.exists())
            sendLog(ChatColor.YELLOW + "未找到邮件数据库，已创建新数据库!");
        systemConnection = DriverManager.getConnection("jdbc:sqlite:" + systemFile.getAbsolutePath().replace("\\", "/"));
        systemStmt = systemConnection.createStatement();
        systemConnection.setAutoCommit(false);
        createSystemPackageTable();
        createSystemMailTable();
        createSystemPlayerLoginTimeTable();
        createSystemOnTimeTaskTable();
    }

    public static void disableSqlite() throws SQLException {
        mailBoxesStmt.close();
        mailBoxesConnection.close();
        systemStmt.close();
        systemConnection.close();
        sendLog(ChatColor.YELLOW + "数据库已断开！");
    }

    public static boolean isTableExist(int database, String tableName) throws SQLException {
        boolean flag = false;
        String sql = "SELECT COUNT(*) FROM sqlite_master where type='table' and name='" + tableName.trim() + "'";
        ResultSet set;
        if (database == 0) {
            set = systemStmt.executeQuery(sql); //0为系统
        } else {
            set = mailBoxesStmt.executeQuery(sql); //1为 玩家
        }
        int count = set.getInt(1);
        if (count == 1) flag = true;
        return flag;
    }

    private static void createSystemPackageTable() throws SQLException {
        if (isTableExist(0, "PackageList")) return;
        String sql = "CREATE TABLE " + "PackageList".trim() +
                "(包裹ID TEXT NOT NULL," +
                "内容 TEXT," +
                "数量 INTEGER," +
                "拥有者 TEXT," +
                "创建时间 TEXT," +
                "PRIMARY KEY(包裹ID));";
        systemStmt.executeUpdate(sql);
        systemConnection.commit();
    }

    private static void createSystemPlayerLoginTimeTable() throws SQLException {
        if (isTableExist(0, "LoginTime")) return;
        String sql = "CREATE TABLE " + "LoginTime" +
                "(玩家名称 TEXT ," +
                "登录时间 TEXT," +
                "PRIMARY KEY(玩家名称));";
        systemStmt.executeUpdate(sql);
        systemConnection.commit();
    }

    public static void updatePlayerLoginTime(String name, String time) throws SQLException {
        String sql = "INSERT OR REPLACE INTO LoginTime (玩家名称,登录时间) VALUES (\"" + name.trim() + "\",\"" + time + "\");";
        systemStmt.executeUpdate(sql);
        systemConnection.commit();
    }

    public static ResultSet getPlayerLoginTime() throws SQLException {
        return systemStmt.executeQuery("SELECT * FROM LoginTime;");
    }

    public static Object getPackageValue(String cdk, String column) throws SQLException {
        ResultSet rs = systemStmt.executeQuery("SELECT * FROM PackageList WHERE 包裹ID=\"" + cdk + "\";");
        if (rs.isClosed()) return null;
        return rs.getObject(column);
    }

    public static ResultSet getPackageList() throws SQLException {
        return systemStmt.executeQuery("SELECT * FROM PackageList;");
    }

    public static ResultSet getPackageList(String playerName) throws SQLException {
        return systemStmt.executeQuery("SELECT * FROM PackageList WHERE 拥有者=\"" + playerName + "\";");
    }

    public static String addPackage(String zipString, int num, String owner) {
        String cdk = Tools.getCDK(11);
        String sql = "INSERT INTO PackageList"
                + " (包裹ID,内容,数量,拥有者,创建时间) VALUES (\""
                + cdk + "\", " + "\"" + zipString + "\", " + num + ", "
                + "\"" + owner + "\", " + "\"" + Tools.getDataAndTime() + "\");";
        try {
            systemStmt.executeUpdate(sql);
            systemConnection.commit();
            return cdk;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static String getPackageZipString(String playerName, String cdk, Boolean isOP) throws SQLException {
        if (!isRecordExist(0, "PackageList", "包裹ID", cdk)) return null;
        ResultSet rs = systemStmt.executeQuery("SELECT * FROM PackageList WHERE 包裹ID=\"" + cdk + "\";");
        String zipString = rs.getString(2);
        int count = rs.getInt(3);
        String name = (String) getPackageValue(cdk, "拥有者");
        if (!isOP)
            if (!(name != null && name.equals(playerName))) {//不是是拥有者
                ResultSet grs = systemStmt.executeQuery("SELECT * FROM SystemMail WHERE 附件=\"" + cdk + "\";");
                if (!grs.isClosed()) { //是群发的
                    String groupID = grs.getString(2);
                    ResultSet prs = mailBoxesStmt.executeQuery("SELECT * FROM " + playerName + " WHERE 群发ID=\"" + groupID + "\";");
                    if (prs.isClosed()) return null;//群发列表里没有这个人
                    if (prs.getBoolean(9)) return null;//领过了
                    addSystemMailAcceptCount(cdk);
                    setPlayerMailIsAccept(playerName, prs.getInt(1));
                } else { //不是群发的
                    ResultSet prs = mailBoxesStmt.executeQuery("SELECT * FROM " + playerName + " WHERE 附件=\"" + cdk + "\";");
                    if (prs.isClosed()) return null;//这个人没有收到这个包裹
                    if (prs.getBoolean(9)) return null;//领过了
                    setPlayerMailIsAccept(playerName, cdk);
                }
            }
        if (count == 1) {
            removePackage(cdk);
        } else if (count > 1) {
            systemStmt.executeUpdate("UPDATE PackageList SET 数量 = " + (count - 1) + " WHERE 包裹ID=\"" + cdk + "\";");
            systemConnection.commit();
        }
        //todo 只有收到邮件的才可以领取，包裹有数量限制
        return zipString;
    }

    public static Boolean isRecordExist(int database, String tableName, String column, String value) throws SQLException {
        int count = getRecordValueCount(database, tableName, column, value);
        return count >= 1;
    }

    public static int getRecordValueCount(int database, String tableName, String column, String value) throws SQLException {
        Statement statement;
        if (database == 1) {
            statement = mailBoxesStmt;
        } else {
            statement = systemStmt;
        }
        String sql = "SELECT COUNT(*) FROM " + tableName.trim() + " where " + column.trim() + "='" + value + "';";
        ResultSet resultSet = statement.executeQuery(sql);
        return resultSet.getInt(1);
    }

    public static void removePackage(String cdk) throws SQLException {
        String sql = "DELETE from PackageList where 包裹ID=\"" + cdk + "\";";
        systemStmt.executeUpdate(sql);
        systemConnection.commit();

    }

    private static void createSystemMailTable() throws SQLException {
        if (isTableExist(0, "SystemMail")) return;
        String sql = "CREATE TABLE SystemMail" +
                "(ID INTEGER NOT NULL," +
                "群发ID TEXT," +
                "类型 TEXT," +
                "主题 TEXT," +
                "内容 TEXT," +
                "附件 TEXT," +
                "发送者 TEXT," +
                "发送时间 TEXT," +
                "阅读数 INTEGER," +
                "领取数 INTEGER," +
                "PRIMARY KEY(ID AUTOINCREMENT));";
        systemStmt.executeUpdate(sql);
        systemConnection.commit();
    }

    private static void createSystemOnTimeTaskTable() throws SQLException {
        if (isTableExist(0, "OnTimeTask")) return;
        String sql = "CREATE TABLE OnTimeTask" +
                "(ID INTEGER NOT NULL," +
                "群发ID TEXT," +
                "群发参数 TEXT," +
                "类型 TEXT," +
                "发送时间 TEXT," +
                "添加时间 TEXT," +
                "PRIMARY KEY(ID AUTOINCREMENT));";
        systemStmt.executeUpdate(sql);
        systemConnection.commit();
    }

    public static void addTask(String groupID, String groupArgs, String type, String time, String addTime) throws SQLException {
        String sql = "INSERT INTO OnTimeTask" +
                " (群发ID,群发参数,类型,发送时间,添加时间) VALUES (\""
                + groupID + "\", " + "\"" + groupArgs + "\", "
                + "\"" + type + "\", " + "\"" + time + "\"," + "\"" + addTime + "\");";
        systemStmt.executeUpdate(sql);
        systemConnection.commit();
    }

    public static void removeTask(String groupID) throws SQLException {
        String sql = "DELETE from OnTimeTask where 群发ID=\"" + groupID + "\";";
        systemStmt.executeUpdate(sql);
        systemConnection.commit();

    }

    public static List<Task> getTaskList() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        ResultSet taskList = systemStmt.executeQuery("SELECT * FROM OnTimeTask;");
        while (taskList.next()) {
            Task task = new Task();
            task.groupID = taskList.getString(2);
            task.groupArg = taskList.getString(3);
            task.type = taskList.getString(4);
            task.sendTime = taskList.getString(5);
            task.addTime = taskList.getString(6);
            tasks.add(task);
        }
        return tasks;
    }

    public static String getTaskString(String groupID, String column) throws SQLException {
        String sql = "SELECT * FROM OnTimeTask WHERE 群发ID=\"" + groupID + "\";";
        ResultSet rs = systemStmt.executeQuery(sql);
        return rs.getString(column);
    }

    public static void addSystemMail(Mail mail) throws SQLException {
        String sql = "INSERT INTO SystemMail" +
                " (群发ID,类型,主题,内容,附件,发送者,发送时间,阅读数,领取数) VALUES (\""
                + mail.groupID + "\", " + "\"" + mail.type + "\", " + "\"" + mail.theme + "\", "
                + "\"" + mail.content + "\", " + "\"" + mail.attached + "\", " + "\"" + mail.sender
                + "\", " + "\"" + mail.time + "\", " + "0," + "0);";
        systemStmt.executeUpdate(sql);
        systemConnection.commit();
    }

    public static ArrayList<Mail> getSystemMails(String type) throws SQLException {
        String sql = "SELECT * FROM SystemMail WHERE 类型=\"" + type + "\";";
        ResultSet rs = systemStmt.executeQuery(sql);
        ArrayList<Mail> mails = new ArrayList<>();
        while (rs.next()) {
            Mail mail = new Mail();
            mail.groupID = rs.getString(2);
            mail.sender = rs.getString(7);
            mail.time = rs.getString(8);
            mails.add(mail);
        }
        return mails;
    }

    public static ArrayList<Mail> getSystemMails() throws SQLException {
        String sql = "SELECT * FROM SystemMail;";
        ResultSet rs = systemStmt.executeQuery(sql);
        ArrayList<Mail> mails = new ArrayList<>();
        while (rs.next()) {
            Mail mail = new Mail();
            mail.groupID = rs.getString(2);
            mail.type = rs.getString(3);
            mail.theme = rs.getString(4);
            mail.attached = rs.getString(6);
            mail.sender = rs.getString(7);
            mail.time = rs.getString(8);
            mail.readCount = rs.getInt(9);
            mail.acceptCount = rs.getInt(10);
            mails.add(mail);
        }
        return mails;
    }

    public static Mail getSystemMail(String groupID) throws SQLException {
        String sql = "SELECT * FROM SystemMail WHERE 群发ID=\"" + groupID + "\";";
        ResultSet rs = systemStmt.executeQuery(sql);
        Mail mail = new Mail();
        mail.groupID = rs.getString(2);
        mail.sender = rs.getString(7);
        mail.time = rs.getString(8);
        return mail;
    }

    public static Boolean removeSystemMail(String groupID) throws SQLException {
        if (!isRecordExist(0, "SystemMail", "群发ID", groupID)) return false;
        String sql = "DELETE from SystemMail where 群发ID=\"" + groupID + "\";";
        systemStmt.executeUpdate(sql);
        systemConnection.commit();
        return true;
    }

    public static String getSystemMailContent(String groupID) throws SQLException {
        String sql = "SELECT * FROM SystemMail WHERE 群发ID=\"" + groupID + "\";";
        ResultSet rs = systemStmt.executeQuery(sql);
        return rs.getString(5);
    }

    public static boolean createPlayerMailBoxTable(String tableName) throws SQLException { //不区分大小写
        if (isTableExist(1, tableName)) return false;
        String sql = "CREATE TABLE " + tableName.trim() +
                "(ID INTEGER NOT NULL," +
                "群发ID TEXT," +
                "主题 TEXT," +
                "内容 TEXT," +
                "附件 TEXT," +
                "发送者 TEXT," +
                "发送时间 TEXT," +
                "已阅读 INTEGER," + //0 为 未读 1 为已读
                "已领取 INTEGER," + // 0 为 未领 1 为 已领
                "PRIMARY KEY(ID AUTOINCREMENT));";
        mailBoxesStmt.executeUpdate(sql);
        mailBoxesConnection.commit();
        return true;
    }

    public static boolean addPlayerMail(String tableName, Mail mail) throws SQLException {
        if (!isTableExist(1, tableName)) return false;
        String sql = "INSERT INTO " + tableName.trim() +
                " (群发ID,主题,内容,附件,发送者,发送时间,已阅读,已领取) VALUES (\""
                + mail.groupID + "\", " + "\"" + mail.theme + "\", " + "\"" + mail.content + "\", "
                + "\"" + mail.attached + "\", " + "\"" + mail.sender + "\", " + "\"" + mail.time + "\", "
                + "0," + "0);";
        mailBoxesStmt.executeUpdate(sql);
        mailBoxesConnection.commit();
        return true;
    }

    public static void removePlayerMail(String tableName, int id) throws SQLException {
        String sql = "DELETE from " + tableName.trim() + " where ID=" + id + ";";
        mailBoxesStmt.executeUpdate(sql);
        mailBoxesConnection.commit();
    }

    public static Mail getPlayerMail(String tableName, int id) throws SQLException {
        if (!isTableExist(1, tableName)) return null;
        ResultSet rs = mailBoxesStmt.executeQuery("SELECT * FROM " + tableName + " WHERE ID=\"" + id + "\";");
        if (rs.isClosed()) return null;
        Mail mailData = new Mail();
        String groupID = rs.getString(2);
        String theme = rs.getString(3);
        String content = rs.getString(4);
        String attached = rs.getString(5);
        String sender = rs.getString(6);
        String sendTime = rs.getString(7);
        boolean isRead = rs.getBoolean(8);
        boolean isAccept = rs.getBoolean(9);
        if (!groupID.isEmpty()) {
            String groupTheme = getSystemRecord("SystemMail", groupID, "主题");
            if (groupTheme == null) {
                removePlayerMail(tableName, id);
                return null;
            }
            if (!isRead) {
                setPlayerMailIsRead(tableName, id);
                addSystemMailReadCount(groupID);
            }
            theme = groupTheme;
            content = getSystemRecord("SystemMail", groupID, "内容");
            attached = getSystemRecord("SystemMail", groupID, "附件");
        }
        mailData.setData(id, groupID, theme, content, attached, sender, sendTime, isRead, isAccept);
        return mailData;
    }

    public static String getSystemRecord(String tableName, String groupID, String value) {
        try {
            ResultSet rs = systemStmt.executeQuery("SELECT " + value + " FROM " + tableName + " WHERE 群发ID=\"" + groupID + "\";");
            return rs.getString(1);
        } catch (SQLException throwables) {
            return null;
        }
    }

    public static void setPlayerMailIsRead(String playerName, int id) throws SQLException {
        mailBoxesStmt.executeUpdate("UPDATE " + playerName.trim() + " SET 已阅读 = 1 WHERE ID=" + id + ";");
        mailBoxesConnection.commit();
    }

    public static void setPlayerMailIsAccept(String playerName, int id) throws SQLException {
        mailBoxesStmt.executeUpdate("UPDATE " + playerName.trim() + " SET 已领取 = 1 WHERE ID=\"" + id + "\";");
        mailBoxesConnection.commit();
    }

    public static void setPlayerMailIsAccept(String playerName, String cdk) throws SQLException {
        mailBoxesStmt.executeUpdate("UPDATE " + playerName.trim() + " SET 已领取 = 1 WHERE 附件=\"" + cdk + "\";");
        mailBoxesConnection.commit();
    }

    public static void addSystemMailAcceptCount(String packageID) throws SQLException {
        ResultSet rs = systemStmt.executeQuery("SELECT * FROM SystemMail WHERE 附件=\"" + packageID + "\";");
        int oldAcceptCount = rs.getInt(10);
        systemStmt.executeUpdate("UPDATE SystemMail SET 领取数 = " + (++oldAcceptCount) + " WHERE 附件=\"" + packageID + "\";");
        systemConnection.commit();
    }

    public static void addSystemMailReadCount(String groupID) throws SQLException {
        String countString = getSystemRecord("SystemMail", groupID, "阅读数");
        if (countString == null) return;
        int oldReadCount = Integer.parseInt(countString);
        systemStmt.executeUpdate("UPDATE SystemMail SET 阅读数 = " + (++oldReadCount) + " WHERE 群发ID=\"" + groupID + "\";");
        systemConnection.commit();
    }

    public static ArrayList<Mail> getPlayerMails(String playerName) throws SQLException {
        ArrayList<Mail> mailList = new ArrayList<>();
        ResultSet rs = mailBoxesStmt.executeQuery("SELECT * FROM " + playerName + ";");
        while (rs.next()) {
            Mail mailData = new Mail();
            int id = rs.getInt(1);
            String groupID = rs.getString(2);
            String theme = rs.getString(3);
            String attached = rs.getString(5);
            String sender = rs.getString(6);
            String sendTime = rs.getString(7);
            boolean isRead = rs.getBoolean(8);
            boolean isAccept = rs.getBoolean(9);
            if (!groupID.isEmpty()) {
                String groupTheme = getSystemRecord("SystemMail", groupID, "主题");
                if (groupTheme == null) {
                    removePlayerMail(playerName, rs.getInt(1));
                    continue;
                }
                theme = groupTheme;
                attached = getSystemRecord("SystemMail", groupID, "附件");
            }
            // 预览不需要内容
            mailData.setData(id, groupID, theme, "", attached, sender, sendTime, isRead, isAccept);
            mailList.add(mailData);
        }
        return mailList;
    }

}
