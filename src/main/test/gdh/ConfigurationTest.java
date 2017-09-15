package main.test.gdh;

import org.junit.Assert;
import org.junit.Test;

import main.java.gdh.Configuration;

public class ConfigurationTest 
{
	@Test
	public void testConfigurationFile()
	{
		Configuration conf = Configuration.readConfigFile("configuration");
		Assert.assertEquals(conf, new Configuration());
	}
	
	@Test
	public void testConfigurationIP()
	{
		Configuration conf = new Configuration();
		conf = conf.setPort("3333");
		Assert.assertTrue(conf.getPort().equals("3333"));
	}
}