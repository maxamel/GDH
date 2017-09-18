package main.test.gdh;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	GroupTest.class, 
	ConfigurationTest.class, 
	KeyExchangeTest.class, 
	MultipleGroupKeyExchangeTest.class,
	AsyncKeyExchangeTest.class,
	NoKeyOnWireTest.class
})

public class VertxTestSuite
{
	private Vertx vertx;
	@Before
	public void setUp(TestContext context) throws IOException {
	   vertx = Vertx.vertx();

	}
	  
	@After
	public void tearDown(TestContext context) {
	   vertx.close(context.asyncAssertSuccess());
	}
}
