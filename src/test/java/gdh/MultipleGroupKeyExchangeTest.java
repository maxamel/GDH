package test.java.gdh;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import main.java.gdh.Configuration;
import main.java.gdh.GDHVertex;
import main.java.gdh.Group;
import main.java.gdh.PrimaryVertex;

@RunWith(VertxUnitRunner.class)
public class MultipleGroupKeyExchangeTest 
{
	@Test
	public void testDoubleGroupKeyExchange(TestContext context)
	{
		int amount = 3;
		testNegotiation(amount, context);
	}
	
	@Test
	public void testTripleGroupKeyExchange(TestContext context)
	{
		int amount = 4;
		testNegotiation(amount, context);
	}
	
	@Test
	public void testQuadrupleGroupKeyExchange(TestContext context)
	{
		int amount = 5;
		testNegotiation(amount, context);
	}

	// real deployment and communication between verticles
	private void testNegotiation(int amount, TestContext context) {
		Async async = context.async();
		PrimaryVertex pv = new PrimaryVertex();
		GDHVertex[] verticles = new GDHVertex[amount];
		Configuration[] confs = new Configuration[amount];

		for (int i=0; i<amount; i++)
		{
			verticles[i] = new GDHVertex();
			confs[i] = new Configuration();
			String port = amount + "08" + i;
			confs[i].setIP("localhost").setPort(port);
			verticles[i].setConfiguration(confs[i]);
		}
		
		Group[] groups = new Group[amount-1];
		BigInteger[] keys = new BigInteger[amount-1];
		for (int i=0; i<amount-1; i++)
		{
			groups[i] = new Group(confs[0], verticles[0].getNode(), verticles[i+1].getNode());
			verticles[0].addGroup(groups[i]);
		}
		
		for (int i=0; i<amount; i++)
			pv.run(verticles[i],res -> {
			      if (res.succeeded()) {
			          	System.out.println("Deployed verticle!" + res.result());
			          	async.countDown();
			      } else {
			        	System.out.println("Deployment failed for verticle!" + res.result());
			      }
		});
		
		async.awaitSuccess();
		
		try 
		{
			for (int i=0; i<amount-1; i++)
				keys[i] = verticles[0].negotiate(groups[i].getGroupId()).get();
			
			for (int i=0; i<amount-1; i++)
				Assert.assertEquals(verticles[i+1].getKey(groups[i].getGroupId()).get(),
									verticles[0].getKey(groups[i].getGroupId()).get());
			
			Map<BigInteger, Integer> mapOfKeys = new HashMap<>();
			for (int i=0; i<amount-1; i++)
			{
				BigInteger key = verticles[i+1].getKey(groups[i].getGroupId()).get();
				if (mapOfKeys.containsKey(key))
					mapOfKeys.put(key, mapOfKeys.get(key)+1);
				else mapOfKeys.put(key, 1);
			}
			
			for (Integer count : mapOfKeys.values())
				Assert.assertTrue(count==1);
				
		} 
		catch (InterruptedException | ExecutionException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i=0; i<amount; i++)
			pv.kill(verticles[i],res -> {
				      if (res.succeeded()) {
				          	System.out.println("Undeployed verticle!" + res.result());
				          	async.countDown();
				      } else {
				    	    res.cause().printStackTrace();
				        	System.out.println("Undeployment failed for verticle!" + res.cause().getMessage() + " Error " +
				        			res.toString() + " Result " + res.result());
				      }
			});
	}
	
}