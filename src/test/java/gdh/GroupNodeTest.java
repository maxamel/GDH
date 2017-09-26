package test.java.gdh;

import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import main.java.gdh.Configuration;
import main.java.gdh.Group;
import main.java.gdh.Node;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class GroupNodeTest {
    private static final String ip1 = "1.1.1.1";
    private static final String port1 = "3000";
    private static final String port2 = "3001";

    @Test
    public void testSameGroup() {
        Node n1 = new Node(ip1, port1);
        Node n2 = new Node(ip1, port2);
        Node n3 = new Node("2.2.2.2", port1);

        Group g1 = new Group(new Configuration(), n1, n2, n3);
        Group g2 = new Group(new Configuration(), n1, n2, n3);
        Assert.assertFalse(n1.equals(n2));
        Assert.assertFalse(n1.equals(n3));
        Assert.assertTrue(g1.hashCode() == g2.hashCode());
        Assert.assertTrue(g1.equals(g2));
    }

    @Test
    public void testSimilarGroup() {
        Node n1 = new Node(ip1, port1);
        Node n2 = new Node(ip1, port2);

        Node n3 = new Node(ip1, port1);
        Node n4 = new Node(ip1, port2);

        TreeSet<Node> set = new TreeSet<>();
        TreeSet<Node> set2 = new TreeSet<>();
        set.add(n1);
        set.add(n2);
        set2.add(n3);
        set2.add(n4);
        Group g1 = new Group(new Configuration(), n1, n2, n3, n4);
        Group g2 = new Group(new Configuration(), n1, n2, n3, n4);
        Assert.assertTrue(n1.equals(n3));
        Assert.assertTrue(g1.hashCode() == g2.hashCode());
        Assert.assertTrue(g1.equals(g2));
    }

    @Test
    public void testDifferentGroup() {
        Node n1 = new Node(ip1, port1);
        Node n2 = new Node(ip1, port2);

        Node n3 = new Node(ip1, port1);
        Node n4 = new Node(ip1, "3002");

        TreeSet<Node> set = new TreeSet<>();
        TreeSet<Node> set2 = new TreeSet<>();
        set.add(n1);
        set.add(n2);
        set2.add(n3);
        set2.add(n4);
        Group g1 = new Group(new Configuration(), n1, n2);
        Group g2 = new Group(new Configuration(), n3, n4);
        Assert.assertTrue(n1.equals(n3));
        Assert.assertTrue(g1.hashCode() != g2.hashCode());
        Assert.assertTrue(!g1.equals(g2));
    }
}
