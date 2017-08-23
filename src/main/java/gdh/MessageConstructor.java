package main.java.gdh;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MessageConstructor 
{
	public static JSONObject groupInfo(Group g)
	{
		JSONObject msg = new JSONObject();
		msg.put(Constants.prime, g.getPrime());
		msg.put(Constants.generator, g.getGenerator());
		
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
	
	public static JSONObject roundInfo(Group g)
	{
		JSONObject msg = new JSONObject();
		msg.put(Constants.groupId, msg.hashCode());
		msg.put(Constants.round, 1);
		msg.put(Constants.partial_key, 0);
		return msg;
	}
}
