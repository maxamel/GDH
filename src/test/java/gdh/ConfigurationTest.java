package test.java.gdh;

import org.apache.log4j.ConsoleAppender;
import org.junit.Assert;
import org.junit.Test;

import main.java.gdh.Configuration;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class ConfigurationTest {
    @Test
    public void testConfigurationFile() {
        Configuration conf = Configuration.readConfigFile("configuration");
        Assert.assertTrue(conf.equals(new Configuration()));
    }

    @Test
    public void testConfigurationPort() {
        Configuration conf = new Configuration();
        conf = conf.setPort("3333");
        Assert.assertTrue(conf.getPort().equals("3333"));
    }

    @Test(expected = AssertionError.class)
    public void testConfigurationPortFail1() {
        Configuration conf = new Configuration();
        conf = conf.setPort("333a");
    }

    @Test(expected = AssertionError.class)
    public void testConfigurationPortFail2() {
        Configuration conf = new Configuration();
        conf = conf.setPort("");
    }

    @Test
    public void testConfigurationRetries() {
        Configuration conf = new Configuration();
        conf = conf.setRetries(10);
        Assert.assertTrue(conf.getRetries() == 10);
    }

    @Test
    public void testConfigurationIP() {
        Configuration conf = new Configuration();
        conf = conf.setIP("1.1.1.1");
        Assert.assertTrue(conf.getIP().equals("1.1.1.1"));
    }

    @Test
    public void testConfigurationIPLocalhost() {
        Configuration conf = new Configuration();
        conf = conf.setIP("localhost");
        Assert.assertTrue(conf.getIP().equals("localhost"));
    }

    @Test(expected = AssertionError.class)
    public void testConfigurationIPFail1() {
        Configuration conf = new Configuration();
        conf = conf.setIP("1.1.1.a");
    }

    @Test(expected = AssertionError.class)
    public void testConfigurationIPFail2() {
        Configuration conf = new Configuration();
        conf = conf.setIP("1.1.1");
    }

    @Test(expected = AssertionError.class)
    public void testConfigurationIPFail3() {
        Configuration conf = new Configuration();
        conf = conf.setIP("1.1.1.1111");
    }

    @Test
    public void testConfigurationPrime() {
        Configuration conf = new Configuration();
        conf = conf.setPrime("AFB239DE");
        Assert.assertTrue(conf.getPrime().equals("AFB239DE"));
    }

    @Test(expected = AssertionError.class)
    public void testConfigurationPrimeFail1() {
        Configuration conf = new Configuration();
        conf = conf.setPrime("AFB239DE88G");
    }

    @Test(expected = AssertionError.class)
    public void testConfigurationPrimeFail2() {
        Configuration conf = new Configuration();
        conf = conf.setPrime("AFB239DE88a");
    }

    @Test
    public void testEquality1() {
        Configuration conf1 = new Configuration();
        Configuration conf2 = new Configuration();

        conf1 = conf1.setRetries(3);
        conf2 = conf2.setRetries(4);
        Assert.assertTrue(conf1.hashCode() == conf2.hashCode());
        Assert.assertTrue(conf1.equals(conf2));

        conf1 = conf1.setIP("1.1.1.1");
        conf2 = conf2.setIP("2.2.2.2");
        Assert.assertFalse(conf1.hashCode() == conf2.hashCode());
        Assert.assertFalse(conf1.equals(conf2));
    }

    @Test
    public void testEquality2() {
        Configuration conf1 = new Configuration();
        Configuration conf2 = new Configuration();

        conf1 = conf1.setExchangeTimeout(3000);
        conf2 = conf2.setExchangeTimeout(4000);
        Assert.assertTrue(conf1.hashCode() == conf2.hashCode());
        Assert.assertTrue(conf1.equals(conf2));

        conf1 = conf1.setPort("1111");
        conf2 = conf2.setPort("2222");
        Assert.assertFalse(conf1.hashCode() == conf2.hashCode());
        Assert.assertFalse(conf1.equals(conf2));
    }

    @Test
    public void testEquality3() {
        Configuration conf1 = new Configuration();
        Configuration conf2 = new Configuration();

        conf1 = conf1.setExchangeTimeout(3000);
        conf2 = conf2.setRetries(7);
        Assert.assertTrue(conf1.hashCode() == conf2.hashCode());
        Assert.assertTrue(conf1.equals(conf2));

        conf1 = conf1.setGenerator("111111");
        conf2 = conf2.setGenerator("222222");
        Assert.assertFalse(conf1.hashCode() == conf2.hashCode());
        Assert.assertFalse(conf1.equals(conf2));
    }
    
    @Test
    public void testEquality4() {
        Configuration conf1 = new Configuration();
        Configuration conf2 = new Configuration();

        conf1 = conf1.setAppender(null);
        conf2 = conf2.setAppender(new ConsoleAppender());
        Assert.assertTrue(conf1.hashCode() == conf2.hashCode());
        Assert.assertTrue(conf1.equals(conf2));

        conf1 = conf1.setPrime("111111");
        conf2 = conf2.setPrime("222222");
        Assert.assertFalse(conf1.hashCode() == conf2.hashCode());
        Assert.assertFalse(conf1.equals(conf2));
    }
}