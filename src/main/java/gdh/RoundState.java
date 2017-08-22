package main.java.gdh;

public class RoundState 
{
	private String groupId;
	
	private String partial_key = "";
	
	private int round = 0;

	public RoundState(String groupId) {
		this.groupId = groupId;
	}
	
	public String getGroupId() {
		return groupId;
	}

	public String getPartial_key() {
		return partial_key;
	}

	public int getRound() {
		return round;
	} 
}
