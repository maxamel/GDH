package main.java.parser;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import main.java.gdh.Constants;
import main.java.gdh.ExchangeState;
import main.java.gdh.Group;
import main.java.gdh.Node;

public class JsonMessageParser implements MessageParser {

	private final Map<Integer,Group> groupMappings;
	private final Map<Integer,ExchangeState> stateMappings;
	
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
		TreeSet<Node> set = new TreeSet<>();
		Group group = null;
		JsonObject obj = new JsonObject(msg);
		String prime = (String) obj.getString(Constants.prime);
		String generator = (String) obj.getString(Constants.generator);
		JsonArray members = (JsonArray) obj.getJsonArray(Constants.members);
		Iterator<?> iter = members.iterator();
		while (iter.hasNext())
		{
			JsonObject member = (JsonObject) iter.next();
			String ip = (String) member.getString(Constants.ip);
			String port = (String) member.getString(Constants.port);
			Node n = new Node(ip, port);
			set.add(n);
		}
		group = new Group( generator, prime, set);
		group.setGenerator(new BigInteger(generator));
		group.setPrime(new BigInteger(prime));
		groupMappings.put(group.getGroupId(), group);
		stateMappings.put(group.getGroupId(), new ExchangeState(group.getGroupId(), group.getGenerator()));
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
