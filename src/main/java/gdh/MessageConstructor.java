package main.java.gdh;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MessageConstructor 
{
	public static JSONObject groupInfo(Group g)
	{
		JSONObject msg = new JSONObject();
		msg.put(Constants.prime, g.getPrime().toString());
		msg.put(Constants.generator, g.getGenerator().toString());
		
		JSONArray members = new JSONArray();
		for (Node n : g.getTreeNodes())
		{
			JSONObject obj = new JSONObject();
			obj.put(Constants.ip, n.getIP());
			obj.put(Constants.port, n.getPort());
			members.add(obj);
		}
		msg.put(Constants.members, members);
		return msg;
	}
	
	public static JSONObject roundInfo(ExchangeState state)
	{
		JSONObject msg = new JSONObject();
		msg.put(Constants.groupId, String.valueOf(state.getGroupId()));
		msg.put(Constants.round, String.valueOf(state.getRound()));
		msg.put(Constants.partial_key, state.getPartial_key().toString());
		return msg;
	}
}
