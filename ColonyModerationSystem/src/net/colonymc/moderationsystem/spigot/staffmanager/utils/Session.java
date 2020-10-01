package net.colonymc.moderationsystem.spigot.staffmanager.utils;

public class Session {
	
	long joinTimestamp;
	long leaveTimestamp;
	
	public Session(long joinTimestamp, long leaveTimestamp) {
		this.joinTimestamp = joinTimestamp;
		this.leaveTimestamp = leaveTimestamp;
	}
	
	public long getJoin() {
		return joinTimestamp;
	}
	
	public long getLeave() {
		return joinTimestamp;
	}
	
	public int getDuration() {
		return (int) ((leaveTimestamp - joinTimestamp) / 1000);
	}

}
