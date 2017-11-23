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
 * GDHVertex is an object which participates in a Generalized Diffie-Hellman Key
 * exchange process.
 * 
 * As an AbstractVerticle, it must be deployed in order to be used.
 * 
 * @author Max Amelchenko
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
                conf.getLogger().debug(getNode().toString() + " " + Constants.LOG_IN + " from: " + netSocket.remoteAddress() + " "
                 + buffer.length() + " " + msg);

                int groupId = parser.parse(msg);
                if (groupId == -1) {
                    // This node is behind in its info. Come back later...
                    conf.getLogger().debug(getNode().toString() + " Unkown group " + msg);
                    Buffer outBuffer = Buffer.buffer();
                    outBuffer.appendString(Constants.ACK);
                    netSocket.write(outBuffer);
                    return;
                } else if (groupId == -2) {
                    // receiving doubled messages. Come back later...
                    conf.getLogger().debug(getNode().toString() + " Double message " + msg);
                    Buffer outBuffer = Buffer.buffer();
                    outBuffer.appendString(Constants.ACK);
                    netSocket.write(outBuffer);
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
                conf.getLogger().info(getNode().toString() + " started listening on: " + conf.getPort());
            } else {
                future.fail(res.cause());
                conf.getLogger().info(getNode().toString() + " startup failure: " + conf.getPort());
            }
        });

    }

    /**
     * Start a key exchange process
     * 
     * @param groupId
     *            the id of the group for which a key exchange will be initiated
     * @return A Future representation of the key
     */
    public CompletableFuture<BigInteger> exchange(int groupId) {
        conf.getLogger().info(getNode().toString() + Constants.NEGO_CALL + groupId);
        Group g = groupMappings.get(groupId);
        CompletableFuture<Void> res = broadcast(g);
        CompletableFuture<BigInteger> future = res.thenCompose(s -> compute(g));
        vertx.setTimer(conf.getExchangeTimeout(), id -> {
            future.completeExceptionally(
                    new TimeoutException(Constants.EXCEPTIONTIMEOUTEXCEEDED + conf.getExchangeTimeout()));
        });
        return future;
    }

    /**
     * Start a key exchange process
     * 
     * @param groupId
     *            the id of the group for which a key exchange will be initiated
     * @param aHandler
     *            the handler which succeeds or fails in accordance to the key
     *            exchange outcome
     * @return A Future representation of the key
     */
    public CompletableFuture<BigInteger> exchange(int groupId, Handler<AsyncResult<BigInteger>> aHandler) {
        conf.getLogger().info(getNode().toString() + Constants.NEGO_CALL + groupId);
        Group g = groupMappings.get(groupId);
        ExchangeState state = stateMappings.get(groupId);
        state.registerHandler(aHandler);
        CompletableFuture<Void> res = broadcast(g);
        CompletableFuture<BigInteger> future = res.thenCompose(s -> compute(g));
        long timer[] = new long[1];
        timer[0] = vertx.setTimer(conf.getExchangeTimeout(), id -> {
            if (future.isDone() && !future.isCompletedExceptionally()) {
                aHandler.handle(Future.succeededFuture());
                vertx.cancelTimer(timer[0]);
            } else {
                aHandler.handle(Future.failedFuture(Constants.EXCEPTIONTIMEOUTEXCEEDED + Constants.NEGO_TIMEOUT));
                future.completeExceptionally(
                        new TimeoutException(Constants.EXCEPTIONTIMEOUTEXCEEDED + conf.getExchangeTimeout()));
                vertx.cancelTimer(timer[0]);
            }
        });
        future.exceptionally(e -> {
            aHandler.handle(Future.failedFuture(e.getMessage()));
            vertx.cancelTimer(timer[0]);
            return future.join();
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
     * Get a Node object from this GDHVertex. The parameters of the Node depend
     * on the Configuration of this GDHVertex.
     * 
     * @return a Node object of this GDHVertex.
     */
    public Node getNode() {
        return new Node(conf.getIP(), conf.getPort());
    }

    private CompletableFuture<BigInteger> compute(Group g) {
        ExchangeState state = stateMappings.get(g.getGroupId());
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        if (g.getTreeNodes().size() == state.getRound() + 1) {
            conf.getLogger().debug(getNode().toString() + " Finishing Round: " + state.getRound());
            BigInteger partial_key = state.getPartial_key().modPow(state.getSecret(), g.getPrime());
            state.setPartial_key(partial_key);
            state.done();
            future.complete(true);
        } else {
            Node n = g.getNext(conf.getNode());
            BigInteger partial_key = state.getPartial_key().modPow(state.getSecret(), g.getPrime());
            state.incRound();
            state.setPartial_key(partial_key);
            conf.getLogger().debug(getNode().toString() + " Computing key: " + partial_key);
            future = sendMessage(n, MessageConstructor.roundInfo(state));
        }
        return future.thenCompose(s -> state.getKey());
    }

    /**
     * Get the Diffie-Hellman key of a group. The actual value of the key may
     * not be available right away, as it is dependent on the key exchange
     * process. Once this process finishes the key will be available.
     * CompletableFuture has many methods to deal with the asynchronous nature
     * of the result, such as blocking to wait for the result, returning
     * immediately with a default result, scheduling tasks for after the result
     * is available, and more.
     * 
     * @param groupId
     *            the id of the group
     * @return a Future representation of the key
     */
    public CompletableFuture<BigInteger> getKey(int groupId) {
        return stateMappings.get(groupId).getKey();
    }

    private CompletableFuture<Void> broadcast(Group group) {
        CompletableFuture<Boolean> results[] = new CompletableFuture[group.getTreeNodes().size() - 1];
        int i = 0;
        for (Node n : group.getTreeNodes()) {
            if (!n.equals(getNode())) {
                results[i] = sendMessage(n, MessageConstructor.groupInfo(group));
                i++;
            }
        }
        return CompletableFuture.allOf(results);
    }

    private CompletableFuture<Boolean> sendMessage(Node n, JsonObject msg) {
        NetClientOptions options = new NetClientOptions();
        options.setSendBufferSize(Constants.BUFFER_SIZE);

        NetClient tcpClient = vertx.createNetClient(options);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Long[] timingAndRetries = new Long[2];
        for (int t = 0; t < timingAndRetries.length; t++)
            timingAndRetries[t] = Long.valueOf("0");

        timingAndRetries[0] = vertx.setPeriodic(Constants.SEND_RETRY, ((Long myLong) -> {
            tcpClient.connect(Integer.parseInt(n.getPort()), n.getIP(), ((AsyncResult<NetSocket> result) -> {
                NetSocket socket = result.result();
                if (socket != null) {
                    socket.handler((Buffer buffer) -> {
                        String reply = buffer.getString(0, buffer.length());
                        if (reply.equals(Constants.ACK)) {
                            conf.getLogger().debug(getNode().toString() + " Got an ack from " + n.toString());
                            future.complete(true);
                            vertx.cancelTimer(timingAndRetries[0]);
                            socket.close();
                            tcpClient.close();
                        }

                    });
                    conf.getLogger().debug(getNode().toString() + " " + Constants.LOG_OUT + " to: " + 
                    n.toString() + " " + msg.toString());
                    socket.write(msg.toString());
                }
                timingAndRetries[1]++;
                if (timingAndRetries[1] == conf.getRetries()) {
                    // No more retries left. Exit...
                    conf.getLogger().error(getNode().toString() + " Retry parameter exceeded " + conf.getRetries());
                    if (socket != null)
                        socket.close();
                    vertx.cancelTimer(timingAndRetries[0]);
                    tcpClient.close();
                    server.close();
                    future.completeExceptionally(
                            new TimeoutException(Constants.EXCEPTIONRETRIESEXCEEDED + conf.getRetries()));
                }
            }));
        }));
        return future;
    }

    @Override
    public void stop(Future<Void> future) throws Exception {
        server.close(res -> {
            if (res.succeeded()) {
                future.complete();
                conf.getLogger().info(getNode().toString() + " stopped listening on: " + conf.getPort());
            } else {
                future.fail(res.cause());
                conf.getLogger().info(getNode().toString() + " stoppage failure: " + conf.getPort());
            }
        });
    }
}
