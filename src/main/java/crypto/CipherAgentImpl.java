package main.java.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import main.java.gdh.Constants;

public class CipherAgentImpl implements CipherAgent
{
	private Cipher encryptCipher;
	private Cipher decryptCipher;
	
	public CipherAgentImpl(String instance)
	{
		try
		{
			encryptCipher = Cipher.getInstance(instance);
			decryptCipher = Cipher.getInstance(instance);
		}
	    catch (Exception e)
		{
			System.out.println("AES Encryptor initialization failure!");
		}
	}
	// encryption method receiving a value to encrypt, the initial vector and a key 
	public byte[] encrypt(String value, byte[] iv, SecretKey key) 
	{
		byte[] encryptedBytes = null;
		try
		{
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			encryptCipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, encryptCipher);
			cipherOutputStream.write(value.getBytes(StandardCharsets.UTF_8));
			cipherOutputStream.flush();
			cipherOutputStream.close();
			encryptedBytes = outputStream.toByteArray();
		}
		catch (InvalidKeyException | InvalidAlgorithmParameterException | IOException e)
		{
			System.out.println("Exception Encrypting! " + e.getMessage());
		}
		return encryptedBytes;
	}
	// decryption method receiving a value to decrypt, the initial vector and a key 
	@SuppressFBWarnings("UC_USELESS_OBJECT")
	public String decrypt(byte[] encryptedBytes, byte[] iv, SecretKey key) 
	{
		byte[] buf = new byte[Constants.CIPHER_SIZE];
		try
		{
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			decryptCipher.init(Cipher.DECRYPT_MODE, key, ivspec);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    ByteArrayInputStream inStream = new ByteArrayInputStream(encryptedBytes);
		    CipherInputStream cipherInputStream = new CipherInputStream(inStream, decryptCipher);
		    int bytesRead;
		    while ((bytesRead = cipherInputStream.read(buf)) >= 0) {
		        outputStream.write(buf, 0, bytesRead);
		    }
		    cipherInputStream.close();
		}
		catch (IOException | InvalidKeyException | InvalidAlgorithmParameterException e)
		{
			System.out.println("Exception Encrypting! " + e.getMessage());
		}
	    return new String(buf,StandardCharsets.UTF_8);
	}
}
