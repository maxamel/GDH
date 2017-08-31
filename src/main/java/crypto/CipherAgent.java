package main.java.crypto;

import javax.crypto.SecretKey;

public interface CipherAgent {
	
	public byte[] encrypt(String value, byte[] iv, SecretKey key);

	public String decrypt(byte[] encryptedBytes, byte[] iv, SecretKey key);
}
