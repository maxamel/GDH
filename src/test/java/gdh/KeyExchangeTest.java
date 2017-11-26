package test.java.gdh;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import main.java.gdh.Configuration;
import main.java.gdh.GDHVertex;
import main.java.gdh.Group;
import main.java.gdh.PrimaryVertex;

@RunWith(VertxUnitRunner.class)
public class KeyExchangeTest {

    @Test
    public void testDoubleKeyExchange(TestContext context) {
        int amount = 2;
        testNegotiation(amount, context);
    }

    @Test
    public void testTripleKeyExchange(TestContext context) {
        int amount = 3;
        testNegotiation(amount, context);
    }

    @Test
    public void testQuadrupleKeyExchange(TestContext context) {
        int amount = 4;
        testNegotiation(amount, context);
    }

    @Test
    public void testQuintupleKeyExchange(TestContext context) {
        int amount = 5;
        testNegotiation(amount, context);
    }
    
    @Test
    public void testSextupleKeyExchange(TestContext context) {
        int amount = 5;
        testNegotiation(amount, context);
    }
    
    @Test
    public void testSeptupleKeyExchange(TestContext context) {
        int amount = 5;
        testNegotiation(amount, context);
    }

    // real deployment and communication between verticles on localhost
    private void testNegotiation(int amount, TestContext context) {
        // Vertx vertx = Vertx.vertx();
        PrimaryVertex pv = new PrimaryVertex();
        GDHVertex[] verticles = new GDHVertex[amount];
        Configuration[] confs = new Configuration[amount];

        for (int i = 0; i < amount; i++) {
            verticles[i] = new GDHVertex();
            confs[i] = new Configuration();
            String port = amount + "07" + i;
            confs[i].setIP("localhost").setPort(port).setLogLevel(Level.DEBUG);
            verticles[i].setConfiguration(confs[i]);
        }
        List<GDHVertex> list = new ArrayList<>(Arrays.asList(verticles));

        Group g = new Group(confs[0], list.stream().map(y -> y.getNode()).collect(Collectors.toList()));
        verticles[0].addGroup(g);

        Async async1 = context.async(amount);
        for (int i = 0; i < amount; i++)
            pv.run(verticles[i], res -> {
                if (res.succeeded()) {
                    async1.countDown();
                } else {
                    res.cause().printStackTrace();
                    return;
                }
            });
        async1.awaitSuccess();

        BigInteger key = null;
        try {
            key = verticles[0].exchange(g.getGroupId()).get();

            for (int j = 0; j < verticles.length; j++) {
                Assert.assertEquals(verticles[j].getKey(g.getGroupId()).get(), key);
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Async async2 = context.async(amount);
        for (int i = 0; i < amount; i++)
            pv.kill(verticles[i], res -> {
                if (res.succeeded()) {
                    async2.countDown();
                } else {
                    res.cause().printStackTrace();
                }
            });
        async2.awaitSuccess();

    }
}
