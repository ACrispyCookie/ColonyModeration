package net.colonymc.moderationsystem.spigot.staffmanager.utils;

public class Report {
	
	final int id;
	final String uuid;
	final String reporter;
	final String reason;
	final long processedAt;
	final boolean punished;
	
	public Report(int id, String uuid, String reporter, String reason, long timestamp, boolean punished) {
		this.id = id;
		this.uuid = uuid;
		this.reporter = reporter;
		this.reason = reason;
		this.processedAt = timestamp;
		this.punished = punished;
	}

	public int getId() {
		return id;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public String getReporter() {
		return reporter;
	}
	
	public String getReason() {
		return reason;
	}
	
	public long getTimestamp() {
		return processedAt;
	}
	
	public boolean wasPunished() {
		return punished;
	}

}
