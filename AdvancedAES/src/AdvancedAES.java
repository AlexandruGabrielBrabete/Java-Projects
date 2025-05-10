import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.Base64; // For Base64 encoding/decoding in Java 9+

public class AdvancedAES {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256"; // PBKDF2 for key derivation
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int KEY_SIZE = 256; // AES 256-bit key
    private static final int IV_SIZE = 12; // 12 bytes for AES-GCM IV
    private static final int TAG_SIZE = 128; // 128-bit authentication tag
    private static final int SALT_SIZE = 16; // Salt size for KDF
    private static final int PBKDF2_ITERATIONS = 10000; // Number of iterations for PBKDF2

    public static void main(String[] args) throws Exception {
        String password = "superSecurePassword";
        String plaintext = "This is a complex AES encryption example that uses AES-GCM and PBKDF2!";

        // 1. Derive a key from the password using PBKDF2
        SecretKey key = deriveKeyUsingPBKDF2(password);

        // 2. Encrypt the plaintext
        String encryptedData = encrypt(plaintext, key);
        System.out.println("Encrypted Data: " + encryptedData);

        // 3. Decrypt the ciphertext
        String decryptedData = decrypt(encryptedData, key);
        System.out.println("Decrypted Data: " + decryptedData);
    }

    // 1. Derive a key from the password using PBKDF2
    public static SecretKey deriveKeyUsingPBKDF2(String password) throws Exception {
        // Generate random salt
        byte[] salt = new byte[SALT_SIZE];
        new SecureRandom().nextBytes(salt);

        // Initialize PBKDF2 with HMACSHA256
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KDF_ALGORITHM);
        byte[] derivedKey = factory.generateSecret(spec).getEncoded();

        // Return the AES key from PBKDF2-derived key material
        return new SecretKeySpec(derivedKey, ALGORITHM);
    }

    // 2. Encrypt the plaintext using AES-GCM
    public static String encrypt(String plaintext, SecretKey key) throws Exception {
        // Generate a random IV (Initialization Vector)
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        // Initialize the AES-GCM Cipher
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        // Encrypt the plaintext
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Generate HMAC for integrity
        byte[] hmac = generateHMAC(iv, ciphertext);

        // Combine IV, ciphertext, and HMAC into one array for transmission
        byte[] encryptedData = new byte[IV_SIZE + ciphertext.length + hmac.length];
        System.arraycopy(iv, 0, encryptedData, 0, IV_SIZE);
        System.arraycopy(ciphertext, 0, encryptedData, IV_SIZE, ciphertext.length);
        System.arraycopy(hmac, 0, encryptedData, IV_SIZE + ciphertext.length, hmac.length);

        // Return Base64 encoded encrypted data
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // 3. Decrypt the ciphertext using AES-GCM with integrity check via HMAC
    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        // Decode the encrypted data from Base64
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

        // Extract the IV (first 12 bytes)
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(encryptedBytes, 0, iv, 0, IV_SIZE);

        // Extract the ciphertext (everything between IV and HMAC)
        int ciphertextLength = encryptedBytes.length - IV_SIZE - 32; // 32 bytes for HMAC
        byte[] ciphertext = new byte[ciphertextLength];
        System.arraycopy(encryptedBytes, IV_SIZE, ciphertext, 0, ciphertextLength);

        // Extract the HMAC (last 32 bytes)
        byte[] hmac = new byte[32];
        System.arraycopy(encryptedBytes, IV_SIZE + ciphertextLength, hmac, 0, 32);

        // Verify the HMAC for integrity
        if (!verifyHMAC(iv, ciphertext, hmac)) {
            throw new SecurityException("Integrity check failed: HMAC mismatch.");
        }

        // Initialize AES-GCM for decryption
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        // Decrypt the ciphertext
        byte[] decryptedBytes = cipher.doFinal(ciphertext);

        // Return the plaintext
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // 4. Generates HMAC for integrity verification
    private static byte[] generateHMAC(byte[] iv, byte[] ciphertext) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(iv, HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        mac.update(ciphertext);
        return mac.doFinal();
    }

    // 5. Verifies HMAC for integrity
    private static boolean verifyHMAC(byte[] iv, byte[] ciphertext, byte[] expectedHMAC) throws Exception {
        byte[] computedHMAC = generateHMAC(iv, ciphertext);
        return Arrays.equals(computedHMAC, expectedHMAC);
    }
}
