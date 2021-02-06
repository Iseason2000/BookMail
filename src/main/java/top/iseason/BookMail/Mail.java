package top.iseason.BookMail;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Mail {
    public int ID;
    public int groupID;
    public String them;
    public String content;
    public String attached;
    public String sender;
    public String time;
    public boolean isRead;
    public boolean isAccept;

    public Mail newMail(int groupID, String them, String content, String attached, String sender) { //新邮件
        this.groupID = groupID;
        this.them = them;
        this.content = content;
        this.attached = attached;
        this.sender = sender;
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        time = dateTime.format(formatter);
        return this;
    }

    public void setData(int ID, int groupID, String them, String content, String attached, String sender,String time, boolean isRead, boolean isAccept) {
        this.ID = ID;
        this.groupID = groupID;
        this.them = them;
        this.content = content;
        this.attached = attached;
        this.sender = sender;
        this.time = time;
        this.isRead = isRead;
        this.isAccept = isAccept;
    }
}
