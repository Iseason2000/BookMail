package top.iseason.MailSystem.Util;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BookTranslator {
    ItemStack bookItem;
    String title = "BookMailTitle";
    String author = "BookMailAuthor";
    String NbtString;
    List<String> pages;

    public BookTranslator(ItemStack bookItem) {
        this.bookItem = bookItem;
        NbtString = ItemTranslator.itemToNBTString(bookItem);
        loadContents();
    }
    public void playerTranslate() {
        NbtString = "{id:\"minecraft:written_book\",tag:{pages:["
                + translatePages() + "],title:\""
                + title + "\",author:\""
                + author + "\",resolved:1b},Count:1b}";
    }
    public ItemStack Build() {
        if (pages.isEmpty()) return null;
        return ItemTranslator.nbtStringToItem(NbtString);
    }
    private void loadContents() {
        pages = new ArrayList<>();
        String pagesPatternString = null;
        String pagePatternString = null;
        if (bookItem.getType().name().equals("BOOK_AND_QUILL")) {
            pagesPatternString = "pages:\\[([\\s\\S]*)][\\s\\S]*";
            pagePatternString = "\"([\\s\\S]*?)\"";
        } else {
            pagesPatternString = "pages:\\[([\\s\\S]*)]";
            pagePatternString = "\"text\":\"([\\s\\S]*?)\"";
            Matcher titleMatcher = Pattern.compile("title:\"(.*)\",author").matcher(NbtString);
            Matcher authorMatcher = Pattern.compile("author:\"(.*)\".*").matcher(NbtString);
            if (titleMatcher.find())
                title = titleMatcher.group(1);
            if (authorMatcher.find())
                author = authorMatcher.group(1);
        }
        Pattern pagesPattern = Pattern.compile(pagesPatternString);
        Matcher pageMatcher = pagesPattern.matcher(NbtString);
        if (!pageMatcher.find()) return;
        Pattern pagePattern = Pattern.compile(pagePatternString);
        Matcher matcher = pagePattern.matcher(pageMatcher.group(1));
        while (matcher.find()) {
            String pageContent = matcher.group(1);
            pages.add(toColor(pageContent));
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
        String buildString = null;
        int maxindex = parts.length;
        for (int n = 0; n < maxindex; n++) {
            String thisPart = parts[n];
            buildString = buildIfHover(thisPart);
            if (tryAppend(buildString, newString, n, maxindex)) continue;
            buildString = buildIfOpenUrl(thisPart);
            if (tryAppend(buildString, newString, n, maxindex)) continue;
            buildString = buildIfRunCommand(thisPart);
            if (tryAppend(buildString, newString, n, maxindex)) continue;
            buildString = buildIfCopyToClipboard(thisPart);
            if (tryAppend(buildString, newString, n, maxindex)) continue;
            buildString = "{\"text\":\"" + thisPart + "\"}";
            tryAppend(buildString, newString, n, maxindex);
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

    private static String toColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
