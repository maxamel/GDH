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

import main.java.gdh.Constants;
import main.java.gdh.ExchangeState;
import main.java.gdh.Group;
import main.java.gdh.Node;

public class JsonMessageParser implements MessageParser {

	private Map<Integer,Group> groupMappings = new HashMap<>();
	
	public JsonMessageParser(Map<Integer,Group> groupMappings)
	{
		this.groupMappings = groupMappings;
	}
	
	@Override
	public Group parse(String msg) {
		if (msg.contains(Constants.round)) return extractRoundInfo(msg);
		return extractGroupInfo(msg);	
	}

	private Group extractGroupInfo(String msg)
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
			group = new Group(set, generator, prime);
			group.setGenerator(new BigInteger(generator));
			group.setPrime(new BigInteger(prime));
		} 
		catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return group;
	}
	
	private Group extractRoundInfo(String msg)
	{
		JSONParser parser = new JSONParser();
		Group group = null;
		try 
		{
			JSONObject obj = (JSONObject) parser.parse(msg);
			String groupId = (String) obj.get(Constants.groupId);
			String round = (String) obj.get(Constants.round);
			String partial_key = (String) obj.get(Constants.partial_key);
			ExchangeState state = new ExchangeState(Integer.parseInt(groupId),new BigInteger(partial_key), Integer.parseInt(round));
			group = groupMappings.get(groupId);
			group.setState(state);
		} 
		catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return group;
	}
}
