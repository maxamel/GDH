package main.test.gdh;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import main.java.gdh.Configuration;


@RunWith(VertxUnitRunner.class)
@SuiteClasses({
	GroupTest.class, Configuration.class, KeyExchangeTest.class
})

public class VertxTestSuite 
{
	private Vertx vertx;
	@Before
	public void setUp(TestContext context) throws IOException {
	   vertx = Vertx.vertx();

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
