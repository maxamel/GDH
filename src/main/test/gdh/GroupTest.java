package main.test.gdh;

import java.util.TreeSet;

import org.junit.Test;

import main.java.gdh.Group;
import main.java.gdh.Node;

public class GroupTest 
{
	@Test
	public void testSameGroupHashCode()
	{
		Node n1 = new Node("1.1.1.1", "3000");
		Node n2 = new Node("1.1.1.1", "3001");
		Node n3 = new Node("2.2.2.2", "3000");
		TreeSet<Node> set = new TreeSet<>();
		set.add(n1);set.add(n2);set.add(n3);
		Group g1 = new Group(null, set);
		Group g2 = new Group(null ,set);
		assert(g1.hashCode() == g2.hashCode());
	}
	
	@Test
	public void testSimilarGroupHashCode()
	{
		Node n1 = new Node("1.1.1.1", "3000");
		Node n2 = new Node("1.1.1.1", "3001");
		
		Node n3 = new Node("1.1.1.1", "3000");
		Node n4 = new Node("1.1.1.1", "3001");
		
		TreeSet<Node> set = new TreeSet<>();
		TreeSet<Node> set2 = new TreeSet<>();
		set.add(n1);set.add(n2);
		set2.add(n3);set2.add(n4);
		Group g1 = new Group(null, set);
		Group g2 = new Group(null, set2);
		assert(g1.hashCode() == g2.hashCode());
	}
	
	@Test
	public void testDifferentGroupHashCode()
	{
		Node n1 = new Node("1.1.1.1", "3000");
		Node n2 = new Node("1.1.1.1", "3001");
		
		Node n3 = new Node("1.1.1.1", "3000");
		Node n4 = new Node("1.1.1.1", "3002");
		
		TreeSet<Node> set = new TreeSet<>();
		TreeSet<Node> set2 = new TreeSet<>();
		set.add(n1);set.add(n2);
		set2.add(n3);set2.add(n4);
		Group g1 = new Group(null, set);
		Group g2 = new Group(null, set2);
		assert(g1.hashCode() != g2.hashCode());
	}
}
