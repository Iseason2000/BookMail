package top.iseason.MailSystem.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MailData {
    public int groupID;
    public String them;
    public String content;
    public String attached;
    public String sender;
    public String time;
    public boolean isRead = false;
    public boolean isReceive = false;

    public MailData(int groupID, String them, String content, String attached, String sender) {
        this.groupID = groupID;
        this.them = them;
        this.content = content;
        this.attached = attached;
        this.sender = sender;
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = formatter.format(date);
    }
}
