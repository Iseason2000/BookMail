package top.iseason.MailSystem.Util;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookTranslator {
    ItemStack bookItem;
    String title = "BookMailTitle";
    String auther = "BookMailAuther";
    String oldNbtString;
    String newNbtString;
    List<String> pages;

    public BookTranslator(ItemStack bookItem) {
        this.bookItem = bookItem;
        oldNbtString = ItemTranslator.itemToNBTString(bookItem);
        System.out.println(oldNbtString);
        loadContents();
    }

    public void loadContents() {
        pages = new ArrayList<>();
        String pagesPatternString = null;
        String pagePatternString = null;
        if (bookItem.getType().name().equals("BOOK_AND_QUILL")) {
            pagesPatternString = "pages:\\[(.*)].*";
            pagePatternString = "\"(.*?)\"";
        } else {
            pagesPatternString = "pages:\\[(.*)]";
            pagePatternString = "\"text\":\"(.*?)\"";
            System.out.println(oldNbtString);
            Matcher titleMatcher = Pattern.compile("title:\"(.*)\",author").matcher(oldNbtString);
            Matcher autherMatcher = Pattern.compile("author:\"(.*)\".*").matcher(oldNbtString);
            if (titleMatcher.find())
                title = titleMatcher.group(1);
            if (autherMatcher.find())
                auther = autherMatcher.group(1);
        }
        Pattern pagesPattern = Pattern.compile(pagesPatternString);
        Matcher pageMatcher = pagesPattern.matcher(oldNbtString);
        if (!pageMatcher.find()) return;
        Pattern pagePattern = Pattern.compile(pagePatternString);
        Matcher matcher = pagePattern.matcher(pageMatcher.group(1));
        while (matcher.find()) {
            String pageContent = matcher.group(1);
            pages.add(toColor(pageContent));
        }
    }

    public String translatePages() {
        StringBuilder string = new StringBuilder();
        for (int n = 0; n < pages.size() - 1; n++) {
            String str = "'{\"extra\":[{\"text\":\""
                    + pages.get(n)
                    + "\"}],\"text\":\"\"}',";
            string.append(str);
        }
        string.append("'{\"extra\":[{\"text\":\"")
                .append(pages.get(pages.size() - 1))
                .append("\"}],\"text\":\"\"}'");
        return string.toString();
    }

    public ItemStack Build() {
        if (pages.isEmpty()) return null;
        newNbtString = "{id:\"minecraft:written_book\",tag:{pages:["
                + translatePages() + "],title:\""
                + title + "\",author:\""
                + auther + "\",resolved:1b},Count:1b}";
        return ItemTranslator.nbtStringToItem(newNbtString);
    }

    public static String toColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
