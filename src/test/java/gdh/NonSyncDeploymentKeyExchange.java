package test.java.gdh;

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
public class NonSyncDeploymentKeyExchange {

    @Test
    public void testDoubleKeyExchangeUnfinished(TestContext context) {
        int amount = 2;
        testUnfinishedDeployment(amount, context);
    }

    @Test
    public void testTripleKeyExchangeUnfinished(TestContext context) {
        int amount = 3;
        testUnfinishedDeployment(amount, context);
    }

    @Test
    public void testQuadrupleKeyExchangeUnfinished(TestContext context) {
        int amount = 4;
        testUnfinishedDeployment(amount, context);
    }

    @Test
    public void testQuintupleKeyExchangeUnfinished(TestContext context) {
        int amount = 5;
        testUnfinishedDeployment(amount, context);
    }

    @Test
    public void testDoubleKeyExchangeDelayed(TestContext context) {
        int amount = 2;
        testDelayedDeployment(amount, context);
    }

    @Test
    public void testTripleKeyExchangeDelayed(TestContext context) {
        int amount = 3;
        testDelayedDeployment(amount, context);
    }

    @Test
    public void testQuadrupleKeyExchangeDelayed(TestContext context) {
        int amount = 4;
        testDelayedDeployment(amount, context);
    }

    @Test
    public void testQuintupleKeyExchangeDelayed(TestContext context) {
        int amount = 5;
        testDelayedDeployment(amount, context);
    }

    // real deployment and communication between verticles on localhost
    private void testUnfinishedDeployment(int amount, TestContext context) {
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

        Async async = context.async();
        pv.run(verticles[0], res -> {
            if (res.succeeded()) {
                for (int i = 1; i < amount; i++) {
                    pv.run(verticles[i]);
                }
                verticles[0].exchange(g.getGroupId());
                async.complete();
            } else {
                System.out.println("Cannot initiate startup and exchange!");
                Assert.fail();
            }
        });
        async.awaitSuccess();

        for (int j = 0; j < amount - 1; j++)
            try {
                Assert.assertTrue(verticles[j].getKey(g.getGroupId()).get()
                        .equals(verticles[j + 1].getKey(g.getGroupId()).get()));
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

    // real deployment and communication between verticles on localhost
    private void testDelayedDeployment(int amount, TestContext context) {
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

        Async async = context.async();
        pv.run(verticles[0], res -> {
            if (res.succeeded()) {
                verticles[0].exchange(g.getGroupId());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                for (int i = 1; i < amount; i++) {
                    pv.run(verticles[i]);
                }
                async.complete();
            } else {
                System.out.println("Cannot initiate startup and exchange!");
                Assert.fail();
            }
        });
        async.awaitSuccess();

        for (int j = 0; j < amount - 1; j++)
            try {
                Assert.assertTrue(verticles[j].getKey(g.getGroupId()).get()
                        .equals(verticles[j + 1].getKey(g.getGroupId()).get()));
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
