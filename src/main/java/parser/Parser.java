package main.java.parser;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import main.java.gdh.Constants;
import main.java.gdh.Group;
import main.java.gdh.Node;
import main.java.gdh.RoundState;

public class Parser 
{
	public Group extractGroupInfo(String msg)
	{
		JSONParser parser = new JSONParser();
		TreeSet<Node> set = new TreeSet<>();
		Group group = new Group(set);
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
			group.setG(new BigInteger(generator));
			group.setN(new BigInteger(prime));
		} 
		catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return group;
	}
	
	public RoundState extractRoundInfo(String msg)
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
		return null;
	}
}
