package com.strangequark.vpnservice.utility;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtility {
    private static final String ENCRYPTION_KEY = resolveKey();

    private static String resolveKey() {
        String key = System.getProperty("ENCRYPTION_KEY");
        if(key == null)
            key = System.getenv("ENCRYPTION_KEY");
        if(key == null || key.length() != 32)
            throw new IllegalStateException("ENCRYPTION_KEY must be set and 32 chars long");
        return key;
    }

    public static String encrypt(String raw) {
        try {
            SecretKey key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(raw.getBytes()));
        } catch(Exception ex) {
            throw new RuntimeException("Encryption error", ex);
        }
    }

    public static String decrypt(String encrypted) {
        try {
            SecretKey key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
        } catch(Exception ex) {
            throw new RuntimeException("Decryption error", ex);
        }
    }
}
