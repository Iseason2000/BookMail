package top.iseason.MailSystem.Manager;

import org.bukkit.ChatColor;
import top.iseason.MailSystem.MaiLPlugin;
import top.iseason.MailSystem.Util.MailData;

import java.io.File;
import java.sql.*;

import static top.iseason.MailSystem.Util.LogSender.sendLog;

public class DataManager {
    private static Connection c = null;
    private static Statement stmt = null;

    public static void initSqilte() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        File dataBaseFile = new File(MaiLPlugin.getInstance().getDataFolder(), "mailboxes.db");
        if (!dataBaseFile.exists())
            sendLog(ChatColor.YELLOW + "未找到数据库，已创建新数据库!");
        c = DriverManager.getConnection("jdbc:sqlite:" + dataBaseFile.getAbsolutePath().replace("\\", "/"));
        stmt = c.createStatement();
        c.setAutoCommit(false);
    }

    public static void disableSqilte() throws SQLException {
        stmt.close();
        c.close();
    }

    public static boolean isTableExist(String tableName) throws SQLException {
        boolean flag = false;
        String sql = "SELECT COUNT(*) FROM sqlite_master where type='table' and name='" + tableName.trim() + "'";
        ResultSet set = stmt.executeQuery(sql);
        int count = set.getInt(1);
        if (count == 1) flag = true;
        return flag;
    }

    public static boolean createPlayerMailBoxTable(String tableName) throws SQLException { //不区分大小写
        if (isTableExist(tableName)) return false;
        String sql = "CREATE TABLE " + tableName.trim() +
                "(ID INTEGER NOT NULL," +
                "群发ID INTEGER," +
                "主题 TEXT," +
                "内容 TEXT," +
                "附件 TEXT," +
                "发送者 TEXT," +
                "发送时间 TEXT," +
                "已阅读 INTEGER," + //0 为 未读 1 为已读
                "已领取 INTEGER," + // 0 为 未领 1 为 已领
                "PRIMARY KEY(ID AUTOINCREMENT));";
        stmt.executeUpdate(sql);
        c.commit();
        return true;
    }

    public static boolean addPlayerMail(String tableName, MailData mail) throws SQLException {
        if (!isTableExist(tableName)) return false;
        String sql = "INSERT INTO " + tableName.trim() +
                " (群发ID,主题,内容,附件,发送者,发送时间,已阅读,已领取) VALUES ("
                + mail.groupID + ", " + "\"" + mail.them + "\", " + "\"" + mail.content + "\", "
                + "\"" + mail.attached + "\", " + "\"" + mail.sender + "\", " + "\"" + mail.time + "\", "
                + "0," + "0);";
        stmt.executeUpdate(sql);
        c.commit();
        return true;
    }

    public static void removeTable(String tableName) throws SQLException {
        String sql = "DROP TABLE IF EXISTS " + tableName.trim();
        stmt.executeUpdate(sql);
        c.commit();

    }

}
