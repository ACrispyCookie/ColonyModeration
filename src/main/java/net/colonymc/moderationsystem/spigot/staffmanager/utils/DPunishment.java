package net.colonymc.moderationsystem.spigot.staffmanager.utils;

public class DPunishment {
	
	final PunishmentType type;
	final long userId;
	final String reason;
	final long timestamp;
	
	public DPunishment(PunishmentType type, long userId, String reason, long timestamp) {
		this.type = type;
		this.userId = userId;
		this.reason = reason;
		this.timestamp = timestamp;
	}
	
	public PunishmentType getType() {
		return type;
	}
	
	public long getUserId() {
		return userId;
	}
	
	public String getReason() {
		return reason;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

}
