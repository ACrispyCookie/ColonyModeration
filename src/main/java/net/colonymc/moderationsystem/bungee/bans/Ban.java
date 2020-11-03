package net.colonymc.moderationsystem.bungee.bans;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.bungee.Main;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class Ban {
	
	public static final ArrayList<Ban> bans = new ArrayList<>();
	
	String name;
	final String uuid;
	String bannedIp;
	final String staff;
	final String staffUuid;
	final String reason;
	final String id;
	ScheduledTask revoke;
	final long bannedUntil;
	final long issuedAt;
	
	public Ban(String name, String uuid, String bannedIp, String staff, String staffUuid, String reason, String id, long bannedUntil, long issuedAt) {
		this.name = name;
		this.uuid = uuid;
		this.bannedIp = bannedIp;
		this.staff = staff;
		this.staffUuid = staffUuid;
		this.reason = reason;
		this.id = id;
		this.bannedUntil = bannedUntil;
		this.issuedAt = issuedAt;
		bans.add(this);
		if(bannedUntil != -1) {
			revoke = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> revoke(false), bannedUntil - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}
	}
	
	public void revoke(boolean wasFalse) {
		revoke.cancel();
		if(wasFalse) {
			int timesBanned = MainDatabase.getTimesBanned(name);
			MainDatabase.sendStatement("UPDATE PlayerInfo SET timesBanned= " + (timesBanned - 1) + " WHERE uuid= '"+ uuid + "';");
		}
		else {
			MainDatabase.sendStatement("INSERT INTO ArchivedBans (uuid, bannedIp, staffUuid, reason, bannedUntil, issuedAt, ID) VALUES "
					+ "('" + uuid + "', '" + bannedIp + "', '" + (staffUuid.equals("CONSOLE") ? null : staffUuid) + "', '" + reason + "', " + bannedUntil + ", " + issuedAt + ", '" + id + "')");
		}
		MainDatabase.sendStatement("DELETE FROM ActiveBans WHERE uuid='" + uuid + "';");
		bans.remove(this);
	}
	
	public String getPlayerName() {
		return name;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public String getBannedIp() {
		return bannedIp;
	}
	
	public String getStaff() {
		return staff;
	}
	
	public String getStaffUuid() {
		return staffUuid;
	}
	
	public String getReason() {
		return reason;
	}
	
	public String getId() {
		return id;
	}
	
	public long getBannedUntil() {
		return bannedUntil;
	}
	
	public long getIssuedAt() {
		return issuedAt;
	}
	
	public static Ban getByIp(String ip) {
		for(Ban m : bans) {
			if(m.getBannedIp().equals(ip)) {
				return m;
			}
		}
		return null;
	}
	
	public static Ban getByName(String name) {
		for(Ban m : bans) {
			if(m.getPlayerName().equalsIgnoreCase(name)) {
				return m;
			}
		}
		return null;
	}
	
	public static Ban getByUuid(String uuid) {
		for(Ban m : bans) {
			if(m.getUuid().equals(uuid)) {
				return m;
			}
		}
		return null;
	}

}
