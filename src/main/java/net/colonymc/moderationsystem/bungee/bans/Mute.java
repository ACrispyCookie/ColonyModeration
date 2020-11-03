package net.colonymc.moderationsystem.bungee.bans;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.colonyapi.Time;
import net.colonymc.moderationsystem.bungee.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

public class Mute implements Listener {
	
	public static final ArrayList<Mute> mutes = new ArrayList<>();
	
	String name;
	String uuid;
	String staff;
	String staffUuid;
	String reason;
	String id;
	ScheduledTask revoke;
	long mutedUntil;
	long issuedAt;
	
	public Mute(String name, String uuid, String staff, String staffUuid, String reason, String Id, long mutedUntil, long issuedAt) {
		this.name = name;
		this.uuid = uuid;
		this.staff = staff;
		this.staffUuid = staffUuid;
		this.reason = reason;
		this.id = Id;
		this.mutedUntil = mutedUntil;
		this.issuedAt = issuedAt;
		mutes.add(this);
		revoke = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> revoke(false), mutedUntil - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}
	
	public Mute() {
	}
	
	public void revoke(boolean wasFalse) {
		revoke.cancel();
		if(wasFalse) {
			int timesMuted = MainDatabase.getTimesMuted(name);
			MainDatabase.sendStatement("UPDATE PlayerInfo SET timesMuted= " + (timesMuted - 1) + " WHERE uuid= '"+ uuid + "';");
		}
		else {
			MainDatabase.sendStatement("INSERT INTO ArchivedMutes (uuid, staffUuid, reason, mutedUntil, issuedAt, ID) VALUES "
					+ "('" + uuid + "', '" + (staffUuid.equals("CONSOLE") ? null : staffUuid) + "', '" + reason + "', " + mutedUntil + ", " + issuedAt + ", '" + id + "')");
		}
		MainDatabase.sendStatement("DELETE FROM ActiveMutes WHERE uuid='" + uuid + "';");
		if(ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)) != null && ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).isConnected()) {
			ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have been unmuted!")));
		}
		mutes.remove(this);
	}
	
	public ProxiedPlayer getPlayer() {
		if(ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).isConnected()) {
			return ProxyServer.getInstance().getPlayer(UUID.fromString(uuid));
		}
		else {
			return null;
		}
	}
	
	public String getPlayerName() {
		return name;
	}
	
	public String getUuid() {
		return uuid;
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
	
	public long getMutedUntil() {
		return mutedUntil;
	}
	
	public long getIssuedAt() {
		return issuedAt;
	}
	
	public static Mute getByPlayer(ProxiedPlayer p) {
		for(Mute m : mutes) {
			if(m.getUuid().equals(p.getUniqueId().toString())) {
				return m;
			}
		}
		return null;
	}
	
	public static Mute getByName(String name) {
		for(Mute m : mutes) {
			if(m.getPlayerName().equalsIgnoreCase(name)) {
				return m;
			}
		}
		return null;
	}
	
	public static Mute getByUuid(String uuid) {
		for(Mute m : mutes) {
			if(m.getUuid().equals(uuid)) {
				return m;
			}
		}
		return null;
	}
	
	@EventHandler
	public void onChat(ChatEvent e) {
		if(e.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) e.getSender();
			if(Mute.getByPlayer(p) != null) {
				Mute m = Mute.getByPlayer(p);
				if(!e.getMessage().startsWith("/") || e.getMessage().startsWith("/message ") || e.getMessage().startsWith("/msg ") || e.getMessage().startsWith("/t ") ||  e.getMessage().startsWith("/m ")
						|| e.getMessage().startsWith("/pm ") || e.getMessage().startsWith("/r ") || e.getMessage().startsWith("/reply ") || e.getMessage().startsWith("/tell ") || e.getMessage().startsWith("/whisper ") || e.getMessage().startsWith("/w ")) {
					e.setCancelled(true);
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&f&m-------------------------------------\n&r \n&r      &5&lYou" + 
							" are temporarily muted!\n&r \n&r        &fReason &5» &d" + m.getReason() + "\n&r        &fUnmute in &5» &d" + Time.formatted((m.getMutedUntil() - System.currentTimeMillis())/1000) + "\n&r        &fMuted by &5» &d" + m.getStaff() +
							"\n&r \n&f&m-------------------------------------")));
				}
			}
		}
	}

}
