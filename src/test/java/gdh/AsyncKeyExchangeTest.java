package test.java.gdh;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
public class AsyncKeyExchangeTest 
{

	@Test
	public void testDoubleKeyExchange(TestContext context)
	{
		int amount = 2;
		testAsyncNegotiation(amount, context);
	}

	// real deployment and communication between verticles on localhost
	private void testAsyncNegotiation(int amount, TestContext context) {
		Async async = context.async();
		PrimaryVertex pv = new PrimaryVertex();
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
			pv.run(verticles[i],res -> {
				      if (res.succeeded()) {
				          	System.out.println("Deployed verticle!");
				          	async.countDown();
				      } else {
				        	System.out.println("Deployment failed for verticle!");
				      }
			});
		async.awaitSuccess();
		
		BigInteger[] keys = new BigInteger[2];
	  	try 
	  	{
	  		keys[0] = verticles[0].negotiate(g.getGroupId(), res -> {
			      if (res.succeeded()) {
			          	keys[1] = res.result();
			          	async.countDown();
			          	try 
			          	{
							Assert.assertEquals(keys[1],verticles[0].getKey(g.getGroupId()).get());
						} 
			          	catch (InterruptedException e) 
			          	{
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
			          	catch (ExecutionException e) 
			          	{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			      } else {
			        	System.out.println("Negotiation failed! ");
			      }
			      
			}).get();
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
