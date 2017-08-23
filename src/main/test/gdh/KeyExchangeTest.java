package main.test.gdh;

import java.util.TreeSet;

import org.junit.Test;

import io.vertx.core.Vertx;
import main.java.gdh.Configuration;
import main.java.gdh.GDHVertex;
import main.java.gdh.Group;
import main.java.gdh.Node;

public class KeyExchangeTest 
{
	/*@Test
	public void testKeyExchange()
	{
		Vertx vertx = Vertx.vertx(); 
		GDHVertex vertx1 = new GDHVertex();
		GDHVertex vertx2 = new GDHVertex();
		
		Configuration conf1 = new Configuration();
		conf1.setIP("localhost").setPort("1081");
		Configuration conf2 = new Configuration();
		conf1.setIP("localhost").setPort("1082");
		vertx1.setConfiguration(conf1);
		vertx2.setConfiguration(conf2);
		
		TreeSet<Node> set = new TreeSet<>();
		set.add(vertx1.getNode());
		set.add(vertx2.getNode());
		Group g = new Group(set, conf1);

		vertx1.addGroup(g);
	
		vertx.deployVerticle(vertx1);
		vertx.deployVerticle(vertx2);
		//vertx.deployVerticle(new GDHVertex());
		//vertx.deployVerticle("main.java.gdh.GDHVertex");
		while (true)
		{}
	}*/
}
