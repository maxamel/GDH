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
	public void testKeyExchange(TestContext context)
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
	
		vertx.deployVerticle(vertx1, context.asyncAssertSuccess(ID -> {
			System.out.println("Vertx1 deployed!");
		}));
		vertx.deployVerticle(vertx2, context.asyncAssertSuccess(ID -> {
			System.out.println("Vertx2 deployed!");
		}));
		//vertx.deployVerticle(new GDHVertex());
		//vertx.deployVerticle("main.java.gdh.GDHVertex");
		
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
