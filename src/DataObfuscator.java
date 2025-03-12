import java.security.SecureRandom;

public class DataObfuscator {
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
    private static final int PADDING_SIZE = 512;
    private static final String SEPARATOR = ":::";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String prepare(String data) {
        if (data == null || data.isEmpty()) return null;

        String left = generateRandomString();
        String right = generateRandomString();

        return left + SEPARATOR + data.trim() + SEPARATOR + right;
    }

    public static String extract(String data) {
        if (data == null || data.isEmpty()) return null;

        int left = data.indexOf(SEPARATOR);
        int right = data.indexOf(SEPARATOR, left + SEPARATOR.length());

        if (left == -1 || right == -1 || right <= left) return null;

        return data.substring(left + SEPARATOR.length(), right);
    }

    private static String generateRandomString() {
        StringBuilder sb = new StringBuilder(DataObfuscator.PADDING_SIZE);
        for (int i = 0; i < DataObfuscator.PADDING_SIZE; i++) {
            int index = RANDOM.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }
        return sb.toString();
    }
}
