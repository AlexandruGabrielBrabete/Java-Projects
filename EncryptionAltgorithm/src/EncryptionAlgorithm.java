import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class EncryptionAlgorithm {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Scanner scanner = new Scanner(System.in);
        SecureRandom random = new SecureRandom();

        // User inputs
        System.out.print("Enter string A (text to encrypt): ");
        String inputText = scanner.nextLine();

        System.out.print("Enter string B (encryption key): ");
        String encryptionKey = scanner.nextLine();

        System.out.print("Case-sensitive? (yes/no): ");
        boolean caseSensitive = scanner.nextLine().trim().equalsIgnoreCase("yes");

        System.out.print("Choose encryption mode (basic/weighted/chaotic): ");
        String mode = scanner.nextLine().trim().toLowerCase();

        // Generate a random salt
        int salt = random.nextInt(500) + 100;

        // Create config object
        EncryptionConfig config = new EncryptionConfig(caseSensitive, mode, salt, true);

        // Perform encryption
        EncryptionEngine engine = new EncryptionEngine(config);
        EncryptionResult result = engine.encrypt(inputText, encryptionKey);

        // Output
        result.displayReport();
    }
}

// Configuration class
class EncryptionConfig {
    boolean caseSensitive;
    String mode;
    int salt;
    boolean debug;

    public EncryptionConfig(boolean caseSensitive, String mode, int salt, boolean debug) {
        this.caseSensitive = caseSensitive;
        this.mode = mode;
        this.salt = salt;
        this.debug = debug;
    }
}

// Result class
class EncryptionResult {
    int finalSum;
    String checksum;
    String validationCode;
    List<String> logSteps = new ArrayList<>();

    public void log(String step) {
        logSteps.add(step);
    }

    public void displayReport() {
        System.out.println("\n=== CRYPTOGRAPHIC REPORT ===");
        logSteps.forEach(System.out::println);
        System.out.println("Final sum: " + finalSum);
        System.out.println("Validation code: " + validationCode);
        System.out.println("Checksum (SHA-256): " + checksum);
        System.out.println("=============================");
    }
}

// Encryption engine
class EncryptionEngine {
    private final EncryptionConfig config;
    private final SecureRandom random = new SecureRandom();

    public EncryptionEngine(EncryptionConfig config) {
        this.config = config;
    }

    public EncryptionResult encrypt(String input, String key) throws NoSuchAlgorithmException {
        EncryptionResult result = new EncryptionResult();

        // Step 1: Normalize
        if (!config.caseSensitive) {
            input = input.toLowerCase();
            key = key.toLowerCase();
        }
        result.log("Normalized input and key based on case sensitivity.");

        // Step 2: Expand Key
        List<Character> expandedKey = expandKey(key, input.length());
        result.log("Expanded key to match input length.");

        // Step 3: Assign values
        Map<Character, List<Integer>> valueMap = new HashMap<>();
        for (int i = 0; i < expandedKey.size(); i++) {
            char ch = expandedKey.get(i);
            int value = generateValue(i, ch);
            valueMap.computeIfAbsent(ch, k -> new ArrayList<>()).add(value);
        }
        result.log("Assigned values to expanded key characters.");

        // Step 4: Rotate values
        rotateValues(valueMap, config.salt % 5);
        result.log("Rotated value lists.");

        // Step 5: Compute weighted sum
        int sum = 0;
        Map<Character, Integer> frequency = computeFrequencies(input);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (valueMap.containsKey(ch)) {
                List<Integer> values = valueMap.get(ch);
                int val = values.get(i % values.size());
                int freq = frequency.getOrDefault(ch, 1);
                switch (config.mode) {
                    case "basic":
                        sum += val;
                        break;
                    case "weighted":
                        sum += val + (i + 1) * freq;
                        break;
                    case "chaotic":
                        sum += val * (i + 1) + config.salt % (i + 2);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported mode: " + config.mode);
                }
            }
        }
        result.log("Computed complex weighted sum: " + sum);

        // Step 6: Add salt
        sum += config.salt;

        // Step 7: Final validations and hash
        result.finalSum = sum;
        result.checksum = computeSHA256(sum + key + config.mode);
        result.validationCode = computeSHA256("VALIDATE" + key + sum).substring(0, 12);
        result.log("Generated checksum and validation code.");

        return result;
    }

    private List<Character> expandKey(String baseKey, int targetLength) {
        List<Character> expanded = new ArrayList<>();
        while (expanded.size() < targetLength) {
            for (char c : baseKey.toCharArray()) {
                expanded.add(c);
                if (expanded.size() >= targetLength) break;
            }
        }
        return expanded;
    }

    private void rotateValues(Map<Character, List<Integer>> map, int steps) {
        for (Map.Entry<Character, List<Integer>> entry : map.entrySet()) {
            List<Integer> list = entry.getValue();
            Collections.rotate(list, steps % list.size());
        }
    }

    private int generateValue(int position, char ch) {
        int base = random.nextInt(100) + 1;
        return base * (ch % 5 + 1) + (position + 1);
    }

    private Map<Character, Integer> computeFrequencies(String input) {
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : input.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }
        return freqMap;
    }

    private String computeSHA256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes());

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String hexVal = Integer.toHexString(0xff & b);
            if (hexVal.length() == 1) hex.append('0');
            hex.append(hexVal);
        }
        return hex.toString();
    }
}
