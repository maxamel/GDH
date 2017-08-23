package main.java.gdh;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import main.java.parser.JsonMessageParser;
import main.java.parser.MessageParser;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;

public class GDHVertex extends AbstractVerticle
{
	private Map<Integer,Group> groupMappings = new HashMap<>();
	private MessageParser parser;
	private NetServer server;
	private Configuration conf;
	
	@Override
    public void start() throws Exception {
		//conf = readConfigFile();
		
		//fileWatch();
		run();   
	}

	public void run() 
	{
		parser = new JsonMessageParser(groupMappings);
		
        server = vertx.createNetServer();
        
        Handler<NetSocket> handler = (NetSocket netSocket) -> {
                netSocket.handler(new Handler<Buffer>() {

                    @Override
                    public void handle(Buffer buffer) {
                        System.out.println("incoming data: "+buffer.length());
                        // parsing message
                        String msg = buffer.getString(0,buffer.length());
                        Group g = parser.parse(msg);
                        groupMappings.put(g.getGroupId(), g);
                        compute(g);
                    }

                });
        };
        
        server.connectHandler(handler);
        server.listen(Integer.parseInt(conf.getPort()));
        
        EventBus bus = vertx.eventBus();
        
        broadcastGroupMappings();
        
        bus.consumer("groups.new", message -> {
      	  System.out.println("I have been given the task to distribute a new group: " + message.body());
      	  Group group = (Group) message.body();
      	  broadcast(group);
      	});
	}
	
	private void broadcastGroupMappings() 
	{
		Iterator<Group> iter = groupMappings.values().iterator();
		while (iter.hasNext())
		{
			Group g = iter.next();
			broadcast(g);
		}
	}

	public void setConfiguration(Configuration conf)
	{
		this.conf = conf;
		
	}
	
	public void addGroup(Group g)
	{
		groupMappings.put(g.getGroupId(), g);
	}

	public Node getNode()
	{
		return new Node(conf.getIP(), conf.getPort());
	}
	
	private void compute(Group g)
	{
		System.out.println("HEY");
		if (g.getTreeNodes().size() != g.getState().getRound()+1)
		{
			Node n = g.getNext(conf.getNode());
			BigInteger partial_key = g.getState().getPartial_key().modPow(g.getSecret(),g.getPrime());
			ExchangeState state = g.getState();
			state.setKey(partial_key);
			state.incRound();
			g.setState(state);
			sendMessage(n, MessageConstructor.groupInfo(g));
		}
		else System.out.println("Final key: " + g.getState().getPartial_key());
	}
	
	public Future<BigInteger> getKey(int groupId)
	{
		return null;
	}
	
	private void broadcast(Group group) 
	{
		for (Node n : group.getTreeNodes())
		{
			if (!n.equals(getNode())) sendMessage(n, MessageConstructor.groupInfo(group));
		}
	}

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
	}
	
	private void sendMessage(Node n, JSONObject msg)
	{
		NetClient client = vertx.createNetClient();
		client.connect(Integer.parseInt(n.getPort()), n.getIP(), msg.toJSONString(), res -> {
			if (res.succeeded()) {
				System.out.println("Connected!" + res.result());
			}
			else System.out.println("Failure!");
		});
		client.close();
	}
	
	@Override
    public void stop() throws Exception {
		server.close();
	}

	private Configuration readConfigFile()
	{
		JSONParser parser = new JSONParser();
		Configuration conf = new Configuration();
		try
		{
			Object obj = parser.parse(new FileReader("configuration"));
			JSONObject json = (JSONObject) obj;
			
			String IP = (String) json.get(Constants.ip);
			String port = (String) json.get(Constants.port);
			
			conf.setIP(IP).setPort(port);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return conf;
	}
	
	private void readGroupMapping(String path)
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
					Group g = new Group(set, conf);
					groupMappings.put(g.hashCode(),g);
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
	}
}
