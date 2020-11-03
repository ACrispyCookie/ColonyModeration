package net.colonymc.colonymoderationsystem.spigot.staffmanager.utils;

public class Session {
	
	final long joinTimestamp;
	final long leaveTimestamp;
	
	public Session(long joinTimestamp, long leaveTimestamp) {
		this.joinTimestamp = joinTimestamp;
		this.leaveTimestamp = leaveTimestamp;
	}
	
	public long getJoin() {
		return joinTimestamp;
	}
	
	public long getLeave() {
		return leaveTimestamp;
	}
	
	public int getDuration() {
		return (int) ((leaveTimestamp - joinTimestamp) / 1000);
	}

}
