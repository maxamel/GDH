package main.java.gdh;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MessageConstructor 
{
	
	public static JsonObject groupInfo(Group g)
	{
		JsonObject msg = new JsonObject();
		msg.put(Constants.prime, g.getPrime().toString());
		msg.put(Constants.generator, g.getGenerator().toString());
		
		JsonArray members = new JsonArray();
		for (Node n : g.getTreeNodes())
		{
			JsonObject obj = new JsonObject();
			obj.put(Constants.ip, n.getIP());
			obj.put(Constants.port, n.getPort());
			members.add(obj);
		}
		msg.put(Constants.members, members);
		return msg;
	}
	
	public static JsonObject roundInfo(ExchangeState state)
	{
		JsonObject msg = new JsonObject();
		msg.put(Constants.groupId, String.valueOf(state.getGroupId()));
		msg.put(Constants.round, String.valueOf(state.getRound()));
		msg.put(Constants.partial_key, state.getPartial_key().toString());
		return msg;
	}
}
