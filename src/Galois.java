public class Galois {
    private static final int[] EXP_TABLE = new int[512];
    private static final int[] LOG_TABLE = new int[256];

    static {
        int x = 1;
        for (int i = 0; i < 255; i++) {
            EXP_TABLE[i] = x;
            LOG_TABLE[x] = i;
            x <<= 1;
            if (x >= 256) {
                x ^= 0x11D;
            }
        }
        for (int i = 255; i < 512; i++) {
            EXP_TABLE[i] = EXP_TABLE[i - 255];
        }
    }

    public static int mul(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return EXP_TABLE[LOG_TABLE[a] + LOG_TABLE[b]];
    }

    public static int div(int a, int b) {
        if (b == 0) throw new IllegalArgumentException("Division by zero");
        if (a == 0) return 0;
        return EXP_TABLE[(LOG_TABLE[a] + 255 - LOG_TABLE[b]) % 255];
    }

    public static int exp(int a) {
        return EXP_TABLE[a];
    }

    public static int log(int a) {
        if (a == 0) throw new IllegalArgumentException("Log of zero");
        return LOG_TABLE[a];
    }

    public static int inverse(int a) {
        if (a == 0) throw new IllegalArgumentException("No inverse for zero");
        return EXP_TABLE[255 - LOG_TABLE[a]];
    }
}
