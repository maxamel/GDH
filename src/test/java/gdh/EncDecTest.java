package test.java.gdh;

import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.Test;

import main.java.crypto.CipherAgent;
import main.java.crypto.CipherAgentImpl;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class EncDecTest {
    @Test
    public void testEncryption1() {
        String algo = "AES/CBC/PKCS5Padding";
        byte[] iv = new byte[16];
        Random random = new SecureRandom();
        random.nextBytes(iv);

        byte[] key = new byte[16];
        random.nextBytes(key);
        SecretKey seckey = new SecretKeySpec(key, "AES");

        String dec = null;
        try {
            CipherAgent agent = new CipherAgentImpl(algo);
            byte[] enc = agent.encrypt(algo, iv, seckey);
            dec = agent.decrypt(enc, iv, seckey);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(algo.equals(dec));
    }

    @Test
    public void testEncryption2() {
        String algo = "AES/OFB/PKCS5Padding";
        byte[] iv = new byte[16];
        Random random = new SecureRandom();
        random.nextBytes(iv);

        byte[] key = new byte[16];
        random.nextBytes(key);
        SecretKey seckey = new SecretKeySpec(key, "AES");

        String dec = null;
        try {
            CipherAgent agent = new CipherAgentImpl(algo);
            byte[] enc = agent.encrypt(algo, iv, seckey);
            dec = agent.decrypt(enc, iv, seckey);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(algo.equals(dec));
    }

    @Test
    public void testEncryption3() {
        String algo = "AES/CFB/NoPadding";
        byte[] iv = new byte[16];
        Random random = new SecureRandom();
        random.nextBytes(iv);

        byte[] key = new byte[16];
        random.nextBytes(key);
        SecretKey seckey = new SecretKeySpec(key, "AES");

        String dec = null;
        try {
            CipherAgent agent = new CipherAgentImpl(algo);
            byte[] enc = agent.encrypt(algo, iv, seckey);
            dec = agent.decrypt(enc, iv, seckey);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(algo.equals(dec));
    }
}
