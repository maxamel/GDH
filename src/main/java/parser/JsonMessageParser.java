package main.java.parser;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.vertx.core.json.JsonObject;
import main.java.gdh.Constants;
import main.java.gdh.ExchangeState;
import main.java.gdh.Group;
import main.java.gdh.Node;

public class JsonMessageParser implements MessageParser {

	private Map<Integer,Group> groupMappings = new HashMap<>();
	private Map<Integer,ExchangeState> stateMappings = new HashMap<>();
	
	public JsonMessageParser(Map<Integer,Group> groupMappings, Map<Integer,ExchangeState> stateMappings)
	{
		this.groupMappings = groupMappings;
		this.stateMappings = stateMappings;
	}
	
	@Override
	public int parse(String msg) {
		if (msg.contains(Constants.round)) return extractRoundInfo(msg);
		return extractGroupInfo(msg);	
	}

	private int extractGroupInfo(String msg)
	{
		JSONParser parser = new JSONParser();
		TreeSet<Node> set = new TreeSet<>();
		Group group = null;
		try 
		{
			JSONObject obj = (JSONObject) parser.parse(msg);
			String prime = (String) obj.get(Constants.prime);
			String generator = (String) obj.get(Constants.generator);
			JSONArray members = (JSONArray) obj.get(Constants.members);
			Iterator<?> iter = members.iterator();
			while (iter.hasNext())
			{
				JSONObject member = (JSONObject) iter.next();
				String ip = (String) member.get(Constants.ip);
				String port = (String) member.get(Constants.port);
				Node n = new Node(ip, port);
				set.add(n);
			}
			group = new Group( generator, prime, set);
			group.setGenerator(new BigInteger(generator));
			group.setPrime(new BigInteger(prime));
			groupMappings.put(group.getGroupId(), group);
			stateMappings.put(group.getGroupId(), new ExchangeState(group.getGroupId(), group.getGenerator()));
		} 
		catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return group.getGroupId();
	}
	
	private int extractRoundInfo(String msg)
	{
		JsonObject obj = new JsonObject(msg);
		String groupId = (String) obj.getString(Constants.groupId);
		int ret = Integer.parseInt(groupId);
		//String round = (String) obj.get(Constants.round);
		String partial_key = (String) obj.getString(Constants.partial_key);
		ExchangeState state = stateMappings.get(ret);
		if (state==null) return -1;
		state.setPartial_key(new BigInteger(partial_key));
		return ret;
	}
}
