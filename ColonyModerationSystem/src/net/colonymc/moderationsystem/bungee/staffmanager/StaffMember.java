package net.colonymc.moderationsystem.bungee.staffmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import net.colonymc.colonyapi.MainDatabase;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class StaffMember {
	
	String uuid;
	Rank rank;
	long joinTimestamp;
	boolean online;
	static ArrayList<StaffMember> staff = new ArrayList<StaffMember>();
	
	public StaffMember(String uuid, boolean online) {
		this.uuid = uuid;
		this.rank = decideRank();
		this.online = online;
		if(online) {
			joinTimestamp = System.currentTimeMillis();
		}
		staff.add(this);
	}
	
	public void promote(Rank rank, String staff) {
		MainDatabase.sendStatement("INSERT INTO StaffPromotions (uuid, actionBy, action, rankAfter, timestamp) VALUES ('" + uuid + "', '" + staff + "', '" + StaffAction.PROMOTE.name() + "', '" + rank.name() + "', " + System.currentTimeMillis() + ")");
		if(online) {
			ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have been promoted to &d" + rank.name + "&f!")));
		}
	}
	
	public void demote(Rank rank, String staff) {
		if(rank == Rank.KNIGHT) {
			remove();
		}
		else {
			MainDatabase.sendStatement("INSERT INTO StaffPromotions (uuid, actionBy, action, rankAfter, timestamp) VALUES ('" + uuid + "', '" + staff + "', '" + StaffAction.DEMOTE.name() + "', '" + rank.name() + "', " + System.currentTimeMillis() + ")");
			if(online) {
				ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have been demoted to &d" + rank.name + "&f!")));
			}
		}
	}
	
	public void login() {
		joinTimestamp = System.currentTimeMillis();
		this.online = true;
	}
	
	public void logout() {
		MainDatabase.sendStatement("INSERT INTO StaffSession (uuid, loginTimestamp, logoutTimestamp) VALUES ('" + uuid + "', " + joinTimestamp + ", " + System.currentTimeMillis() + ");");
		online = false;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public Rank getRank() {
		return rank;
	}
	
	public long getTimestamp() {
		return joinTimestamp;
	}
	
	public boolean isOnline() {
		return online;
	}
	
	private void remove() {
		MainDatabase.sendStatement("UPDATE StaffInfo SET leaveTimestamp=" + System.currentTimeMillis() + " WHERE uuid='" + uuid + "';");
		ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "knight " + ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).getName());
		if(online) {
			ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have been remove from the staff team!")));
		}
	}
	
	private Rank decideRank() {
		ProxiedPlayer p = ProxyServer.getInstance().getPlayer(UUID.fromString(uuid));
		Rank rank = null;
		if(p.hasPermission("owner.store")) {
			rank = Rank.OWNER;
		}
		else if(p.hasPermission("*")) {
			rank = Rank.ADMIN;
		}
		else if(p.hasPermission("mod.bans")) {
			rank = Rank.MODERATOR;
		}
		else {
			rank = Rank.HELPER;
		}
		return rank;
	}
	
	public static StaffMember getByUuid(String uuid) {
		for(StaffMember m : staff) {
			if(m.uuid.equals(uuid)) {
				return m;
			}
		}
		return load(uuid);
	}
	
	public static StaffMember addPlayer(String uuid, String staff) {
		if(StaffMember.getByUuid(uuid) == null) {
			MainDatabase.sendStatement("INSERT INTO StaffInfo (uuid, joinTimestamp) VALUES ('" + uuid + "', " + System.currentTimeMillis() + ")");
			StaffMember m = new StaffMember(uuid, (ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)) != null));
			m.promote(Rank.HELPER, staff);
			return m;
		}
		return null;
	}
	
	private static StaffMember load(String uuid) {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM StaffInfo WHERE uuid='" + uuid + "';");
		try {
			if(rs.next()) {
				return new StaffMember(uuid, false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
