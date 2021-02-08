package top.iseason.BookMail.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Tools {
    public static String getCDK(int length) {
        StringBuilder val = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) { //循环次数等于长度
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            if ("char".equalsIgnoreCase(charOrNum)) {
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val.append((char) (random.nextInt(26) + temp));
            } else {
                val.append(random.nextInt(10));
            }
        }
        return val.toString();
    }

    public static String getDataAndTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
    public static LocalDateTime formatDataTimeString(String dataTimeString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dataTimeString,formatter);
    }
}
