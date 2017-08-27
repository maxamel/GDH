package main.java.gdh;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import main.java.parser.JsonMessageParser;
import main.java.parser.MessageParser;

import java.math.BigInteger;

public class GDHVertex extends AbstractVerticle
{
	private Map<Integer,Group> groupMappings = new HashMap<>();
	private Map<Integer,ExchangeState> stateMappings = new HashMap<>();
	private MessageParser parser;
	private NetServer server;
	private Configuration conf;
	
	@Override
    public void start() throws Exception {
		parser = new JsonMessageParser(groupMappings, stateMappings);
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
                        System.out.println("incoming data: "+ netSocket.localAddress() +" " + buffer.length() + " " + msg);
                        
                        int groupId = parser.parse(msg);
                        if (groupId == -1) {System.out.println(getNode().toString() + " UNKNOWN GROUP " + msg);return;	}	// This node is behind in its info. Come back later...
                        Group group = groupMappings.get(groupId);
                        
                        Buffer outBuffer = Buffer.buffer();
                        outBuffer.appendString(Constants.ack);
                        netSocket.write(outBuffer);
       
                        compute(group);
                    }
                });
        };
        
        server.connectHandler(handler);
        server.listen(Integer.parseInt(conf.getPort()));
		//run();   
	}

	public void run() 
	{  
        broadcastGroupMappings();
	}
	
	public void negotiate(int groupId)
	{
		Group g = groupMappings.get(groupId);		
		broadcast(g);
		compute(g);
	}

	public boolean isDone(int groupId)
	{
		if (stateMappings.containsKey(groupId)) return stateMappings.get(groupId).isDone();
		return false;
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
	
	private void compute(Group g)
	{
		ExchangeState state = stateMappings.get(g.getGroupId());
		if (g.getTreeNodes().size() != state.getRound()+1)
		{
			Node n = g.getNext(conf.getNode());
			BigInteger partial_key = state.getPartial_key().modPow(g.getSecret(),g.getPrime());
			state.incRound();
			state.setPartial_key(partial_key);
			System.out.println("sending " + getNode().toString());
			sendMessage(n, MessageConstructor.roundInfo(state));
		}
		else 
		{
			BigInteger partial_key = state.getPartial_key().modPow(g.getSecret(),g.getPrime());
			state.setPartial_key(partial_key);
			state.done();
		}
	}
	
	public BigInteger getFinalKey(int groupId)
	{
		if (stateMappings.containsKey(groupId))
		{
			if (stateMappings.get(groupId).isDone()) return stateMappings.get(groupId).getPartial_key();
		}
		return null;
	}
	
	private void broadcast(Group group) 
	{
		for (Node n : group.getTreeNodes())
		{
			if (!n.equals(getNode())) sendMessage(n, MessageConstructor.groupInfo(group));
		}
	}
	/*
	private void fileWatch() throws IOException {
		WatchService watcher = FileSystems.getDefault().newWatchService();
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
		
		Path dir = Paths.get(".");
		WatchKey key = dir.register(watcher,
                ENTRY_CREATE,
                ENTRY_MODIFY);
		
		AtomicInteger size = new AtomicInteger(0);
		List<WatchEvent<?>> eventList = new ArrayList<>();
		
		long timerId = vertx.setPeriodic(1000, id -> {
			
			List<WatchEvent<?>> list = key.pollEvents();
			int currSize = list.size();
			if (currSize > 0 && size.get() != currSize) System.out.println("HEYAAA!");
			if (currSize > 0) 
			{
				WatchEvent<Path> event = (WatchEvent<Path>) list.get(list.size()-1);
				Kind<?> kind = event.kind();
				Path fileName = event.context();
				System.out.println(kind.name() + " " + fileName);
				readGroupMapping(fileName.toString());
			}
			eventList.addAll(list);
		});
	}*/
	
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
                Long[] timing = new Long[1];
                timing[0] = vertx.setPeriodic(3000, new Handler<Long>() {

                    @Override
                    public void handle(Long aLong) {
                        socket.handler(new Handler<Buffer>(){
                            @Override
                            public void handle(Buffer buffer) {
                                String reply = buffer.getString(0, buffer.length());
                                if (reply.equals(Constants.ack)) 
                                {
                                	System.out.println(getNode().toString() + " Got an ack for " + msg);
                                	socket.close();
                                	vertx.cancelTimer(timing[0]);
                                }
                            }
                        });
                        socket.write(msg.toString());
                    }
                });
            }
        });
	}
	
	@Override
    public void stop() throws Exception {
		server.close();
	}
	
	/*private void readGroupMapping(String path)
	{
		JSONParser parser = new JSONParser();
		try
		{
			Object obj = parser.parse(new FileReader(path));
			JSONObject json = (JSONObject) obj;
			
			JSONArray groups = (JSONArray) json.get(Constants.groups);
			Iterator<?> iter1 = groups.iterator();
			while(iter1.hasNext())
			{
				JSONArray group = (JSONArray) iter1.next();
				Iterator<?> iter2 = group.iterator();
				TreeSet<Node> set = new TreeSet<>();
				while(iter2.hasNext())
				{
					JSONObject node = (JSONObject) iter2.next();
					String IP = (String) node.get(Constants.ip);
					String port = (String) node.get(Constants.port);
					Node n = new Node(IP, port);
					set.add(n);
				}
				if (!set.isEmpty())
				{
					Group g = new Group(conf, set);
					groupMappings.put(g.getGroupId(),g);
					stateMappings.put(g.getGroupId(), new ExchangeState(g.getGroupId(), g.getGenerator()));
					//vertx.eventBus().publish("groups.new", g);
					broadcast(g);
					compute(g);
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ParseException e) 
		{
			System.out.println("Illegal Json structure in group mappings!");
			e.printStackTrace();
		}
	}*/
}
