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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import static java.nio.file.StandardWatchEventKinds.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class GDHVertex extends AbstractVerticle
{
	private Map<Integer,Group> groupMappings = new HashMap<>();
	private NetServer server;
	
	@Override
    public void start() throws Exception {
		Configuration conf = readConfigFile();
		
		fileWatch();
		
        NetServer server = vertx.createNetServer();
        
        Handler<NetSocket> handler = (NetSocket netSocket) -> {
                System.out.println("Incoming connection!");
                netSocket.handler(new Handler<Buffer>() {

                    @Override
                    public void handle(Buffer buffer) {
                        System.out.println("incoming data: "+buffer.length());
                        // parsing message
                        String msg = buffer.getString(0,buffer.length());
                        parseMessage(msg);
                    }

                });
        };
        
        server.connectHandler(handler);
        server.listen(Integer.parseInt(conf.getPort()));
        
        EventBus bus = vertx.eventBus();
        
        bus.consumer("groups", message -> {
      	  System.out.println("I have received a message: " + message.body());
      	  Group group = (Group) message.body();
      	  broadcast(group);
      	});
    }
	
	private void parseMessage(String msg) 
	{
		JSONParser parser = new JSONParser();
		try 
		{
			JSONObject obj = (JSONObject) parser.parse(msg);
		} 
		catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void broadcast(Group group) 
	{
		for (Node n : group.getTreeNodes())
		{
			sendMessage(n, MessageConstructor.groupInfo(group));
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
				//File file = new File()
			}
			eventList.addAll(list);
		});
	}
	
	private void sendMessage(Node n, String msg)
	{
		NetClient client = vertx.createNetClient();
		client.connect(Integer.parseInt(n.getPort()), n.getIP(), msg, res -> {
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
			
			String IP = (String) json.get("ip");
			String port = (String) json.get("port");
			
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
			
			JSONArray groups = (JSONArray) json.get("GROUPS");
			Iterator<?> iter1 = groups.iterator();
			while(iter1.hasNext())
			{
				JSONArray group = (JSONArray) iter1.next();
				Iterator<?> iter2 = group.iterator();
				TreeSet<Node> set = new TreeSet<>();
				while(iter2.hasNext())
				{
					JSONObject node = (JSONObject) iter2.next();
					String IP = (String) node.get("IP");
					String port = (String) node.get("PORT");
					Node n = new Node(IP, port);
					set.add(n);
				}
				if (!set.isEmpty())
				{
					Group g = new Group(set);
					groupMappings.put(g.hashCode(),g);
					vertx.eventBus().publish("groups", g);
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
