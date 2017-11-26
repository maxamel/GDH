package test.java.gdh;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
public class AsyncKeyExchangeTest {

    @Test
    public void testDoubleKeyExchange(TestContext context) {
        int amount = 2;
        testAsyncNegotiation(amount, context);
    }

    @Test
    public void testTripleKeyExchange(TestContext context) {
        int amount = 3;
        testAsyncNegotiation(amount, context);
    }

    @Test
    public void testQuadrupleKeyExchange(TestContext context) {
        int amount = 4;
        testAsyncNegotiation(amount, context);
    }

    // real deployment and communication between verticles on localhost
    private void testAsyncNegotiation(int amount, TestContext context) {
        PrimaryVertex pv = new PrimaryVertex();
        GDHVertex[] verticles = new GDHVertex[amount];
        Configuration[] confs = new Configuration[amount];

        for (int i = 0; i < amount; i++) {
            verticles[i] = new GDHVertex();
            confs[i] = new Configuration();
            String port = (amount * 2) + "08" + i;
            confs[i].setIP("localhost").setPort(port).setLogLevel(Level.DEBUG);
            verticles[i].setConfiguration(confs[i]);
        }
        List<GDHVertex> list = new ArrayList<>(Arrays.asList(verticles));

        Group g = new Group(confs[0], list.stream().map(y -> y.getNode()).collect(Collectors.toList()));
        verticles[0].addGroup(g);

        Async async1 = context.async(amount);
        for (int i = 0; i < amount; i++) {
            pv.run(verticles[i], res -> {
                if (res.failed()) {
                    res.cause().printStackTrace();
                    return;
                } else
                    async1.countDown();
            });
        }
        async1.awaitSuccess();

        Async async2 = context.async(1);
        CompletableFuture<BigInteger> key = verticles[0].exchange(g.getGroupId(), result -> {
            Assert.assertTrue(result.succeeded());
            async2.countDown();
        });
        async2.awaitSuccess();

        for (int j = 0; j < amount; j++)
            try {
                Assert.assertTrue(verticles[j].getKey(g.getGroupId()).get().equals(key.get()));
            } catch (InterruptedException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        Async async3 = context.async(amount);
        for (int i = 0; i < amount; i++)
            pv.kill(verticles[i], res -> {
                if (res.failed()) {
                    res.cause().printStackTrace();
                } else
                    async3.countDown();
            });
        async3.awaitSuccess();
    }
}
