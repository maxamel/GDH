package main.test.gdh;

import java.io.IOException;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import main.java.gdh.Configuration;
import main.java.gdh.GDHVertex;
import main.java.gdh.Group;
import main.java.gdh.Node;

@RunWith(VertxUnitRunner.class)
public class VertxTestSuite 
{
	private Vertx vertx;
	@Before
	  public void setUp(TestContext context) throws IOException {
	    vertx = Vertx.vertx();

	    // Let's configure the verticle to listen on the 'test' port (randomly picked).
	    // We create deployment options and set the _configuration_ json object:
	    
	    // We pass the options as the second parameter of the deployVerticle method.
	    //vertx.deployVerticle(MyFirstVerticle.class.getName(), options, context.asyncAssertSuccess());
	  }

	@Test
	public void testDoubleKeyExchange(TestContext context)
	{
		Vertx vertx = Vertx.vertx(); 
		GDHVertex vertx1 = new GDHVertex();
		GDHVertex vertx2 = new GDHVertex();
		
		Configuration conf1 = new Configuration();
		conf1.setIP("localhost").setPort("1081");
		Configuration conf2 = new Configuration();
		conf2.setIP("localhost").setPort("1082");
		vertx1.setConfiguration(conf1);
		vertx2.setConfiguration(conf2);
		
		TreeSet<Node> set = new TreeSet<>();
		set.add(vertx1.getNode());
		set.add(vertx2.getNode());
		Group g = new Group(set, conf1);

		vertx1.addGroup(g);
		vertx.deployVerticle(vertx1);
		vertx.deployVerticle(vertx2);
		
		vertx1.negotiate(g.getGroupId());
		while(!vertx2.isDone(g.getGroupId())) {}
		{
			System.out.println("ONE " + vertx1.getFinalKey(g.getGroupId()) + "\nTWO " + vertx2.getFinalKey(g.getGroupId()));
		}
	}
	
	@Test
	public void testTripleKeyExchange(TestContext context)
	{
		Vertx vertx = Vertx.vertx(); 
		GDHVertex vertx1 = new GDHVertex();
		GDHVertex vertx2 = new GDHVertex();
		GDHVertex vertx3 = new GDHVertex();
		
		Configuration conf1 = new Configuration();
		conf1.setIP("localhost").setPort("1081");
		Configuration conf2 = new Configuration();
		conf2.setIP("localhost").setPort("1082");
		Configuration conf3 = new Configuration();
		conf3.setIP("localhost").setPort("1083");
		vertx1.setConfiguration(conf1);
		vertx2.setConfiguration(conf2);
		vertx3.setConfiguration(conf3);
		
		TreeSet<Node> set = new TreeSet<>();
		set.add(vertx1.getNode());
		set.add(vertx2.getNode());
		set.add(vertx3.getNode());
		Group g = new Group(set, conf1);

		vertx1.addGroup(g);
		vertx.deployVerticle(vertx1);
		vertx.deployVerticle(vertx2);
		vertx.deployVerticle(vertx3);
		
		vertx1.negotiate(g.getGroupId());
		while(!vertx3.isDone(g.getGroupId())) {}
		{
			System.out.println("ONE " + vertx1.getFinalKey(g.getGroupId()) + 
					"\nTWO " + vertx2.getFinalKey(g.getGroupId()) +
					"\nTHREE " + vertx3.getFinalKey(g.getGroupId()));
		}
	}
	
	@Test
	public void testQuadrupleKeyExchange(TestContext context)
	{
		Vertx vertx = Vertx.vertx(); 
		GDHVertex vertx1 = new GDHVertex();
		GDHVertex vertx2 = new GDHVertex();
		GDHVertex vertx3 = new GDHVertex();
		GDHVertex vertx4 = new GDHVertex();
		
		Configuration conf1 = new Configuration();
		conf1.setIP("localhost").setPort("1081");
		Configuration conf2 = new Configuration();
		conf2.setIP("localhost").setPort("1082");
		Configuration conf3 = new Configuration();
		conf3.setIP("localhost").setPort("1083");
		Configuration conf4 = new Configuration();
		conf4.setIP("localhost").setPort("1084");
		vertx1.setConfiguration(conf1);
		vertx2.setConfiguration(conf2);
		vertx3.setConfiguration(conf3);
		vertx4.setConfiguration(conf4);
		
		TreeSet<Node> set = new TreeSet<>();
		set.add(vertx1.getNode());
		set.add(vertx2.getNode());
		set.add(vertx3.getNode());
		set.add(vertx3.getNode());
		Group g = new Group(set, conf1);

		vertx1.addGroup(g);
		vertx.deployVerticle(vertx1);
		vertx.deployVerticle(vertx2);
		vertx.deployVerticle(vertx3);
		vertx.deployVerticle(vertx4);
		
		vertx1.negotiate(g.getGroupId());
		while(!vertx4.isDone(g.getGroupId())) {}
		{
			System.out.println("ONE " + vertx1.getFinalKey(g.getGroupId()) + 
					"\nTWO " + vertx2.getFinalKey(g.getGroupId()) +
					"\nTHREE " + vertx3.getFinalKey(g.getGroupId()) +
					"\nFOUR " + vertx4.getFinalKey(g.getGroupId()));
		}
	}
		
	  /**
	   * This method, called after our test, just cleanup everything by closing the vert.x instance
	   *
	   * @param context the test context
	   */
	  @After
	  public void tearDown(TestContext context) {
	    vertx.close(context.asyncAssertSuccess());
	  }
}
