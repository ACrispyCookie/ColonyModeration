package net.colonymc.colonymoderation.spigot.staffmanager.utils;

public class Punishment {
	
	final String id;
	final PunishmentType type;
	final String uuid;
	final String reason;
	final long timestamp;
	
	public Punishment(String id, PunishmentType type, String uuid, String reason, long timestamp) {
		this.id = id;
		this.type = type;
		this.uuid = uuid;
		this.reason = reason;
		this.timestamp = timestamp;
	}
	
	public String getId() {
		return id;
	}
	
	public PunishmentType getType() {
		return type;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public String getReason() {
		return reason;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}
