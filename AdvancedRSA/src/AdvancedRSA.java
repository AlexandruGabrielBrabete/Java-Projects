import java.math.BigInteger;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;

public class AdvancedRSA {

    private static final SecureRandom random = new SecureRandom();

    // Step 1: Generate large prime numbers and keys
    public static BigInteger[] generateKeys(int bitLength) {
        BigInteger p = generatePrime(bitLength / 2); // Generate primes of bitLength/2
        BigInteger q = generatePrime(bitLength / 2);

        // Compute n = p * q
        BigInteger n = p.multiply(q);

        // Compute Euler's Totient function: phi(n) = (p-1) * (q-1)
        BigInteger phiN = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        // Choose e (public exponent), typically 65537 is used
        BigInteger e = BigInteger.valueOf(65537);

        // Compute d (private exponent), which is the modular inverse of e mod phiN
        BigInteger d = e.modInverse(phiN);

        // Return public key (e, n) and private key (d, n)
        return new BigInteger[]{e, n, d};
    }

    // Step 2: Generate a large prime number (ensures cryptographic security)
    private static BigInteger generatePrime(int bitLength) {
        BigInteger prime;
        do {
            prime = new BigInteger(bitLength, random).nextProbablePrime();
        } while (prime.bitLength() < bitLength); // Ensure it is of the correct bit length
        return prime;
    }

    // Step 3: Encrypt the message using the public key (e, n)
    public static BigInteger encrypt(BigInteger message, BigInteger e, BigInteger n) {
        // Apply full PKCS#1 v1.5 padding before encryption
        byte[] paddedMessage = pkcs1Padding(message.toByteArray(), n.bitLength() / 8);
        BigInteger paddedMessageBigInt = new BigInteger(1, paddedMessage);

        // Encrypt using modular exponentiation
        return paddedMessageBigInt.modPow(e, n);
    }

    // Step 4: Decrypt the message using the private key (d, n)
    public static BigInteger decrypt(BigInteger ciphertext, BigInteger d, BigInteger n) {
        // Decrypt the ciphertext using modular exponentiation
        BigInteger decryptedMessage = ciphertext.modPow(d, n);

        // Remove PKCS#1 v1.5 padding
        byte[] paddedMessageBytes = decryptedMessage.toByteArray();
        byte[] originalMessageBytes = pkcs1UnPadding(paddedMessageBytes);

        return new BigInteger(1, originalMessageBytes);
    }

    // Step 5: Apply PKCS#1 v1.5 Padding
    private static byte[] pkcs1Padding(byte[] messageBytes, int keyLength) {
        int messageLength = messageBytes.length;

        // Ensure the message fits in the key length
        if (messageLength + 11 > keyLength) {
            throw new IllegalArgumentException("Message too long to encrypt");
        }

        byte[] paddedMessage = new byte[keyLength];
        paddedMessage[0] = 0x00;
        paddedMessage[1] = 0x02; // Start with 0x02 byte for padding

        // Fill padding with random bytes
        byte[] padding = new byte[keyLength - 3 - messageLength];
        random.nextBytes(padding);
        System.arraycopy(padding, 0, paddedMessage, 2, padding.length);

        // Add the message with separator (0x00)
        paddedMessage[keyLength - messageLength - 1] = 0x00;
        System.arraycopy(messageBytes, 0, paddedMessage, keyLength - messageLength, messageLength);

        return paddedMessage;
    }

    // Step 6: Remove PKCS#1 v1.5 Padding
    private static byte[] pkcs1UnPadding(byte[] paddedMessage) {
        int i = 0;

        // Skip the leading 0x00 and 0x02 bytes
        while (paddedMessage[i] != 0x00) {
            i++;
        }
        i++; // Skip the 0x00 byte

        // The remaining bytes are the actual message
        byte[] message = new byte[paddedMessage.length - i];
        System.arraycopy(paddedMessage, i, message, 0, message.length);

        return message;
    }

    // Step 7: Example usage
    public static void main(String[] args) {
        // Key length for RSA (e.g., 2048 bits)
        int bitLength = 2048;

        // Generate keys
        BigInteger[] keys = generateKeys(bitLength);

        BigInteger e = keys[0]; // Public exponent
        BigInteger n = keys[1]; // Modulus
        BigInteger d = keys[2]; // Private exponent

        System.out.println("Public key (e, n): (" + e + ", " + n + ")");
        System.out.println("Private key (d, n): (" + d + ", " + n + ")");

        // Message to encrypt (as a BigInteger)
        String message = "Hello RSA with improved PKCS#1 v1.5 padding!";
        BigInteger m = new BigInteger(message.getBytes(StandardCharsets.UTF_8));

        // Encrypt the message
        BigInteger encryptedMessage = encrypt(m, e, n);
        System.out.println("Encrypted message: " + encryptedMessage);

        // Decrypt the message
        BigInteger decryptedMessage = decrypt(encryptedMessage, d, n);
        String decryptedString = new String(decryptedMessage.toByteArray(), StandardCharsets.UTF_8);
        System.out.println("Decrypted message: " + decryptedString);
    }
}
