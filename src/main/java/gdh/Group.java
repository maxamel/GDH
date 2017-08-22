package main.java.gdh;

import java.math.BigInteger;
import java.util.Random;
import java.util.TreeSet;

public class Group 
{
	private String groupId;
	private TreeSet<Node> treeNodes;		// keep order among nodes
	private BigInteger g;
	private BigInteger N;
	
	public Group(TreeSet<Node> set)
	{
		treeNodes = set;
		byte[] gen = new byte[256];
		byte[] mod = new byte[256];
		Random random = new Random();
		random.nextBytes(gen);
		random.nextBytes(mod);
		g = new BigInteger(gen);
		N = new BigInteger(mod);
		groupId = String.valueOf(hashCode());
	}
	
	public String getGroupId() {
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

	public void setG(BigInteger g) {
		this.g = g;
	}

	public void setN(BigInteger N) {
		this.N = N;
	}

	public BigInteger getG() {
		return g;
	}
	public BigInteger getN() {
		return N;
	}
}
