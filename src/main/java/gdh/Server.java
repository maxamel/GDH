package main.java.gdh;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import static java.nio.file.StandardWatchEventKinds.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Server extends AbstractVerticle
{
	private Map<String, Group> groups = new HashMap<>();
	
	@Override
    public void start() throws Exception {
		Configuration conf = readConfigFile();
		
		//fileWatch();
		
        NetServer server = vertx.createNetServer();
        
        Handler<NetSocket> handler = (NetSocket netSocket) -> {
                System.out.println("Incoming connection!");
                netSocket.handler(new Handler<Buffer>() {

                    @Override
                    public void handle(Buffer buffer) {
                        System.out.println("incoming data: "+buffer.length());
                        // parsing message
                        buffer.getString(0,buffer.length());
                    }
                });
        };
        
        server.connectHandler(handler);
        server.listen(Integer.parseInt(conf.getPort()));
        
        EventBus bus = vertx.eventBus();
        bus.consumer("news.uk.sport", message -> {
        	  System.out.println("I have received a message: " + message.body());
        	});
    }

	private void fileWatch() throws IOException {
		WatchService watcher = FileSystems.getDefault().newWatchService();
		Path dir = Paths.get("../");
		WatchKey key = dir.register(watcher,
                ENTRY_CREATE,
                ENTRY_MODIFY);
		
		AtomicInteger size = new AtomicInteger(0);
		List<WatchEvent<?>> eventList = new ArrayList<>();
		
		long timerId = vertx.setPeriodic(1000, id -> {
			
			List<WatchEvent<?>> list = key.pollEvents();
			int currSize = list.size();
			if (currSize > 0 && size.get() != currSize)
				size.set(list.size());
			else
			{
				WatchEvent<?> event = list.get(0);
			}
			eventList.addAll(list);
		});
	}
	
	@Override
    public void stop() throws Exception {
		
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
}
