package main.test.gdh;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.util.JNetPcapFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import main.java.gdh.Configuration;
import main.java.gdh.GDHVertex;
import main.java.gdh.Group;

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
	
	@Test
	public <T> void testNoKeyOnWire(TestContext context)
	{
		Pcap pcap = Pcap.openLive("lo", 256, Pcap.MODE_NON_PROMISCUOUS, 2000, new StringBuilder());
		JPacketHandler<T> sniffer = new JPacketHandler<T>() {

			@Override
			public void nextPacket(JPacket arg0, T arg1) {
				// TODO Auto-generated method stub
				
			}
		};
		pcap.loop(Pcap.LOOP_INFINITE, sniffer, null);
		
	}

	// real deployment and communication between verticles
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
		
		BigInteger key = null;
	  	try 
	  	{
	  		key = verticles[0].negotiate(g.getGroupId()).get();
	  		for (int j=0; j<verticles.length; j++)
	  			Assert.assertEquals(verticles[j].getKey(g.getGroupId()).get(),key);
	  	} 
	  	catch (InterruptedException | ExecutionException e) 
	  	{
	  		// TODO Auto-generated catch block
	  		e.printStackTrace();
	  	}
	  	vertx.deploymentIDs().forEach(vertx::undeploy);
	}
	
}
