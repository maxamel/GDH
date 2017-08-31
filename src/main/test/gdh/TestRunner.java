package main.test.gdh;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public final class TestRunner {
	
	private TestRunner()
	{
		
	}
	
	public static void main(String[] args) {
	      Result result = JUnitCore.runClasses(VertxTestSuite.class);

	      for (Failure failure : result.getFailures()) {
	         System.out.println(failure.toString());
	      }
			
	      System.out.println(result.wasSuccessful());
	   }
}
