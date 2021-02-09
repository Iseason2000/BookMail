package top.iseason.BookMail.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static LocalDateTime formatDataTimeString(String dataTimeString, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime dateTime = null;
        try {
            dateTime = LocalDateTime.parse(dataTimeString, formatter);
        } catch (DateTimeParseException ignored) {
        }
        return dateTime;
    }

    public static LocalDateTime formatSimpleTimeMinus(String timeString) {
        LocalDateTime dateTime = LocalDateTime.now();
        Matcher s = Pattern.compile("(\\d+?)s").matcher(timeString);
        Matcher m = Pattern.compile("(\\d+?)m").matcher(timeString);
        Matcher h = Pattern.compile("(\\d+?)h").matcher(timeString);
        Matcher d = Pattern.compile("(\\d+?)d").matcher(timeString);
        boolean isShort = false;
        if (s.find()) {
            long second = Long.parseLong(s.group(1));
            dateTime = dateTime.minusSeconds(second);
            isShort = true;
        }
        if (m.find()) {
            long minutes = Long.parseLong(m.group(1));
            dateTime = dateTime.minusMinutes(minutes);
            isShort = true;
        }
        if (h.find()) {
            long hours = Long.parseLong(h.group(1));
            dateTime = dateTime.minusHours(hours);
            isShort = true;
        }
        if (d.find()) {
            long days = Long.parseLong(d.group(1));
            dateTime = dateTime.minusDays(days);
            isShort = true;
        }
        if (isShort) return dateTime;
        return null;
    }
    public static LocalDateTime formatSimpleTimePlus(String timeString) {
        LocalDateTime dateTime = LocalDateTime.now();
        Matcher s = Pattern.compile("(\\d+?)s").matcher(timeString);
        Matcher m = Pattern.compile("(\\d+?)m").matcher(timeString);
        Matcher h = Pattern.compile("(\\d+?)h").matcher(timeString);
        Matcher d = Pattern.compile("(\\d+?)d").matcher(timeString);
        boolean isShort = false;
        if (s.find()) {
            long second = Long.parseLong(s.group(1));
            dateTime = dateTime.plusSeconds(second);
            isShort = true;
        }
        if (m.find()) {
            long minutes = Long.parseLong(m.group(1));
            dateTime = dateTime.plusMinutes(minutes);
            isShort = true;
        }
        if (h.find()) {
            long hours = Long.parseLong(h.group(1));
            dateTime = dateTime.plusHours(hours);
            isShort = true;
        }
        if (d.find()) {
            long days = Long.parseLong(d.group(1));
            dateTime = dateTime.plusDays(days);
            isShort = true;
        }
        if (isShort) return dateTime;
        return null;
    }
}
