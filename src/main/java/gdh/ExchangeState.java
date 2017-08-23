package main.java.gdh;

import java.math.BigInteger;

public class ExchangeState 
{
	private int groupId;
	
	private BigInteger partial_key = new BigInteger("0");
	
	private int round = 1;

	public ExchangeState(int groupId) {
		this.groupId = groupId;
	}
	
	public ExchangeState(int groupId, BigInteger partial_key, int round) {
		this.groupId = groupId;
		this.partial_key = partial_key;
		this.round = round;
	}
	
	public int getGroupId() {
		return groupId;
	}

	public BigInteger getPartial_key() {
		return partial_key;
	}

	public int getRound() {
		return round;
	} 
	
	public void incRound() {
		round++;
	}
	
	public void setKey(BigInteger partial_key) {
		this.partial_key = partial_key;
	}
}
