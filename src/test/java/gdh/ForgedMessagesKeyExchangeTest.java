package test.java.gdh;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import main.java.gdh.Configuration;
import main.java.gdh.ExchangeState;
import main.java.gdh.GDHVertex;
import main.java.gdh.Group;
import main.java.gdh.Node;
import main.java.gdh.PrimaryVertex;
import main.java.parser.MessageConstructor;

@RunWith(VertxUnitRunner.class)
public class ForgedMessagesKeyExchangeTest {

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
            CompletableFuture<BigInteger> bigint = verticles[0].exchange(g.getGroupId());
            // double messages check
            Method method1 = verticles[0].getClass().getDeclaredMethod("broadcast", Group.class);
            method1.setAccessible(true);
            method1.invoke(verticles[0],g);     
            
            // sending message of unknown group
            Method method2 = verticles[0].getClass().getDeclaredMethod("sendMessage", Node.class, JsonObject.class);
            method2.setAccessible(true);
            method2.invoke(verticles[0],verticles[1].getNode(), MessageConstructor.roundInfo(new ExchangeState(45622, BigInteger.TEN)));
            
            key = bigint.get();
            for (int j = 0; j < verticles.length; j++) {
                Assert.assertEquals(verticles[j].getKey(g.getGroupId()).get(), key);
            }
        } catch (InterruptedException | ExecutionException | SecurityException | 
                IllegalArgumentException | NoSuchMethodException | IllegalAccessException |
                InvocationTargetException e) {
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
