package main.test.gdh;

import org.junit.Assert;
import org.junit.Test;

import main.java.gdh.Configuration;

public class ConfigurationTest 
{
	@Test
	public void testConfiguration()
	{
		Configuration conf = Configuration.readConfigFile("configuration");
		Assert.assertEquals(conf, new Configuration());
	}
}