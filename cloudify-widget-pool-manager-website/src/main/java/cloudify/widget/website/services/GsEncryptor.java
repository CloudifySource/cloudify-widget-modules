package cloudify.widget.website.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Created by sefi on 7/16/14.
 */
public class GsEncryptor {
    private static final String CIPHER_ALGORITHM = "AES";
    private static final String KEY_ALGORITHM = "AES";
    private static final String HASH_ALGORITHM = "SHA-256";

    private static final Logger logger = LoggerFactory.getLogger(GsEncryptor.class);

    public String encrypt(String key, String strToEncrypt) {
        if (isEncrypted(strToEncrypt)) {
            return strToEncrypt;
        }

        try {
            Cipher cipher = buildCipher("thisIsASecretKey", Cipher.ENCRYPT_MODE);
            final String encryptedString = Base64.encodeBase64String(cipher.doFinal(strToEncrypt.getBytes()));
            return encryptedString;
        } catch (Exception e) {
            logger.error("failed to encrypt string", e);
            e.printStackTrace();
        }
        return null;

    }

    public String decrypt(String key, String strToDecrypt) {
        if (!isEncrypted(strToDecrypt)) {
            return strToDecrypt;
        }

        try {
            Cipher cipher = buildCipher("thisIsASecretKey", Cipher.DECRYPT_MODE);
            final String decryptedString = new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt)));
            return decryptedString;
        } catch (Exception e) {
            logger.error("failed to decrypt string", e);
            e.printStackTrace();

        }
        return null;
    }

    private Boolean isEncrypted(String string) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.readValue(string, Object.class);
            // parse success - not encrypted.
            return false;
        } catch (Exception e) {
            // parse failed - encrypted.
            return true;
        }
    }

    private Cipher buildCipher(String key, int mode) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        final SecretKeySpec secretKeySpec = buildKey(key);
        cipher.init(mode, secretKeySpec);
        return cipher;
    }

    private SecretKeySpec buildKey(String key) throws Exception {
        MessageDigest digester = MessageDigest.getInstance(HASH_ALGORITHM);
        digester.update(key.getBytes());
        byte[] keyBytes = digester.digest();
        SecretKeySpec spec = new SecretKeySpec(Arrays.copyOfRange(keyBytes, 0, 16), KEY_ALGORITHM);
//        SecretKeySpec spec = new SecretKeySpec(key.getBytes(), KEY_ALGORITHM);
        return spec;
    }
}
