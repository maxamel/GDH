package main.test.gdh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import main.java.gdh.Configuration;
import main.java.gdh.GDHVertex;
import main.java.gdh.Group;

public class KeyExchangeTest 
{
	@Test
	public void testDoubleKeyExchange(TestContext context)
	{
		int amount = 2;
		testNegotiation(amount);
	}
	
	@Test
	public void testTripleKeyExchange(TestContext context)
	{
		int amount = 3;
		testNegotiation(amount);
	}
	
	@Test
	public void testQuadrupleKeyExchange(TestContext context)
	{
		int amount = 4;
		testNegotiation(amount);
	}
	
	@Test
	public void testQuintupleKeyExchange(TestContext context)
	{
		int amount = 5;
		testNegotiation(amount);
	}

	private void testNegotiation(int amount) {
		Vertx vertx = Vertx.vertx(); 
		GDHVertex[] verticles = new GDHVertex[amount];
		Configuration[] confs = new Configuration[amount];

		for (int i=0; i<amount; i++)
		{
			verticles[i] = new GDHVertex();
			confs[i] = new Configuration();
			int port = 1080 + i;
			confs[i].setIP("localhost").setPort(String.valueOf(port));
			verticles[i].setConfiguration(confs[i]);
		}
		List<GDHVertex> list = new ArrayList<>(Arrays.asList(verticles));
		
		Group g = new Group(confs[0], list.stream().map(y->y.getNode()).collect(Collectors.toList()));
		verticles[0].addGroup(g);
		
		for (int i=0; i<amount; i++)
			vertx.deployVerticle(verticles[i]);
		
		verticles[0].negotiate(g.getGroupId());
		
		for (int i=0; i<amount; i++)
			while (!verticles[i].isDone(g.getGroupId()));
		
		for (int i=0; i<verticles.length-1; i++)
			Assert.assertEquals(verticles[i].getFinalKey(g.getGroupId()),verticles[i+1].getFinalKey(g.getGroupId()));
			
		vertx.deploymentIDs().forEach(vertx::undeploy);
	}
	
}
