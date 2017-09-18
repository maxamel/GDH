package main.java.crypto;

import javax.crypto.SecretKey;

public interface CipherAgent {
	
	// encryption method receiving a value to encrypt, the initial vector and a key 
	public byte[] encrypt(String value, byte[] iv, SecretKey key);

	// decryption method receiving a value to decrypt, the initial vector and a key 
	public String decrypt(byte[] encryptedBytes, byte[] iv, SecretKey key);
}
