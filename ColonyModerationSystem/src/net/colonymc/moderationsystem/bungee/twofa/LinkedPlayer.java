package net.colonymc.moderationsystem.bungee.twofa;

import java.util.ArrayList;

import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class LinkedPlayer {
	
	ProxiedPlayer p;
	User u;
	long userID;
	String playerUuid;
	public static ArrayList<LinkedPlayer> linked = new ArrayList<LinkedPlayer>();
	
	public LinkedPlayer(ProxiedPlayer p, User u) {
		this.p = p;
		this.u = u;
		this.playerUuid = p.getUniqueId().toString();
		this.userID = u.getIdLong();
	}
	
	public void remove() {
		linked.remove(this);
	}
	
	public ProxiedPlayer getPlayer() {
		return p;
	}
	
	public User getUser() {
		return u;
	}
	
	public String getPlayerUuid() {
		return playerUuid;
	}
	
	public long getUserId() {
		return userID;
	}
	
	public static LinkedPlayer getByUser(User u) {
		for(LinkedPlayer l : linked) {
			if(l.getUser().equals(u)) {
				return l;
			}
		}
		return null;
	}
	
	public static LinkedPlayer getByPlayer(ProxiedPlayer p) {
		for(LinkedPlayer l : linked) {
			if(l.getPlayer().equals(p)) {
				return l;
			}
		}
		return null;
	}

}
