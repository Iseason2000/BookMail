package top.iseason.BookMail.Manager;

import org.bukkit.ChatColor;
import top.iseason.BookMail.BookMailPlugin;
import top.iseason.BookMail.myclass.Mail;
import top.iseason.BookMail.Util.Tools;

import java.io.File;
import java.sql.*;

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
        createSystemMailTable("SystemMail");
        createSystemPlayerLoginTimeTable();
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
        ResultSet set = null;
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

    public static String getPlayerLoginTime(String name) throws SQLException {
        ResultSet rs = systemStmt.executeQuery("SELECT * FROM LoginTime WHERE 玩家名称=\"" + name.trim() + "\";");
        return rs.getString(2);
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

    public static String getPackageZipString(String cdk) throws SQLException {
        if (!isRecordExist(0, "PackageList", "包裹ID", cdk)) return null;
        ResultSet rs = systemStmt.executeQuery("SELECT * FROM PackageList WHERE 包裹ID=\"" + cdk + "\";");
        int count = rs.getInt(3);
        String zipString = rs.getString(2);
        if (count == 1) {
            removePackage(cdk);
        } else if (count > 1) {
            systemStmt.executeUpdate("UPDATE PackageList SET 数量 = " + (count - 1) + " WHERE 包裹ID=\"" + cdk + "\";");
            systemConnection.commit();
        }
        //todo 只有收到邮件的才可以领取，包裹有数量限制
        return zipString;
    }

    //
//    public static Boolean isPackageExist(String cdk) throws SQLException {
//        String sql = "SELECT COUNT(*) FROM PackageList where 包裹ID=\"" + cdk + "\";";
//        ResultSet resultSet = systemStmt.executeQuery(sql);
//        return resultSet.getInt(1) == 1;
//    }
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

    private static void createSystemMailTable(String tableName) throws SQLException {
        if (isTableExist(0, tableName)) return;
        String sql = "CREATE TABLE " + tableName.trim() +
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

    public static void addSystemMail(Mail mail) throws SQLException {
        String sql = "INSERT INTO SystemMail" +
                " (群发ID,类型,主题,内容,附件,发送者,发送时间,阅读数,领取数) VALUES (\""
                + mail.groupID + "\", " + "\"" + mail.type + "\", " + "\"" + mail.theme + "\", "
                + "\"" + mail.content + "\", " + "\"" + mail.attached + "\", " + "\"" + mail.sender
                + "\", " + "\"" + mail.time + "\", " + "0," + "0);";
        systemStmt.executeUpdate(sql);
        systemConnection.commit();
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


    public static void removeTable(int database, String tableName) throws SQLException {
        String sql = "DROP TABLE IF EXISTS " + tableName.trim();
        if (database == 0) {
            systemStmt.executeUpdate(sql);
            systemConnection.commit();
        } else {
            mailBoxesStmt.executeUpdate(sql);
            mailBoxesConnection.commit();
        }
    }

    public static void removePlayerMail(String tableName, int id) throws SQLException {
        String sql = "DELETE from COMPANY where ID=" + id + ";";
        mailBoxesStmt.executeUpdate(sql);
        mailBoxesConnection.commit();
    }

    public static Mail getPlayerMail(String tableName, int id) throws SQLException {
        if (!isTableExist(1, tableName)) return null;
        ResultSet rs = mailBoxesStmt.executeQuery("SELECT * FROM " + tableName + " WHERE ID=\"" + id + "\";");
        Mail mailData = new Mail();
        mailData.setData(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), Boolean.parseBoolean(rs.getString(8)), Boolean.parseBoolean(rs.getString(9)));
        return mailData;
    }

}
