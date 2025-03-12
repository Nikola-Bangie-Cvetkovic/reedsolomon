import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class ReedSolomon {
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
    private static final String SEPARATOR = "||";
    private final int n;
    private final int k;
    private final int s;
    private final int[][] generatorMatrix;

    private String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }
        return sb.toString();
    }

    public ReedSolomon(int n, int k, int s) {
        if (n > 255 || k >= n) throw new IllegalArgumentException("Invalid n/k");
        this.n = n;
        this.k = k;
        this.s = s;
        this.generatorMatrix = buildGeneratorMatrix();
    }

    public ReedSolomon(int n, int k) {
        if (n > 255 || k >= n) throw new IllegalArgumentException("Invalid n/k");
        this.n = n;
        this.k = k;
        this.s = 256;
        this.generatorMatrix = buildGeneratorMatrix();
    }

    private int[][] buildGeneratorMatrix() {
        int[][] matrix = new int[n][k];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < k; col++) {
                matrix[row][col] = Galois.exp((row * col) % 255);
            }
        }
        return matrix;
    }

    public String prepare(String value) {
        if (value == null || value.isEmpty()) return null;

        String leftPadding = generateRandomString(512);
        String rightPadding = generateRandomString(512);

        return leftPadding + SEPARATOR + value.trim() + SEPARATOR + rightPadding;
    }

    public Map<Integer, byte[]> encode(byte[] data) {
        int totalLength = k * s;
        byte[] padded = new byte[totalLength];
        System.arraycopy(data, 0, padded, 0, Math.min(data.length, totalLength));

        byte[][] dataShards = new byte[k][s];
        for (int i = 0; i < k; i++) {
            System.arraycopy(padded, i * s, dataShards[i], 0, s);
        }

        Map<Integer, byte[]> allShards = new HashMap<>();
        for (int i = 0; i < n; i++) {
            byte[] shard = new byte[s];
            for (int j = 0; j < k; j++) {
                for (int b = 0; b < s; b++) {
                    shard[b] ^= (byte) Galois.mul(generatorMatrix[i][j], dataShards[j][b]);
                }
            }
            allShards.put(i, shard);
        }

        return allShards;
    }

    public String extract(String value) {
        int first = value.indexOf(SEPARATOR);
        int second = value.indexOf(SEPARATOR, first + SEPARATOR.length());
        if (first == -1 || second == -1) return null;

        return value.substring(first + SEPARATOR.length(), second);
    }

    public byte[] decode(Map<Integer, byte[]> shardMap) {
        if (shardMap.size() < k) throw new IllegalArgumentException("Need at least k shards");

        int[] indexes = shardMap.keySet().stream().mapToInt(i -> i).sorted().limit(k).toArray();
        byte[][] matrix = new byte[k][k];
        byte[][] inputShards = new byte[k][s];

        for (int i = 0; i < k; i++) {
            int idx = indexes[i];
            byte[] shard = shardMap.get(idx);
            if (shard == null || shard.length != s) throw new IllegalArgumentException("Invalid shard");
            inputShards[i] = shard;
            for (int j = 0; j < k; j++) {
                matrix[i][j] = (byte) generatorMatrix[idx][j];
            }
        }

        byte[][] inverse = invertMatrix(matrix);
        byte[][] dataShards = new byte[k][s];

        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                for (int b = 0; b < s; b++) {
                    dataShards[i][b] ^= (byte) Galois.mul(inverse[i][j] & 0xFF, inputShards[j][b] & 0xFF);
                }
            }
        }

        byte[] output = new byte[k * s];
        for (int i = 0; i < k; i++) {
            System.arraycopy(dataShards[i], 0, output, i * s, s);
        }

        return output;
    }

    private byte[][] invertMatrix(byte[][] matrix) {
        int size = matrix.length;
        byte[][] augmented = new byte[size][size * 2];

        for (int i = 0; i < size; i++) {
            System.arraycopy(matrix[i], 0, augmented[i], 0, size);
            augmented[i][i + size] = 1;
        }

        for (int i = 0; i < size; i++) {
            int inv = Galois.inverse(augmented[i][i] & 0xFF);
            for (int j = 0; j < size * 2; j++) {
                augmented[i][j] = (byte) Galois.mul(augmented[i][j] & 0xFF, inv);
            }
            for (int k = 0; k < size; k++) {
                if (k == i) continue;
                int factor = augmented[k][i] & 0xFF;
                for (int j = 0; j < size * 2; j++) {
                    augmented[k][j] ^= Galois.mul(factor, augmented[i][j] & 0xFF);
                }
            }
        }

        byte[][] result = new byte[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(augmented[i], size, result[i], 0, size);
        }

        return result;
    }
}
