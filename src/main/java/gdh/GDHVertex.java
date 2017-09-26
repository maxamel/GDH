package main.java.gdh;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import main.java.parser.JsonMessageParser;
import main.java.parser.MessageConstructor;
import main.java.parser.MessageParser;

import java.math.BigInteger;

/**
 * 
 * @author Max Amelchenko
 * 
 * GDHVertex is an object which participates in a Generalized Diffie-Hellman Key exchange process.
 * 
 * As an AbstractVerticle, it must be deployed in order to be used.
 */
public class GDHVertex extends AbstractVerticle {
    private final Map<Integer, Group> groupMappings = new HashMap<>();
    private final Map<Integer, ExchangeState> stateMappings = new HashMap<>();
    private NetServer server;
    private Configuration conf;

    @Override
    public void start(Future<Void> future) throws Exception {
        MessageParser parser = new JsonMessageParser(groupMappings, stateMappings);
        assert (conf != null);

        NetServerOptions options = new NetServerOptions();
        options.setReceiveBufferSize(Constants.BUFFER_SIZE);
        server = vertx.createNetServer(options);
        Handler<NetSocket> handler = (NetSocket netSocket) -> {
            netSocket.handler((Buffer buffer) -> {
                // parsing message
                String msg = buffer.getString(0, buffer.length());
                conf.getLogger().debug(getNode().toString() + " incoming data: " + netSocket.localAddress() + " "
                        + buffer.length() + " " + msg);

                int groupId = parser.parse(msg);
                if (groupId == -1) {
                // This node is behind in its info. Come back later...
                    conf.getLogger().debug(getNode().toString() + " Unkown group " + msg);
                    return;
                }
                Group group = groupMappings.get(groupId);

                Buffer outBuffer = Buffer.buffer();
                outBuffer.appendString(Constants.ACK);
                netSocket.write(outBuffer);

                compute(group);
            });
        };

        server.connectHandler(handler);
        server.listen(Integer.parseInt(conf.getPort()), res -> {
            if (res.succeeded()) {
                future.complete();
            } else {
                future.fail(res.cause());
            }
        });
        conf.getLogger().info(getNode().toString() + " started listening on: " + conf.getPort());
    }

    /**
     * Start a key exchange process
     * @param groupId 
     *                  the id of the group for which a key exchange will be initiated
     * @return A Future representation of the key
     */
    public CompletableFuture<BigInteger> exchange(int groupId) {
        conf.getLogger().info(getNode().toString() + Constants.NEGO_CALL + groupId);
        Group g = groupMappings.get(groupId);
        broadcast(g);
        CompletableFuture<BigInteger> future = compute(g);
        vertx.setTimer(Constants.NEGO_TIMEOUT, id -> {
            future.completeExceptionally(
                    new TimeoutException(Constants.EXCEPTIONTIMEOUTEXCEEDED + Constants.NEGO_TIMEOUT));
        });
        return future;
    }

    /**
     * Start a key exchange process
     * @param groupId 
     *                  the id of the group for which a key exchange will be initiated
     * @param aHandler 
     *                  the handler which succeeds or fails in accordance to the key exchange outcome
     * @return A Future representation of the key
     */
    public CompletableFuture<BigInteger> exchange(int groupId, Handler<AsyncResult<BigInteger>> aHandler) {
        conf.getLogger().info(getNode().toString() + Constants.NEGO_CALL + groupId);
        Group g = groupMappings.get(groupId);
        ExchangeState state = stateMappings.get(groupId);
        state.registerHandler(aHandler);
        broadcast(g);
        CompletableFuture<BigInteger> future = compute(g);
        vertx.setTimer(Constants.NEGO_TIMEOUT, id -> {
            aHandler.handle(Future.failedFuture(Constants.EXCEPTIONTIMEOUTEXCEEDED + Constants.NEGO_TIMEOUT));
            future.completeExceptionally(
                    new TimeoutException(Constants.EXCEPTIONTIMEOUTEXCEEDED + Constants.NEGO_TIMEOUT));
        });
        return future;
    }

    /**
     * Start a key exchange process
     * @param groupId 
     *                  the id of the group for which a key exchange will be initiated
     * @param aHandler 
     *                  the handler which succeeds or fails in accordance to the key exchange outcome
     * @param timeoutMillis 
     *                  the timeout on the key exchange process
     * @return A Future representation of the key
     */
    public CompletableFuture<BigInteger> exchange(int groupId, Handler<AsyncResult<BigInteger>> aHandler,
            int timeoutMillis) {
        conf.getLogger().info(getNode().toString() + Constants.NEGO_CALL + groupId);
        Group g = groupMappings.get(groupId);
        ExchangeState state = stateMappings.get(groupId);
        state.registerHandler(aHandler);
        broadcast(g);
        CompletableFuture<BigInteger> future = compute(g);
        vertx.setTimer(timeoutMillis, id -> {
            aHandler.handle(Future.failedFuture(Constants.EXCEPTIONTIMEOUTEXCEEDED + timeoutMillis));
            future.completeExceptionally(
                    new TimeoutException(Constants.EXCEPTIONTIMEOUTEXCEEDED + timeoutMillis));
        });
        return future;
    }

    public void setConfiguration(Configuration conf) {
        this.conf = conf;
    }

    public void addGroup(Group g) {
        groupMappings.put(g.getGroupId(), g);
        stateMappings.put(g.getGroupId(), new ExchangeState(g.getGroupId(), g.getGenerator()));
    }
    
    /**
     * Get a Node object from this GDHVertex. The parameters of the Node depend on the Configuration of this GDHVertex.
     * @return a Node object of this GDHVertex.
     */
    public Node getNode() {
        return new Node(conf.getIP(), conf.getPort());
    }

    private CompletableFuture<BigInteger> compute(Group g) {
        ExchangeState state = stateMappings.get(g.getGroupId());
        if (g.getTreeNodes().size() == state.getRound() + 1) {
            BigInteger partial_key = state.getPartial_key().modPow(g.getSecret(), g.getPrime());
            state.setPartial_key(partial_key);
            state.done();
        } else {
            Node n = g.getNext(conf.getNode());
            BigInteger partial_key = state.getPartial_key().modPow(g.getSecret(), g.getPrime());
            state.incRound();
            state.setPartial_key(partial_key);
            conf.getLogger().debug(getNode().toString() + " got key: " + partial_key);
            sendMessage(n, MessageConstructor.roundInfo(state));
        }
        return state.getKey();
    }

    /**
     * Get the Diffie-Hellman key of a group. The actual value of the key may not be available right away, as it is 
     * dependent on the key exchange process. Once this process finishes the key will be available. CompletableFuture
     * has many methods to deal with the asynchronous nature of the result, such as blocking to wait for the result, 
     * returning immediately with a default result, scheduling tasks for after the result is available, and more.
     * 
     * @param groupId 
     *                  the id of the group
     * @return a Future representation of the key 
     */
    public CompletableFuture<BigInteger> getKey(int groupId) {
        return stateMappings.get(groupId).getKey();
    }

    private void broadcast(Group group) {
        for (Node n : group.getTreeNodes()) {
            if (!n.equals(getNode()))
                sendMessage(n, MessageConstructor.groupInfo(group));
        }
    }

    private void sendMessage(Node n, JsonObject msg) {
        NetClientOptions options = new NetClientOptions();
        options.setSendBufferSize(Constants.BUFFER_SIZE);

        NetClient tcpClient = vertx.createNetClient(options);

        tcpClient.connect(Integer.parseInt(n.getPort()), n.getIP(),((AsyncResult<NetSocket> result) -> {
                NetSocket socket = result.result();
                Long[] timingAndRetries = new Long[2];
                for (int t = 0; t < timingAndRetries.length; t++)
                    timingAndRetries[t] = Long.valueOf("0");

                timingAndRetries[0] = vertx.setPeriodic(Constants.SEND_RETRY, ((Long aLong) -> {
                    socket.handler((Buffer buffer) -> {
                        String reply = buffer.getString(0, buffer.length());
                        if (reply.equals(Constants.ACK)) {
                            conf.getLogger().debug(getNode().toString() + " Got an ack from " + n.toString());
                            socket.close();
                            vertx.cancelTimer(timingAndRetries[0]);
                        }

                    });
                    conf.getLogger().debug(getNode().toString() + " Sending data to: " + n.toString() + " " + msg.toString());
                    socket.write(msg.toString());
                    timingAndRetries[1]++;
                    if (timingAndRetries[1] == conf.getRetries()) { 
                        // No more retries left. Exit...
                        conf.getLogger().error(getNode().toString() + " Retry parameter exceeded " + conf.getRetries());
                        socket.close();
                        tcpClient.close();
                        server.close();
                        vertx.cancelTimer(timingAndRetries[1]);
                        vertx.close();
                    }

                }));
            
        }));
    }

    @Override
    public void stop(Future<Void> future) throws Exception {
        server.close(res -> {
            if (res.succeeded()) {
                future.complete();
            } else {
                future.fail(res.cause());
            }
        });
        conf.getLogger().info(getNode().toString() + " stopped listening on: " + conf.getPort());
    }
}
