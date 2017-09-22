package main.java.gdh;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class Group 
{
	private int groupId;
	private TreeSet<Node> treeNodes;		// keep order among nodes
	private BigInteger generator;
	private BigInteger prime;
	private BigInteger secret;
	
	public Group(Configuration conf, Node... nodes)
	{
		initGroup(conf, Arrays.asList(nodes));
	}
	
	public Group(Configuration conf, Collection<Node> nodes)
	{
		initGroup(conf, nodes);
	}
	
	public Group(String gen, String prime, Collection<Node> nodes)
	{
		Configuration conf = new Configuration();
		conf.setGenerator(gen).setPrime(prime);
		initGroup(conf, nodes);
	}

	private void initGroup(Configuration conf, Collection<Node> nodes) {
		treeNodes = new TreeSet<>();
		for (Node n : nodes)
			treeNodes.add(n);
		byte[] sec = new byte[32];
		Random random = new SecureRandom();
		random.nextBytes(sec);
		generator = new BigInteger(conf.getGenerator(),16);
		prime = new BigInteger(conf.getPrime(),16);
		secret = (new BigInteger(sec)).abs();
		groupId = hashCode();
	}
	
	public int getGroupId() {
		return groupId;
	}
	public SortedSet<Node> getTreeNodes() {
		return new TreeSet<>(treeNodes);
	}

	@Override
	public final int hashCode() {
		final int primal = 31;
		int result = 1;
		result = primal * result + ((treeNodes == null) ? 0 : treeNodes.hashCode());
		return result;
	}

	/**
	 *   Checks for the equality of this Group and the obj group.
	 *   Two Groups are considered equal if they are the same object or
	 *   if they consist of the same Nodes. This is done so the same Group 
	 *   will not link to two different Nodes.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (treeNodes == null) {
			if (other.treeNodes != null)
				return false;
		} else if (!treeNodes.equals(other.treeNodes))
			return false;
		return true;
	}

	public void setGenerator(BigInteger g) {
		this.generator = g;
	}

	public void setPrime(BigInteger N) {
		this.prime = N;
	}

	public BigInteger getGenerator() {
		return generator;
	}
	
	public BigInteger getPrime() {
		return prime;
	}
	
	public BigInteger getSecret()
	{
		return secret;
	}
	
	public Node getNext(Node curr)
	{
		Iterator<Node> iter = treeNodes.iterator();
		while(iter.hasNext())
		{
			Node n = iter.next();
			if (n.equals(curr))
			{
				if (n.equals(treeNodes.last())) 
					return treeNodes.first();
				
				return iter.next();
			}
		}
		return null;
	}
}
