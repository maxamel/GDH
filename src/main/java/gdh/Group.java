package main.java.gdh;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

public class Group 
{
	private final int groupId;
	private final TreeSet<Node> treeNodes;		// keep order among nodes
	private BigInteger generator;
	private BigInteger prime;
	private BigInteger secret;
	private ExchangeState state;
	
	public Group(TreeSet<Node> set, Configuration conf)
	{
		treeNodes = set;
		byte[] sec = new byte[32];
		Random random = new Random(System.nanoTime());
		random.nextBytes(sec);
		generator = new BigInteger(conf.getGenerator(),16);
		prime = new BigInteger(conf.getPrime(),16);
		secret = new BigInteger(sec);
		groupId = hashCode();
		state = new ExchangeState(groupId);
	}
	
	public Group(TreeSet<Node> set, String generator, String prime)
	{
		treeNodes = set;
		byte[] sec = new byte[32];
		Random random = new Random(System.nanoTime());
		random.nextBytes(sec);
		this.generator = new BigInteger(generator, 16);
		this.prime = new BigInteger(prime, 16);
		secret = new BigInteger(sec);
		groupId = hashCode();
		state = new ExchangeState(groupId);
	}
	
	public int getGroupId() {
		return groupId;
	}
	public TreeSet<Node> getTreeNodes() {
		return treeNodes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((treeNodes == null) ? 0 : treeNodes.hashCode());
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
	
	public ExchangeState getState() {
		return state;
	}
	
	public void setState(ExchangeState state) {
		this.state = state;
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
				if (n.equals(treeNodes.last())) return treeNodes.first();
				return iter.next();
			}
		}
		return null;
	}
}
