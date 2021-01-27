package top.iseason.MailSystem.Util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MailData {
    public int groupID;
    public String them;
    public String content;
    public String attached;
    public String sender;
    public String time;

    public MailData(int groupID, String them, String content, String attached, String sender) {
        this.groupID = groupID;
        this.them = them;
        this.content = content;
        this.attached = attached;
        this.sender = sender;
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        time = dateTime.format(formatter);
    }
}
