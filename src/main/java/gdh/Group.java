package main.java.gdh;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * Group is an object which represents a group of participants(Nodes) in a
 * Diffie-Hellman key exchange.
 * 
 * It is generally identified by its groupId which is generated from the hash
 * code of its Nodes.
 * 
 * The Nodes are organized in a TreeSet to create a total lexicographical order.
 * 
 * @author Max Amelchenko
 */
public class Group {
    private int groupId;
    private TreeSet<Node> treeNodes; // keep order among nodes
    private BigInteger generator;
    private BigInteger prime;

    public Group(Configuration conf, Node... nodes) {
        initGroup(conf, Arrays.asList(nodes));
    }

    public Group(Configuration conf, Collection<Node> nodes) {
        initGroup(conf, nodes);
    }

    public Group(String gen, String prime, Collection<Node> nodes) {
        Configuration conf = new Configuration();
        conf.setGenerator(gen).setPrime(prime);
        initGroup(conf, nodes);
    }

    /**
     * Initiate a group with a Configuration and a collection of Nodes
     * 
     * @param conf
     *            The Configuration of this Group
     * @param nodes
     *            The participants of this Group
     */
    private void initGroup(Configuration conf, Collection<Node> nodes) {
        treeNodes = new TreeSet<>();
        for (Node n : nodes)
            treeNodes.add(n);

        generator = new BigInteger(conf.getGenerator(), 16);
        prime = new BigInteger(conf.getPrime(), 16);
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
     * Checks for the equality of this Group and the obj group. Two Groups are
     * considered equal if they are the same object or if they consist of the
     * same Nodes. This is done because a Group is essentially the Nodes it
     * represents.
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

    /**
     * Get the Node which succeeds the parameter Node. Node Beta succeeds Node
     * Alpha if during the Diffie-Hellman key exchange Node Alpha sends messages
     * to Node Beta. There can only be one successor per Node.
     * 
     * @param curr
     *            The Node for which a successor will be returned
     * @return the Node succeeding the Node curr
     */
    public Node getNext(Node curr) {
        Iterator<Node> iter = treeNodes.iterator();
        while (iter.hasNext()) {
            Node n = iter.next();
            if (n.equals(curr)) {
                if (n.equals(treeNodes.last()))
                    return treeNodes.first();

                return iter.next();
            }
        }
        return null;
    }
}
