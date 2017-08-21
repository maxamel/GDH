package main.java.gdh;

public class Configuration 
{
	private String IP = "localhost";
	private String port = "5900";
	
	public Configuration(){}
	
	public Configuration setIP(String IP)
	{
		this.IP = IP;
		return this;
	}
	
	public Configuration setPort(String port)
	{
		this.port = port;
		return this;
	}

	public String getIP() {
		return IP;
	}

	public String getPort() {
		return port;
	}
}
