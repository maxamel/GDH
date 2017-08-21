package main.java.gdh;

public class Node implements Comparable<Node>
{
	private String IP;
	private String port;
	
	public Node(String IP, String port)
	{
		this.IP = IP;
		this.port = port;
	}
	
	public String getIP() {
		return IP;
	}
	public String getPort() {
		return port;
	}

	@Override
	public int compareTo(Node o) {
		if (this.getIP().compareTo(o.getIP()) == 0)
			return (this.getPort().compareTo(o.getPort()));
		return this.getIP().compareTo(o.getIP());
	}
	
	
}
