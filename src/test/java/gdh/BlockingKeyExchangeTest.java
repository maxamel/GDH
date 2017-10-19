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
public class BlockingKeyExchangeTest {

    @Test
    public void testDoubleKeyExchange(TestContext context) {
        int amount = 2;
        testBlockingNegotiation(amount, context);
    }
    
    @Test
    public void testTripleKeyExchange(TestContext context) {
        int amount = 3;
        testBlockingNegotiation(amount, context);
    }
    
    @Test
    public void testQuadrupleKeyExchange(TestContext context) {
        int amount = 4;
        testBlockingNegotiation(amount, context);
    }

    // real deployment and communication between verticles on localhost
    private void testBlockingNegotiation(int amount, TestContext context) {
        Async async = context.async();
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

        pv.run(verticles[0], res -> {
            if (res.succeeded()) {
                //async.countDown();
                for (int j=1; j<verticles.length; j++)
                {
                    pv.run(verticles[j]);
                }
                try {
                    BigInteger key = verticles[0].exchange(g.getGroupId()).get();
                    for (GDHVertex v : verticles)
                        Assert.assertTrue(v.getKey(g.getGroupId()).get().equals(key));
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            } else {
                res.cause().printStackTrace();
                return;
            }
        });
        async.awaitSuccess();

        for (int i = 0; i < amount; i++)
            pv.kill(verticles[i], res -> {
                if (res.succeeded()) {
                    async.countDown();
                } else {
                    res.cause().printStackTrace();
                }
            });
    }
}
