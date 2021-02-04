package top.iseason.MailSystem.Util;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;

import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ItemTranslator {
    public static String itemToString(ItemStack item) {
        NBTCompound itemData = NBTItem.convertItemtoNBT(item);
        return zipString(itemData.toString());
    }
    public static String itemToNBTString(ItemStack item) {
        NBTCompound itemData = NBTItem.convertItemtoNBT(item);
        return itemData.toString();
    }

    public static String itemListToString(ArrayList<ItemStack> itemList) {
        StringBuilder data = new StringBuilder(";");
        for (ItemStack item : itemList) {
            if (item != null) {
                NBTCompound itemData = NBTItem.convertItemtoNBT(item);
                String nbtData = itemData.toString();
                data.append(nbtData).append(";");
            }
        }
        return zipString(data.toString());
    }

    public static ItemStack nbtStringToItem(String nbtString) {
        NBTContainer cont = new NBTContainer(nbtString);
        return NBTItem.convertNBTtoItem(cont);
    }

    public static ItemStack zipStringToItem(String zipString) {
        String data = unzipString(zipString);
        return nbtStringToItem(data);
    }

    public static ArrayList<ItemStack> stringToItemList(String zipString) {
        String data = unzipString(zipString);
        Pattern pattern = Pattern.compile("(?<=;).*?(?=;)");
        Matcher matcher = pattern.matcher(data);
        ArrayList<ItemStack> itemList = new ArrayList<>();
        while (matcher.find()) {
            String nbtData = matcher.group(0);
            itemList.add(nbtStringToItem(nbtData));
        }
        return itemList;
    }

    public static String zipString(String unzipString) {
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        deflater.setInput(unzipString.getBytes(StandardCharsets.UTF_8));
        deflater.finish();
        final byte[] bytes = new byte[512];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(512);
        while (!deflater.finished()) {
            int length = deflater.deflate(bytes);
            outputStream.write(bytes, 0, length);
        }
        deflater.end();
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public static String unzipString(String zipString) {
        byte[] decode = Base64.getDecoder().decode(zipString);
        Inflater inflater = new Inflater();
        inflater.setInput(decode);
        final byte[] bytes = new byte[512];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(512);
        try {
            while (!inflater.finished()) {
                int length = inflater.inflate(bytes);
                outputStream.write(bytes, 0, length);
            }
        } catch (DataFormatException e) {
            e.printStackTrace();
            return null;
        } finally {
            inflater.end();
        }
        return outputStream.toString();
    }


}
