package net.colonymc.moderationsystem.bungee.feedback.util;

import java.util.ArrayList;

public class StaffMemberCounter implements Comparable<StaffMemberCounter> {
	
	String staff;
	int value;
	static ArrayList<StaffMemberCounter> smc = new ArrayList<StaffMemberCounter>();
	
	public StaffMemberCounter(String staff, int value) {
		this.staff = staff;
		this.value = value;
	}
	
	public String getUuid() {
		return staff;
	}
	
	public int getValue() {
		return value;
	}
	
	public void addValue(int amount) {
		value = value + amount;
	}

	@Override
	public int compareTo(StaffMemberCounter s) {
		return value - s.getValue();
	}
	
	public static StaffMemberCounter getByUuid(String uuid) {
		for(StaffMemberCounter s : smc) {
			if(s.getUuid().equals(uuid)) {
				return s;
			}
		}
		return null;
	}

}
