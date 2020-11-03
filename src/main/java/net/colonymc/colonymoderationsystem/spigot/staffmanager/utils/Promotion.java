package net.colonymc.colonymoderationsystem.spigot.staffmanager.utils;

import net.colonymc.colonymoderationsystem.bungee.staffmanager.Rank;
import net.colonymc.colonymoderationsystem.bungee.staffmanager.StaffAction;

public class Promotion {
	
	final Rank rankAfter;
	final StaffAction action;
	final String byUuid;
	final long timestamp;
	
	public Promotion(Rank rankAfter, StaffAction action, String byUuid, long timestamp) {
		this.rankAfter = rankAfter;
		this.action = action;
		this.byUuid = byUuid;
		this.timestamp = timestamp;
	}
	
	public String getByUuid() {
		return byUuid;
	}
	
	public Rank getRankAfter() {
		return rankAfter;
	}
	
	public StaffAction getAction() {
		return action;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

}
