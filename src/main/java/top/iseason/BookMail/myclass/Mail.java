package top.iseason.BookMail.myclass;


import top.iseason.BookMail.Util.Tools;

public class Mail {
    public int ID;
    public int groupID;
    public String theme;
    public String content;
    public String attached;
    public String sender;
    public String time;
    public boolean isRead;
    public boolean isAccept;
    public Mail(){}
    public Mail(int groupID, String theme, String content, String attached, String sender) { //新邮件
        this.groupID = groupID;
        this.theme = theme;
        this.content = content;
        this.attached = attached;
        this.sender = sender;
        time = Tools.getDataAndTime();
    }

    public void setData(int ID, int groupID, String theme, String content, String attached, String sender,String time, boolean isRead, boolean isAccept) {
        this.ID = ID;
        this.groupID = groupID;
        this.theme = theme;
        this.content = content;
        this.attached = attached;
        this.sender = sender;
        this.time = time;
        this.isRead = isRead;
        this.isAccept = isAccept;
    }
}
