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
import main.java.gdh.Constants;
import main.java.gdh.GDHVertex;
import main.java.gdh.Group;
import main.java.gdh.PrimaryVertex;

@RunWith(VertxUnitRunner.class)
public class ExceptionTest {
    private static final String localhost = "localhost";
    
    @Test(expected = ExecutionException.class)
    public void testVerticleDownTimeout(TestContext context) throws InterruptedException, ExecutionException {
        Async async = context.async();
        int amount = 2;
        PrimaryVertex pv = new PrimaryVertex();
        GDHVertex[] verticles = new GDHVertex[amount];
        Configuration[] confs = new Configuration[amount];

        for (int i = 0; i < amount; i++) {
            verticles[i] = new GDHVertex();
            confs[i] = new Configuration();
            String port = amount + "08" + i;
            confs[i].setIP(localhost).setPort(port).setLogLevel(Level.DEBUG).setExchangeTimeout(5000);
            verticles[i].setConfiguration(confs[i]);
        }
        List<GDHVertex> list = new ArrayList<>(Arrays.asList(verticles));

        Group g = new Group(confs[0], list.stream().map(y -> y.getNode()).collect(Collectors.toList()));
        verticles[0].addGroup(g);

        for (int i = 0; i < amount-1; i++)
            pv.run(verticles[i], res -> {
                if (res.succeeded()) {
                    async.countDown();
                } else {
                    res.cause().printStackTrace();
                    return;
                }
            });
        async.awaitSuccess();

        try {
            BigInteger key = verticles[0].exchange(g.getGroupId()).get();
            for (int j = 0; j < verticles.length; j++) {
                Assert.assertEquals(verticles[j].getKey(g.getGroupId()).get(), key);
            }
        } catch (ExecutionException e) {
            for (int i = 0; i < amount-1; i++)
                pv.kill(verticles[i], res -> {
                    if (res.succeeded()) {
                        async.countDown();
                    } else {
                        res.cause().printStackTrace();
                    }
                });
            throw e;
        }
    }
    
    @Test(expected = ExecutionException.class)
    public void testVerticleDownRetries(TestContext context) throws InterruptedException, ExecutionException{
        Async async = context.async();
        int amount = 2;
        PrimaryVertex pv = new PrimaryVertex();
        GDHVertex[] verticles = new GDHVertex[amount];
        Configuration[] confs = new Configuration[amount];

        for (int i = 0; i < amount; i++) {
            verticles[i] = new GDHVertex();
            confs[i] = new Configuration();
            String port = amount + "08" + i;
            confs[i].setIP(localhost).setPort(port).setLogLevel(Level.DEBUG).setExchangeTimeout(60000);
            verticles[i].setConfiguration(confs[i]);
        }
        List<GDHVertex> list = new ArrayList<>(Arrays.asList(verticles));

        Group g = new Group(confs[0], list.stream().map(y -> y.getNode()).collect(Collectors.toList()));
        verticles[0].addGroup(g);

        for (int i = 0; i < amount-1; i++)
            pv.run(verticles[i], res -> {
                if (res.succeeded()) {
                    async.countDown();
                } else {
                    res.cause().printStackTrace();
                    return;
                }
            });
        async.awaitSuccess();

        try {
            BigInteger key = verticles[0].exchange(g.getGroupId()).get();
            for (int j = 0; j < verticles.length; j++) {
                Assert.assertEquals(verticles[j].getKey(g.getGroupId()).get(), key);
            }
        } catch (ExecutionException e) {
            for (int i = 0; i < amount-1; i++)
                pv.kill(verticles[i], res -> {
                    if (res.succeeded()) {
                        async.countDown();
                    } else {
                        res.cause().printStackTrace();
                    }
                });
            throw e;
        }
    }
    
    @Test
    public void testVerticleDownRetriesAsync(TestContext context) throws InterruptedException, ExecutionException{
        Async async = context.async();
        int amount = 2;
        PrimaryVertex pv = new PrimaryVertex();
        GDHVertex[] verticles = new GDHVertex[amount];
        Configuration[] confs = new Configuration[amount];

        for (int i = 0; i < amount; i++) {
            verticles[i] = new GDHVertex();
            confs[i] = new Configuration();
            String port = amount + "08" + i;
            confs[i].setIP(localhost).setPort(port).setLogLevel(Level.DEBUG).setExchangeTimeout(60000);
            verticles[i].setConfiguration(confs[i]);
        }
        List<GDHVertex> list = new ArrayList<>(Arrays.asList(verticles));

        Group g = new Group(confs[0], list.stream().map(y -> y.getNode()).collect(Collectors.toList()));
        verticles[0].addGroup(g);

        for (int i = 0; i < amount-1; i++)
            pv.run(verticles[i], res -> {
                if (res.succeeded()) {
                    async.countDown();
                } else {
                    res.cause().printStackTrace();
                    return;
                }
            });
        async.awaitSuccess();

        verticles[0].exchange(g.getGroupId(),res -> {
        	Assert.assertTrue(res.failed());
        	Assert.assertTrue(res.cause().getMessage().contains(Constants.EXCEPTIONRETRIESEXCEEDED));
        });
        
        //for (int j = 0; j < verticles.length; j++) 
        //   Assert.assertEquals(verticles[j].getKey(g.getGroupId()).get(), key);
                
        // vertx.deploymentIDs().forEach(vertx::undeploy);
        for (int i = 0; i < amount-1; i++)
            pv.kill(verticles[i], res -> {
                if (res.succeeded()) {
                    async.countDown();
                } else {
                    res.cause().printStackTrace();
                }
            });
    }
    
    @Test
    public void testVerticleDownTimeoutAsync(TestContext context) throws InterruptedException, ExecutionException {
        Async async = context.async();
        int amount = 2;
        PrimaryVertex pv = new PrimaryVertex();
        GDHVertex[] verticles = new GDHVertex[amount];
        Configuration[] confs = new Configuration[amount];

        for (int i = 0; i < amount; i++) {
            verticles[i] = new GDHVertex();
            confs[i] = new Configuration();
            String port = amount + "08" + i;
            confs[i].setIP(localhost).setPort(port).setLogLevel(Level.DEBUG).setExchangeTimeout(5000);
            verticles[i].setConfiguration(confs[i]);
        }
        List<GDHVertex> list = new ArrayList<>(Arrays.asList(verticles));

        Group g = new Group(confs[0], list.stream().map(y -> y.getNode()).collect(Collectors.toList()));
        verticles[0].addGroup(g);

        for (int i = 0; i < amount-1; i++)
            pv.run(verticles[i], res -> {
                if (res.succeeded()) {
                    async.countDown();
                } else {
                    res.cause().printStackTrace();
                    return;
                }
            });
        async.awaitSuccess();

        verticles[0].exchange(g.getGroupId(),res -> {
        	Assert.assertTrue(res.failed());
        	Assert.assertTrue(res.cause().getMessage().contains(Constants.EXCEPTIONTIMEOUTEXCEEDED));
        });
        
        // vertx.deploymentIDs().forEach(vertx::undeploy);
        for (int i = 0; i < amount-1; i++)
            pv.kill(verticles[i], res -> {
                if (res.succeeded()) {
                    async.countDown();
                } else {
                    res.cause().printStackTrace();
                }
            });
    }
}
