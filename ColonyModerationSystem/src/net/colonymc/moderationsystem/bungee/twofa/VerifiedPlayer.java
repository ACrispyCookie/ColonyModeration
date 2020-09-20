package net.colonymc.moderationsystem.bungee.twofa;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import net.colonymc.moderationsystem.bungee.Main;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class VerifiedPlayer {
	
	String playerUuid;
	long discordID;
	String ip;
	ScheduledTask task;
	public static ArrayList<VerifiedPlayer> verified = new ArrayList<VerifiedPlayer>();
	
	public VerifiedPlayer(LinkedPlayer l, String currentIP) {
		this.playerUuid = l.getPlayerUuid();
		this.discordID = l.getUserId();
		this.ip = currentIP;
	}
	
	public long getDiscordID() {
		return discordID;
	}
	
	public String getPlayerUuid() {
		return playerUuid;
	}
	
	public String getCurrentIP() {
		return ip;
	}
	
	public void remove() {
		verified.remove(verified.indexOf(this));
	}
	
	public void startTask() {
		task = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), getRunnable(), 6, TimeUnit.HOURS);	
	}
	
	public void cancelTask() {
		task.cancel();
	}
	
	public Runnable getRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				verified.remove(VerifiedPlayer.this);
			}
		};
	}
	
	public static VerifiedPlayer getByLinkedPlayer(LinkedPlayer p) {
		for(VerifiedPlayer l : verified) {
			if(l.getPlayerUuid().equals(p.getPlayerUuid()) && l.getDiscordID() == p.getUserId()) {
				return l;
			}
		}
		return null;
	}

}
