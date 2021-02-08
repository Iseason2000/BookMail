package top.iseason.BookMail.myclass;


import top.iseason.BookMail.Util.Tools;

public class Mail {

    public String groupID;
    public String type;
    public String theme;
    public String content;
    public String attached;
    public String sender;
    public String time;
    public boolean isRead =false;
    public boolean isAccept =false;
    public Mail(){}
    public Mail(String groupID, String theme, String content, String attached, String sender) { //新邮件
        this.groupID = groupID;
        this.theme = theme;
        this.content = content;
        this.attached = attached;
        this.sender = sender;
        time = Tools.getDataAndTime();
    }
    public int ID;
    public void setData(int ID, String groupID, String theme, String content, String attached, String sender,String time, boolean isRead, boolean isAccept) {
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
