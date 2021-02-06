package top.iseason.BookMail.Manager;

import top.iseason.BookMail.Mail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MailManager {
    //发送邮件给n个人，返回发送失败的玩家名字(不存在表或者数据库操作异常)
    public static List<String> sendMailtoPlayers(Mail mail, String... playerNames) {
        List<String> noPlayerMailList = new ArrayList<>();
        for (String name : playerNames) {
            try {
                if(!SqlManager.addPlayerMail(name, mail)){
                    noPlayerMailList.add(name);
                }
            } catch (SQLException throwables) {
                noPlayerMailList.add(name);
                throwables.printStackTrace();
            }
        }
        return noPlayerMailList;
    }
}
