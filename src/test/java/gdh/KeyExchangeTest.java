package test.java.gdh;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
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
public class KeyExchangeTest
{
	
	@Test
	public void testDoubleKeyExchange(TestContext context)
	{
		int amount = 2;
		testNegotiation(amount, context);
	}
	
	@Test
	public void testTripleKeyExchange(TestContext context)
	{
		int amount = 3;
		testNegotiation(amount, context);
	}
	
	@Test
	public void testQuadrupleKeyExchange(TestContext context)
	{
		int amount = 4;
		testNegotiation(amount, context);
	}
	
	@Test
	public void testQuintupleKeyExchange(TestContext context)
	{
		int amount = 5;
		testNegotiation(amount, context);
	}
	
	// real deployment and communication between verticles on localhost
	private void testNegotiation(int amount, TestContext context) {
		Async async = context.async();
		//Vertx vertx = Vertx.vertx(); 
		PrimaryVertex pv = new PrimaryVertex();
		GDHVertex[] verticles = new GDHVertex[amount];
		Configuration[] confs = new Configuration[amount];

		for (int i=0; i<amount; i++)
		{
			verticles[i] = new GDHVertex();
			confs[i] = new Configuration();
			String port = amount + "08" + i;
			confs[i].setIP("localhost").setPort(port).setLogLevel(Level.ALL);
			verticles[i].setConfiguration(confs[i]);
		}
		List<GDHVertex> list = new ArrayList<>(Arrays.asList(verticles));
		
		Group g = new Group(confs[0], list.stream().map(y->y.getNode()).collect(Collectors.toList()));
		verticles[0].addGroup(g);
		
		for (int i=0; i<amount; i++)
			pv.run(verticles[i],res -> {
				      if (res.succeeded()) {
				          	System.out.println("Deployed verticle!" + res.result());
				          	async.countDown();
				      } else {
				    	    res.cause().printStackTrace();
				        	System.out.println("Deployment failed for verticle!" + res.cause().getMessage() + " Error " +
				        			res.toString() + " Result " + res.result());
				      }
			});
		async.awaitSuccess();
		
		BigInteger key = null;
	  	try 
	  	{
	  		key = verticles[0].negotiate(g.getGroupId()).get();
	  		System.out.println("THE KEY " + key);
	  		for (int j=0; j<verticles.length; j++)
	  		{
	  			System.out.println("CANDIDATE " + verticles[j].getKey(g.getGroupId()).get());
	  			Assert.assertEquals(verticles[j].getKey(g.getGroupId()).get(),key);
	  		}
	  	} 
	  	catch (InterruptedException | ExecutionException e) 
	  	{
	  		// TODO Auto-generated catch block
	  		e.printStackTrace();
	  	}
	  	//vertx.deploymentIDs().forEach(vertx::undeploy);
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
