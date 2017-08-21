package main.java.gdh;

import io.vertx.core.Vertx;

public class Main 
{
	public static void main(String[] args)
	{
		Vertx vertx = Vertx.vertx(); 
		vertx.deployVerticle("main.java.gdh.Server");
	}
}
