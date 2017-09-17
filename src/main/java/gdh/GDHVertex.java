package main.java.gdh;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import main.java.parser.JsonMessageParser;
import main.java.parser.MessageConstructor;
import main.java.parser.MessageParser;

import java.math.BigInteger;

public class GDHVertex extends AbstractVerticle
{
	private final Map<Integer,Group> groupMappings = new HashMap<>();
	private final Map<Integer,ExchangeState> stateMappings = new HashMap<>();
	private MessageParser parser;
	private NetServer server;
	private Configuration conf;
	private final Logger log4jLogger = LoggerFactory.getLogger(Log4j2LogDelegateFactory.class);
	
	@Override
    public void start(Future<Void> future) throws Exception {
		parser = new JsonMessageParser(groupMappings, stateMappings);
		assert (conf != null);
		NetServerOptions options = new NetServerOptions();
		options.setReceiveBufferSize(2500);
        server = vertx.createNetServer(options);
        Handler<NetSocket> handler = (NetSocket netSocket) -> {
                netSocket.handler(new Handler<Buffer>() {

                    @Override
                    public void handle(Buffer buffer) {
                        //System.out.println("incoming data: "+buffer.length());
                        // parsing message
                        String msg = buffer.getString(0,buffer.length());
                        log4jLogger.debug(getNode().toString() + " incoming data: "+ netSocket.localAddress() +" " + buffer.length() + " " + msg);
                        
                        int groupId = parser.parse(msg);
                        if (groupId == -1) 
                        {// This node is behind in its info. Come back later...
                        	log4jLogger.debug(getNode().toString() + " Unkown group " + msg);
                        	return;	
                        }	
                        Group group = groupMappings.get(groupId);
                        
                        Buffer outBuffer = Buffer.buffer();
                        outBuffer.appendString(Constants.ack);
                        netSocket.write(outBuffer);
       
                        compute(group);
                    }
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
        log4jLogger.info(getNode().toString() + " started listening on: "+ conf.getPort());
	}

	public void run() 
	{  
        broadcastGroupMappings();
	}
	
	public CompletableFuture<BigInteger> negotiate(int groupId)
	{
		log4jLogger.info(getNode().toString() + " called negotiation for group " + groupId);
		Group g = groupMappings.get(groupId);
		broadcast(g);
		CompletableFuture<BigInteger> future = compute(g);
		vertx.setTimer(60000, id -> {
			future.completeExceptionally(new TimeoutException(Constants.exceptionTimeoutExceeded + 60000));
		});
		return future;
	}
	
	public CompletableFuture<BigInteger> negotiate(int groupId, Handler<AsyncResult<BigInteger>> aHandler)
	{
		log4jLogger.info(getNode().toString() + " called negotiation for group " + groupId);
		Group g = groupMappings.get(groupId);	
		ExchangeState state = stateMappings.get(groupId);
		state.registerHandler(aHandler);
		broadcast(g);
		CompletableFuture<BigInteger> future = compute(g);
		vertx.setTimer(60000, id -> {
			aHandler.handle(Future.failedFuture(Constants.exceptionTimeoutExceeded + 60000));
			future.completeExceptionally(new TimeoutException(Constants.exceptionTimeoutExceeded + 60000));
		});
		return future;
	}
	
	public CompletableFuture<BigInteger> negotiate(int groupId, Handler<AsyncResult<BigInteger>> aHandler, int timeoutMillis)
	{
		log4jLogger.info(getNode().toString() + " called negotiation for group " + groupId);
		Group g = groupMappings.get(groupId);	
		ExchangeState state = stateMappings.get(groupId);
		state.registerHandler(aHandler);
		broadcast(g);
		CompletableFuture<BigInteger> future = compute(g);
		vertx.setTimer(timeoutMillis, id -> {
			aHandler.handle(Future.failedFuture(Constants.exceptionTimeoutExceeded + timeoutMillis));
			future.completeExceptionally(new TimeoutException(Constants.exceptionTimeoutExceeded + timeoutMillis));
		});
		return future;
	}

	private void broadcastGroupMappings() 
	{
		Iterator<Group> iter = groupMappings.values().iterator();
		while (iter.hasNext())
		{
			Group g = iter.next();
			broadcast(g);
			compute(g);
		}
	}

	public void setConfiguration(Configuration conf)
	{
		this.conf = conf;
	}
	
	public void addGroup(Group g)
	{
		groupMappings.put(g.getGroupId(), g);
		stateMappings.put(g.getGroupId(), new ExchangeState(g.getGroupId(), g.getGenerator()));
	}

	public Node getNode()
	{
		return new Node(conf.getIP(), conf.getPort());
	}
	
	private CompletableFuture<BigInteger> compute(Group g)
	{
		ExchangeState state = stateMappings.get(g.getGroupId());
		if (g.getTreeNodes().size() == state.getRound()+1)
		{
			BigInteger partial_key = state.getPartial_key().modPow(g.getSecret(),g.getPrime());
			state.setPartial_key(partial_key);
			state.done();
		}
		else 
		{
			Node n = g.getNext(conf.getNode());
			BigInteger partial_key = state.getPartial_key().modPow(g.getSecret(),g.getPrime());
			state.incRound();
			state.setPartial_key(partial_key);
			log4jLogger.debug(getNode().toString() + " got key: " + partial_key);
			sendMessage(n, MessageConstructor.roundInfo(state));
		}
		return state.getKey();
	}
	
	public CompletableFuture<BigInteger> getKey(int groupId)
	{
		return stateMappings.get(groupId).getKey();
	}
	
	private void broadcast(Group group) 
	{
		for (Node n : group.getTreeNodes())
		{
			if (!n.equals(getNode())) sendMessage(n, MessageConstructor.groupInfo(group));
		}
	}
	
	private void sendMessage(Node n, JsonObject msg)
	{	
		NetClientOptions options = new NetClientOptions();
		options.setSendBufferSize(2500);
		
		NetClient tcpClient = vertx.createNetClient(options);
		
        tcpClient.connect(Integer.parseInt(n.getPort()), n.getIP(),
            new Handler<AsyncResult<NetSocket>>(){

            @Override
            public void handle(AsyncResult<NetSocket> result) {
                NetSocket socket = result.result();
                Long[] timingAndRetries = new Long[2];
                for (int t=0; t<timingAndRetries.length; t++) timingAndRetries[t] = Long.valueOf("0");
                timingAndRetries[0] = vertx.setPeriodic(2000, new Handler<Long>() {

                    @Override
                    public void handle(Long aLong) {
                        socket.handler(new Handler<Buffer>(){
                            @Override
                            public void handle(Buffer buffer) {
                                String reply = buffer.getString(0, buffer.length());
                                if (reply.equals(Constants.ack)) 
                                {
                                	log4jLogger.debug(getNode().toString() + " Got an ack from " + n.toString());
                                	socket.close();
                                	vertx.cancelTimer(timingAndRetries[0]);
                                }
                            }
                        });
                        log4jLogger.debug(getNode().toString() + " Sending data to: " + n.toString() + " " + msg.toString());
                        socket.write(msg.toString());
                        timingAndRetries[1]++;
                        if (timingAndRetries[1] == conf.getRetries())
                        {	// No more retries left. Exit...
                        	log4jLogger.error(getNode().toString() + " Retry parameter exceeded " + conf.getRetries());
                        	socket.close();
                        	tcpClient.close();
                        	server.close();
                        	vertx.cancelTimer(timingAndRetries[1]);
                        	vertx.close();
                        }
                    }
                });
            }
        });
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
		log4jLogger.info(getNode().toString() + " stopped listening on: " + conf.getPort());
	}
}
