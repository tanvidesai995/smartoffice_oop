public class Encryption {

    private static final int key = 1; // fixed key
    // --------------------- Encryption ---------------------
    public static String encrypt(String text) {
        String result = "";

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isUpperCase(c)) {
                char encryptedChar = (char) ('A' + (c - 'A' + key) % 26);
                result += encryptedChar;
            } else if (Character.isLowerCase(c)) {
                char encryptedChar = (char) ('a' + (c - 'a' + key) % 26);
                result += encryptedChar;
            } else if (Character.isDigit(c)) {
                char encryptedChar = (char) ('0' + (c - '0' + key) % 10);
                result += encryptedChar;
            } else {
                result += c; // special characters unchanged
            }
        }

        return result;
    }

    // --------------------- Decryption ---------------------
    public static String decrypt(String text) {
        String result = "";

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isUpperCase(c)) {
                char decryptedChar = (char) ('A' + (c - 'A' - key + 26) % 26);
                result += decryptedChar;
            } else if (Character.isLowerCase(c)) {
                char decryptedChar = (char) ('a' + (c - 'a' - key + 26) % 26);
                result += decryptedChar;
            } else if (Character.isDigit(c)) {
                char decryptedChar = (char) ('0' + (c - '0' - key + 10) % 10);
                result += decryptedChar;
            } else {
                result += c; // special characters unchanged
            }
        }

        return result;
    }
}
