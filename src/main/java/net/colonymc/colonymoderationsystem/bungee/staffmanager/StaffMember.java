package net.colonymc.colonymoderationsystem.bungee.staffmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderationsystem.bungee.Main;
import net.colonymc.colonymoderationsystem.bungee.bans.JoinListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

public class StaffMember {
	
	final String uuid;
	Rank rank;
	long joinTimestamp;
	boolean online;
	static final ArrayList<StaffMember> staff = new ArrayList<>();
	
	public StaffMember(String uuid, boolean online, boolean firstTime) {
		this.uuid = uuid;
		if(!firstTime) {
			this.rank = decideRank();
		}
		this.online = online;
		if(online) {
			joinTimestamp = System.currentTimeMillis();
		}
		staff.add(this);
	}
	
	public void promote(Rank rank, String staff) {
		this.rank = rank;
		MainDatabase.sendStatement("INSERT INTO StaffActions (uuid, actionBy, action, rankAfter, timestamp) VALUES ('" + uuid + "', '" + staff + "', '" + StaffAction.PROMOTE.name() + "', '" + rank.name() + "', " + System.currentTimeMillis() + ")");
		ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), rank.name().toLowerCase() + " " + MainDatabase.getName(uuid));
		if(online) {
			ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have been promoted to &d" + rank.name + "&f!")));
		}
		if(ProxyServer.getInstance().getPlayer(UUID.fromString(staff)) != null) {
			ProxyServer.getInstance().getPlayer(UUID.fromString(staff)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have promoted &d" + MainDatabase.getName(uuid) + " &fto &d" + rank.name + "&f!")));
		}
		discordRanks();
	}
	
	public void demote(Rank rank, String staff) {
		if(rank.ordinal() < 7) {
			remove(rank, staff);
		}
		else {
			this.rank = rank;
			MainDatabase.sendStatement("INSERT INTO StaffActions (uuid, actionBy, action, rankAfter, timestamp) VALUES ('" + uuid + "', '" + staff + "', '" + StaffAction.DEMOTE.name() + "', '" + rank.name() + "', " + System.currentTimeMillis() + ")");
			ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), rank.name().toLowerCase() + " " + MainDatabase.getName(uuid));
			if(online) {
				ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have been demoted to &d" + rank.name + "&f!")));
			}
			if(ProxyServer.getInstance().getPlayer(UUID.fromString(staff)) != null) {
				ProxyServer.getInstance().getPlayer(UUID.fromString(staff)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have demoted &d" + MainDatabase.getName(uuid) + " &fto &d" + rank.name + "&f!")));
			}
		}
		discordRanks();
	}
	
	public void login() {
		joinTimestamp = System.currentTimeMillis();
		this.online = true;
	}
	
	public void logout() {
		MainDatabase.sendStatement("INSERT INTO StaffSessions (uuid, loginTimestamp, logoutTimestamp) VALUES ('" + uuid + "', " + joinTimestamp + ", " + System.currentTimeMillis() + ");");
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
	
	public void discordRanks() {
		for(Rank rank : Rank.values()) {
			if(rank.ordinal() < 5) {
				continue;
			}
			Main.removeRanksFromPlayer(MainDatabase.getDiscordId(uuid), rank.getDicordRole());
		}
		if(rank.ordinal() >= 10) {
			Main.addRanksToPlayer(MainDatabase.getDiscordId(uuid), 452122638685437953L);
		}
		else {
			Main.removeRanksFromPlayer(MainDatabase.getDiscordId(uuid), 452122638685437953L);
			if(rank.ordinal() >= 7) {
				Main.addRanksToPlayer(MainDatabase.getDiscordId(uuid), 452122434087419906L);
			}
			else {
				Main.removeRanksFromPlayer(MainDatabase.getDiscordId(uuid), 452122434087419906L);
				if(rank.ordinal() > 0) {
					Main.addRanksToPlayer(MainDatabase.getDiscordId(uuid), 349847789506789379L);
				}
				else {
					Main.removeRanksFromPlayer(MainDatabase.getDiscordId(uuid), 349847789506789379L);
				}
			}
		}
		Main.addRanksToPlayer(MainDatabase.getDiscordId(uuid), rank.getDicordRole());
		JoinListener.setRankText(Main.getMember(Main.getUser(MainDatabase.getDiscordId(uuid))), MainDatabase.getName(uuid), UUID.fromString(uuid));
	}
	
	private void remove(Rank rank, String staff) {
		this.rank = rank;
		MainDatabase.sendStatement("UPDATE StaffInfo SET leaveTimestamp=" + System.currentTimeMillis() + " WHERE uuid='" + uuid + "';");
		MainDatabase.sendStatement("INSERT INTO StaffActions (uuid, actionBy, action, rankAfter, timestamp) VALUES ('" + uuid + "', '" + staff + "', '" + StaffAction.DEMOTE.name() + "', '" + rank.name() + "', " + System.currentTimeMillis() + ")");
		ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), rank.name().toLowerCase() + " " + MainDatabase.getName(uuid));
		if(online) {
			ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have been demoted to &d" + rank.name + "&f!")));
			ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have been removed from the staff team!")));
		}
		if(ProxyServer.getInstance().getPlayer(UUID.fromString(staff)) != null) {
			ProxyServer.getInstance().getPlayer(UUID.fromString(staff)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have demoted &d" + MainDatabase.getName(uuid) + " &fto &d" + rank.name + "&f!")));
		}
	}
	
	private Rank decideRank() {
		ResultSet rs = MainDatabase.getResultSet("SELECT rankAfter FROM StaffActions WHERE uuid='" + uuid + "' ORDER BY timestamp DESC LIMIT 1;");
		Rank rank = null;
		try {
			rs.next();
			rank = Rank.valueOf(rs.getString("rankAfter"));
		} catch (SQLException e) {
			e.printStackTrace();
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
	
	public static StaffMember addPlayer(String uuid, String staff, Rank rank) {
		if(StaffMember.getByUuid(uuid) == null) {
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM StaffInfo WHERE uuid='" + uuid + "';");
			try {
				if(rs.next()) {
					MainDatabase.sendStatement("UPDATE StaffInfo SET joinTimestamp=" + System.currentTimeMillis() + ",leaveTimestamp=0 WHERE uuid='" + uuid + "';");
				}
				else {
					MainDatabase.sendStatement("INSERT INTO StaffInfo (uuid, joinTimestamp) VALUES ('" + uuid + "', " + System.currentTimeMillis() + ")");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			StaffMember m = new StaffMember(uuid, (ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)) != null), true);
			m.promote(rank, staff);
			return m;
		}
		return null;
	}
	
	private static StaffMember load(String uuid) {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM StaffInfo WHERE uuid='" + uuid + "' AND leaveTimestamp=0;");
		try {
			if(rs.next()) {
				return new StaffMember(uuid, false, false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
