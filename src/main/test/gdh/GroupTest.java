package main.test.gdh;

import java.util.TreeSet;
import org.junit.Test;

import main.java.gdh.Configuration;
import main.java.gdh.Group;
import main.java.gdh.Node;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class GroupTest 
{
	private static final String ip1 = "1.1.1.1";
	private static final String port1 = "3000";
	private static final String port2 = "3001";
	
	@Test
	public void testSameGroupHashCode()
	{
		Node n1 = new Node(ip1, port1);
		Node n2 = new Node(ip1, port2);
		Node n3 = new Node("2.2.2.2", port1);
		
		Group g1 = new Group(new Configuration(), n1,n2,n3);
		Group g2 = new Group(new Configuration(), n1,n2,n3);
		assert(g1.hashCode() == g2.hashCode());
	}
	
	@Test
	public void testSimilarGroupHashCode()
	{
		Node n1 = new Node(ip1, port1);
		Node n2 = new Node(ip1, port2);
		
		Node n3 = new Node(ip1, port1);
		Node n4 = new Node(ip1, port2);
		
		TreeSet<Node> set = new TreeSet<>();
		TreeSet<Node> set2 = new TreeSet<>();
		set.add(n1);set.add(n2);
		set2.add(n3);set2.add(n4);
		Group g1 = new Group(new Configuration(), n1,n2,n3,n4);
		Group g2 = new Group(new Configuration(), n1,n2,n3,n4);
		assert(g1.hashCode() == g2.hashCode());
	}
	
	@Test
	public void testDifferentGroupHashCode()
	{
		Node n1 = new Node(ip1, port1);
		Node n2 = new Node(ip1, port2);
		
		Node n3 = new Node(ip1, port1);
		Node n4 = new Node(ip1, "3002");
		
		TreeSet<Node> set = new TreeSet<>();
		TreeSet<Node> set2 = new TreeSet<>();
		set.add(n1);set.add(n2);
		set2.add(n3);set2.add(n4);
		Group g1 = new Group(new Configuration(), n1,n2);
		Group g2 = new Group(new Configuration(), n3,n4);
		assert(g1.hashCode() != g2.hashCode());
	}
}
