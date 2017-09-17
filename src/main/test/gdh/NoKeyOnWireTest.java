package main.test.gdh;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Assert;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import main.java.gdh.Configuration;
import main.java.gdh.GDHVertex;
import main.java.gdh.Group;

//@RunWith(VertxUnitRunner.class)
public class NoKeyOnWireTest 
{

	//@Test
	public void testExchangeNoKeyOnWire(TestContext context)
	{
		int amount = 2;
		testNegotiation(amount, context);
	}

	// real deployment and communication between verticles on localhost
	private void testNegotiation(int amount, TestContext context) {
		Async async = context.async();
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
			vertx.deployVerticle(verticles[i],res -> {
				      if (res.succeeded()) {
				          	System.out.println("Deployed verticle!");
				          	async.countDown();
				      } else {
				        	System.out.println("Deployment failed for verticle!");
				      }
			});
		async.awaitSuccess();
		StringBuilder builder = new StringBuilder();
		
		BigInteger[] keys = new BigInteger[2];
	  	try 
	  	{
	  		keys[0] = verticles[0].negotiate(g.getGroupId(), res -> {
			      if (res.succeeded()) {
			          	keys[1] = res.result();
			          	async.countDown();
			          	Assert.assertEquals(keys[1].intValue(),keys[0].intValue());
			          	
			      } else {
			        	System.out.println("Negotiation failed! ");
			      }
			      
			}).get();
	  		
	  		Assert.assertFalse(builder.toString().contains(keys[1].toString()));
	  	} 
	  	catch (InterruptedException | ExecutionException e) 
	  	{
	  		// TODO Auto-generated catch block
	  		e.printStackTrace();
	  	}
	  	vertx.deploymentIDs().forEach(vertx::undeploy);
	}
}
