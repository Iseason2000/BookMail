package top.iseason.BookMail.myclass;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import top.iseason.BookMail.Manager.SqlManager;
import top.iseason.BookMail.Util.ItemTranslator;
import top.iseason.BookMail.Util.Message;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BookTranslator {
    Boolean isReview;
    ItemStack bookItem;
    String title = "BookMailTitle";
    String author = "BookMailAuthor";
    String NbtString;
    String packageCDK = "";
    List<String> pages;

    public BookTranslator(ItemStack bookItem, Boolean ifReview) {
        this.bookItem = bookItem;
        NbtString = ItemTranslator.itemToNBTString(bookItem);
        isReview = ifReview;
        loadContents();
    }

    public void TranslateContent() {
        NbtString = "{id:\"minecraft:written_book\",tag:{pages:["
                + translatePages() + "],title:\""
                + title + "\",author:\""
                + author + "\",resolved:1b},Count:1b}";
    }

    public String getZipString() {
        return ItemTranslator.zipString(NbtString);
    }

    public String getTitle() {
        return Message.toColor(title);
    }

    public String getAuthor() {
        return author;
    }

    public String getCDK() {
        return packageCDK;
    }

    public ItemStack Build() {
        if (pages.isEmpty()) return null;
        return ItemTranslator.nbtStringToItem(NbtString);
    }

    private void loadContents() {
        pages = new ArrayList<>();
        String pagesPatternString;
        String pagePatternString;
        if (bookItem.getType().name().equals("BOOK_AND_QUILL")) {
            pagesPatternString = "pages:\\[([\\s\\S]*)][\\s\\S]*";
            pagePatternString = "\"([\\s\\S]*?)\"";
        } else {
            pagesPatternString = "pages:\\[([\\s\\S]*)]";
            pagePatternString = "\"text\":\"([\\s\\S]*?)\"";
            BookMeta bookMeta = (BookMeta)bookItem.getItemMeta();
            title = bookMeta.getTitle();
            author = bookMeta.getAuthor();
        }
        Pattern pagesPattern = Pattern.compile(pagesPatternString);
        Matcher pageMatcher = pagesPattern.matcher(NbtString);
        if (!pageMatcher.find()) return;
        Pattern pagePattern = Pattern.compile(pagePatternString);
        Matcher matcher = pagePattern.matcher(pageMatcher.group(1));
        while (matcher.find()) {
            String pageContent = matcher.group(1);
            pages.add(Message.toColor(pageContent));
        }
    }

    private String translatePages() {
        StringBuilder string = new StringBuilder();
        for (int n = 0; n < pages.size(); n++) {
            String pageContent = pages.get(n);
            if (pageContent.equals("")) pageContent = " ";
            string.append("'{\"extra\":[")
                    .append(translatePageContent(pageContent))
                    .append("],\"text\":\"\"}'");
            if (n != pages.size() - 1)
                string.append(",");
        }
        return string.toString();
    }

    private String translatePageContent(String content) {
        //[\[|\{|\(][^\(^\[^\{]+?[\]|\}|\)][\[|\{|\(][^\)^\]^\}]+?[\]|\}|\)]
        String regularString = "[\\[|\\{|\\(][^\\(^\\[^\\{]+?[\\]|\\}|\\)][\\[|\\{|\\(][^\\)^\\]^\\}]+?[\\]|\\}|\\)]";
        String[] parts = splitWithDelimiters(content, regularString);
        StringBuilder newString = new StringBuilder();
        String buildString ;
        int maxIndex = parts.length;
        for (int n = 0; n < maxIndex; n++) {
            String thisPart = parts[n];
            buildString = buildIfHover(thisPart);
            if (tryAppend(buildString, newString, n, maxIndex)) continue;
            buildString = buildIfOpenUrl(thisPart);
            if (tryAppend(buildString, newString, n, maxIndex)) continue;
            buildString = buildIfRunCommand(thisPart);
            if (tryAppend(buildString, newString, n, maxIndex)) continue;
            buildString = buildIfCopyToClipboard(thisPart);
            if (tryAppend(buildString, newString, n, maxIndex)) continue;
            buildString = buildIfPackage(thisPart);
            if (tryAppend(buildString, newString, n, maxIndex)) continue;
            buildString = "{\"text\":\"" + thisPart + "\"}";
            tryAppend(buildString, newString, n, maxIndex);
        }
        return newString.toString();
    }

    private static Boolean tryAppend(String buildString, StringBuilder target, int index, int maxIndex) {
        if (buildString != null) {
            target.append(buildString);
            if (index != maxIndex - 1)
                target.append(",");
            return true;
        }
        return false;
    }

    private static String buildIfHover(String str) {
        //\{([\s\S]*?)\}\(([\s\S]*?)\)
        Pattern pattern = Pattern.compile("\\{([\\s\\S]*?)}\\(([\\s\\S]*?)\\)");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find())
            return "{\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\""
                    + matcher.group(2)
                    + "\"}},\"text\": \""
                    + matcher.group(1) + "\"}";
        return null;
    }

    private static String buildIfOpenUrl(String str) {
        //\[([\s\S]*?)\]\(([\s\S]*?)\)
        Pattern pattern = Pattern.compile("\\[([\\s\\S]*?)]\\(([\\s\\S]*?)\\)");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find())
            return "{\"clickEvent\":{\"action\":\"open_url\",\"value\":\""
                    + matcher.group(2)
                    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"打开网址："
                    + matcher.group(2)
                    + "\"}},\"text\": \""
                    + matcher.group(1) + "\"}";
        return null;
    }

    private static String buildIfRunCommand(String str) {
        //\[([\s\S]*?)\]\[([\s\S]*?)\]
        Pattern pattern = Pattern.compile("\\[([\\s\\S]*?)]\\[([\\s\\S]*?)]");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find())
            return "{\"clickEvent\":{\"action\":\"run_command\",\"value\":\""
                    + matcher.group(2)
                    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"点击运行："
                    + matcher.group(2)
                    + "\"}},\"text\": \""
                    + matcher.group(1) + "\"}";
        return null;
    }

    private static String buildIfCopyToClipboard(String str) {
        //\[([\s\S]*?)\]\{([\s\S]*?)\}
        Pattern pattern = Pattern.compile("\\[([\\s\\S]*?)]\\{([\\s\\S]*?)}");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find())
            return "{\"clickEvent\":{\"action\":\"copy_to_clipboard\",\"value\":\""
                    + matcher.group(2)
                    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"点击复制："
                    + matcher.group(2)
                    + "\"}},\"text\": \""
                    + matcher.group(1) + "\"}";
        return null;
    }

    private String buildIfPackage(String str) {
        //\(([\s\S]*?)\)\{([\s\S]*?)\}
        Pattern pattern = Pattern.compile("\\(([\\s\\S]*?)\\)\\{([\\s\\S]*?)}");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            String cdk = matcher.group(2);
            try {
                if (!SqlManager.isRecordExist(0,"PackageList","包裹ID",cdk)) return "{\"text\": \""+str+"\"}";
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            packageCDK = cdk;
            if (isReview)
                return "{\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"点击领取\"}},\"text\": \""
                        + matcher.group(1) + "\"}";
            return "{\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/bookmail package get "
                    + matcher.group(2)
                    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"点击领取\"}},\"text\": \""
                    + matcher.group(1) + "\"}";
        }
        return null;
    }

    private static String[] splitWithDelimiters(String str, String regex) {
        List<String> parts = new ArrayList<>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        int lastEnd = 0;
        while (m.find()) {
            int start = m.start();
            if (lastEnd != start) {
                String nonDelim = str.substring(lastEnd, start);
                parts.add(nonDelim);
            }
            String delim = m.group();
            parts.add(delim);
            lastEnd = m.end();
        }
        if (lastEnd != str.length()) {
            String nonDelim = str.substring(lastEnd);
            parts.add(nonDelim);
        }
        return parts.toArray(new String[]{});
    }

}
