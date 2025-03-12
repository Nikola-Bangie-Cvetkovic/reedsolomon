import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        int n = 10;
        int k = 7;
        ReedSolomon reedSolomon = new ReedSolomon(n, k);

        String stringValue = "This is test text!";
        String preparedStringValue = DataObfuscator.prepare(stringValue);

        Map<Integer, byte[]> chunks = reedSolomon.encode(preparedStringValue.getBytes(StandardCharsets.UTF_8));

        byte[] decodedChunks = reedSolomon.decode(chunks);

        String decodedString = new String(decodedChunks, StandardCharsets.UTF_8);

        String extractedDecodedString = DataObfuscator.extract(decodedString);
        System.out.println(extractedDecodedString);
    }
}