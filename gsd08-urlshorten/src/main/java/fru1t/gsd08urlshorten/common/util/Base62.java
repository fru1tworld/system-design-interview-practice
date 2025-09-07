package fru1t.gsd08urlshorten.common.util;

public class Base62 {
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62_CHARS.length();

    public String encode(long input) {
        StringBuilder result = new StringBuilder();
        long num = input;
        
        while (num > 0) {
            result.insert(0, BASE62_CHARS.charAt((int)(num % BASE)));
            num /= BASE;
        }
        
        return !result.isEmpty() ? result.toString() : "0";
    }
}
