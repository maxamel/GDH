package main.java.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import main.java.gdh.Constants;

public class CipherAgentImpl implements CipherAgent {
    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public CipherAgentImpl(String instance) throws NoSuchAlgorithmException, NoSuchPaddingException {
        encryptCipher = Cipher.getInstance(instance);
        decryptCipher = Cipher.getInstance(instance);
    }

    // encryption method receiving a value to encrypt, the initial vector and a
    // key
    public byte[] encrypt(String value, byte[] iv, SecretKey key)
            throws InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        byte[] encryptedBytes = null;
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, encryptCipher);
        cipherOutputStream.write(value.getBytes(StandardCharsets.UTF_8));
        cipherOutputStream.flush();
        cipherOutputStream.close();
        encryptedBytes = outputStream.toByteArray();

        return encryptedBytes;
    }

    // decryption method receiving a value to decrypt, the initial vector and a
    // key
    @SuppressFBWarnings("UC_USELESS_OBJECT")
    public String decrypt(byte[] encryptedBytes, byte[] iv, SecretKey key)
            throws InvalidKeyException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IOException {
        byte[] buf = new byte[Constants.CIPHER_SIZE];
        ByteArrayOutputStream outputStream = null;
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        decryptCipher.init(Cipher.DECRYPT_MODE, key, ivspec);
        outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inStream = new ByteArrayInputStream(encryptedBytes);
        CipherInputStream cipherInputStream = new CipherInputStream(inStream, decryptCipher);
        int bytesRead = 0;
        while ((bytesRead = cipherInputStream.read(buf)) >= 0) {
            outputStream.write(buf, 0, bytesRead);
        }
        cipherInputStream.close();

        return outputStream.toString(StandardCharsets.UTF_8.name());
    }
}
