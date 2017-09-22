package main.java.gdh;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class PrimaryVertex
{
	private final Vertx vertx = Vertx.vertx();
	
	/**
	 * @param gdh the GDHVertex to be deployed
	 * @param aHandler the handler which handles the deployment
	 */
	public void run(GDHVertex gdh, Handler<AsyncResult<String>> aHandler) 
	{  
		vertx.deployVerticle(gdh, deployment -> {
		      if (deployment.succeeded()) {
		    	  aHandler.handle(Future.succeededFuture(gdh.deploymentID()));
		      } else {
		    	  aHandler.handle(Future.failedFuture("Deployment Failure!"));
		      }
		});
	}
	
	/**
	 * 
	 * 
	 * @param gdh the GDHVertex to be undeployed
	 * @param aHandler the handler which handles the undeployment
	 */
	public void kill(GDHVertex gdh, Handler<AsyncResult<String>> aHandler) 
	{  
		vertx.undeploy(gdh.deploymentID(), undeployment -> {
		      if (undeployment.succeeded()) {
		    	  aHandler.handle(Future.succeededFuture(gdh.deploymentID()));
		      } else {
		    	  aHandler.handle(Future.failedFuture("Undeployment Failure!"));
		      }
		});
	}
}
