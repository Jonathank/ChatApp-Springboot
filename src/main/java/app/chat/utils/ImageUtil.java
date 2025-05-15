package app.chat.utils;

import java.util.Base64;

public class ImageUtil {

    public static String byteArrayToBase64(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(imageData);
    }

    public static byte[] base64ToByteArray(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 string", e);
        }
    }
}