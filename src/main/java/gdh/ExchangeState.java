package main.java.gdh;

import java.math.BigInteger;

public class ExchangeState 
{
	private int groupId;
	
	private BigInteger partial_key;
	
	private int round = 0;

	public ExchangeState(int groupId, BigInteger gen) {
		this.groupId = groupId;
		this.partial_key = gen;
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
	
	public void setPartial_key(BigInteger partial_key) {
		this.partial_key = partial_key;
	}
	
}
