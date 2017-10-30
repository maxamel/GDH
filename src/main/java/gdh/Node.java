package main.java.gdh;

/**
 *
 * Node is an object which represents the network layer of a GDHVertex.
 * 
 * It has an IP and port which must be unique in any verticle layout. No two
 * verticle instances can listen on
 * 
 * the same IP and port.
 * 
 * @author Max Amelchenko
 */
public class Node implements Comparable<Node> {
    private final String IP;
    private final String port;

    public Node(String IP, String port) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((IP == null) ? 0 : IP.hashCode());
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (IP == null) {
            if (other.IP != null)
                return false;
        } else if (!IP.equals(other.IP))
            return false;
        if (port == null) {
            if (other.port != null)
                return false;
        } else if (!port.equals(other.port))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Node [IP=" + IP + ", port=" + port + "]";
    }
}
