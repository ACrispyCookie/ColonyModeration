package net.colonymc.colonymoderation.bungee.bans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonyapi.Time;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MassPunishment {
	
	final ArrayList<String> targetNames;
	final CommandSender staff;
	final ArrayList<Integer> timesMuted;
	final ArrayList<Integer> timesBanned;
	final ArrayList<String> targetUuids;
	final ArrayList<String> targetIP;
	final ArrayList<Long> duration;
	final ArrayList<String> id;
	final ArrayList<PunishmentType> type;
	final String reason;
	
	public MassPunishment(String[] target, CommandSender staff, String reason, long duration, PunishmentType type) {
		this.targetUuids = (ArrayList<String>) Arrays.asList(target);
		this.targetNames = targetNames();
		this.staff = staff;
		this.reason = reason;
		this.targetIP = targetIP();
		this.timesBanned = timesBanned();
		this.timesMuted = timesMuted();
		if(type == null) {
			this.type = decidePunishment();
		}
		else {
			this.type = getTypes(type);
		}
		if(duration == -1) {
			this.duration = decideDurations();
		}
		else {
			this.duration = getDurations(duration);
		}
		this.id = getBanID();
	}
	
	@SuppressWarnings("deprecation")
	public void execute() {
		int banned = 0;
		int muted = 0;
		for(int i = 0; i < targetNames.size(); i++) {
			if(type.get(i) == PunishmentType.BAN) {
				MainDatabase.sendStatement("INSERT INTO ActiveBans (uuid, bannedIp, staffUuid, reason, bannedUntil, issuedAt, ID) VALUES "
						+ "('" + targetUuids.get(i) + "', '" + targetIP.get(i) + "', '" + 
						((ProxiedPlayer) staff).getUniqueId().toString() + "', '" + reason + "', " + 
						((duration.get(i) == -1) ? duration.get(i) : (System.currentTimeMillis() + duration.get(i))) + ", " + System.currentTimeMillis() + ", '" + id + "');");
				MainDatabase.sendStatement("UPDATE PlayerInfo SET timesBanned=" + (timesBanned.get(i) + 1) + " WHERE uuid='" + targetUuids.get(i) + "';");
				if(ProxyServer.getInstance().getPlayer(targetNames.get(i)) != null) {
					ProxyServer.getInstance().getPlayer(targetNames.get(i)).disconnect(getTextReason(i));
				}
				new Ban(targetNames.get(i), targetUuids.get(i), targetIP.get(i), staff.getName(), ((ProxiedPlayer) staff).getUniqueId().toString(), reason, id.get(i),
						((duration.get(i) == -1) ? duration.get(i) : System.currentTimeMillis() + duration.get(i)),
						System.currentTimeMillis());
				banned++;
			}
			else if(type.get(i) == PunishmentType.MUTE) {
				MainDatabase.sendStatement("INSERT INTO ActiveMutes (uuid, staffUuid, reason, mutedUntil, issuedAt, ID) VALUES "
						+ "('" + targetUuids.get(i) + "', '" + ((ProxiedPlayer) staff).getUniqueId().toString() + "', '" + reason + "', " + 
						((duration.get(i) == -1) ? duration.get(i) : (System.currentTimeMillis() + duration.get(i))) + ", " + System.currentTimeMillis() + ", '" + id.get(i) + "');");
				MainDatabase.sendStatement("UPDATE PlayerInfo SET timesMuted=" + (timesMuted.get(i) + 1) + " WHERE uuid='" + targetUuids.get(i) + "';");
				if(ProxyServer.getInstance().getPlayer(targetNames.get(i)) != null) {
					ProxyServer.getInstance().getPlayer(targetNames.get(i)).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&f&m-------------------------------------\n&r \n&r      &5&lYou" + 
							" are temporarily muted!\n&r \n&r        &fReason &5» &d" + reason + "\n&r        &fUnmute in &5» &d" + Time.formatted(duration.get(i)/1000) + "\n&r        &fMuted by &5» &d" + staff.getName() +
							"\n \n&f&m-------------------------------------")));
				}
				new Mute(targetNames.get(i), targetUuids.get(i), staff.getName(), ((ProxiedPlayer) staff).getUniqueId().toString(), reason, id.get(i),
						System.currentTimeMillis() + duration.get(i), System.currentTimeMillis());
				muted++;
			}
		}
		if(banned > 0) {
			staff.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have banned &d" + banned + " players&f for &d" + reason + "&f!")));
			ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &d" + banned + " players &fhave been removed from the network for &d" + reason +  "&f!\n")));
			
		}
		if(muted > 0) {
			staff.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have muted &d" + muted + " players&f for &d" + reason + "&f!")));
			ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &d" + muted + " players &fhave been muted on the network for &d" + reason +  "&f!\n")));
		}
	}

	private ArrayList<String> targetNames() {
		ArrayList<String> uuids = new ArrayList<>();
		for(String s : targetNames) {
			uuids.add(MainDatabase.getUuid(s));
		}
		return uuids;
	}
	
	private ArrayList<String> targetIP() {
		ArrayList<String> ips = new ArrayList<>();
		for(String s : targetUuids) {
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM PlayerInfo WHERE uuid='" + s + "'");
			try {
				if(rs.next()) {
					ips.add(rs.getString("ip"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ips;
	}
	
	private ArrayList<Integer> timesMuted() {
		ArrayList<Integer> times = new ArrayList<>();
		for(String s : targetUuids) {
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM PlayerInfo WHERE uuid='" + s + "'");
			try {
				if(rs.next()) {
					times.add(rs.getInt("timesMuted"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return times;
	}
	
	private ArrayList<Integer> timesBanned() {
		ArrayList<Integer> times = new ArrayList<>();
		for(String s : targetUuids) {
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM PlayerInfo WHERE uuid='" + s + "'");
			try {
				if(rs.next()) {
					times.add(rs.getInt("timesBanned"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return times;
	}

	private ArrayList<Long> decideDurations() {
		ArrayList<Long> durations = new ArrayList<>();
		for(int i = 0; i < targetNames.size(); i++) {
			durations.add(decideDuration(type.get(i), timesBanned.get(i), timesMuted.get(i)));
		}
		return durations;
	}
	
	private ArrayList<Long> getDurations(long duration) {
		ArrayList<Long> durations = new ArrayList<>();
		for(int i = 0; i < targetNames.size(); i++) {
			durations.add(duration);
		}
		return durations;
	}
	
	private ArrayList<PunishmentType> decidePunishment() {
		ArrayList<PunishmentType> punishments = new ArrayList<>();
		for(int i = 0; i < targetNames.size(); i++) {
			if(timesMuted.get(i) == 6 || reason.equals("Cheating - Hacking") || reason.equals("Bug Abusing") || reason.equals("§lAnticheat - Cheating")|| MainDatabase.isMuted(targetNames.get(i))) {
				punishments.add(PunishmentType.BAN);
			}
			else {
				punishments.add(PunishmentType.MUTE);
			}
		}
		return punishments;
	}
	
	private ArrayList<PunishmentType> getTypes(PunishmentType t) {
		ArrayList<PunishmentType> punishments = new ArrayList<>();
		for(int i = 0; i < targetNames.size(); i++) {
			punishments.add(t);
		}
		return punishments;
	}
	
	private ArrayList<String> getBanID() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVYXZ1234567890";
		ArrayList<String> token = new ArrayList<>();
		for(int i = 0; i < targetNames.size(); i++) {
			for(int ii = 0; ii < 5; ii++) {
				String t = "";
				Random rand = new Random();
				int index = rand.nextInt(35);
				t = t + characters.charAt(index);
				token.add(t);
			}
		}
		try {
			ResultSet rs = MainDatabase.getResultSet("SELECT ID FROM ActiveBans");
			ResultSet rs1 = MainDatabase.getResultSet("SELECT ID FROM ActiveMutes");
			ArrayList<String> ids = new ArrayList<>();
			while(rs.next()) {
				ids.add(rs.getString("ID"));
			}
			while(rs1.next()) {
				ids.add(rs1.getString("ID"));
			}
			for(int i = 0; i < token.size(); i++) {
				while(ids.contains(token.get(i))) {
					String t = "";
					Random rand = new Random();
					int index = rand.nextInt(35);
					t = t + characters.charAt(index);
					token.set(i, t);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return token;
	}
	
	private long decideDuration(PunishmentType type, int timesBanned, int timesMuted) {
		if(type == PunishmentType.BAN) {
			switch(timesBanned) {
			case 0:
				return 1209600000L;
			case 1:
				return 2592000000L;
			case 2:
				return 5184000000L;
			case 3:
				return -1;
			}
		}
		else {
			switch(timesMuted) {
			case 0:
				return 3600000L;
			case 1:
				return 21600000L;
			case 2:
				return 86400000L;
			case 3:
				return 604800000L;
			case 4:
				return 1209600000L;
			case 5:
				return 2592000000L;
			}
		}
		return 0;
	}
	
	private String getTextReason(int i) {
		String finalreason;
		if(duration.get(i) == -1) {
			finalreason = "§5§lYour account has been \n§5§lpermanently suspended from our network!\n§5\n§fReason §5» §d" + reason + "\n§fBanned by §5» §d" + staff + "\n§fUnban in §5»"
					+ " §d" + Time.formatted(duration.get(i)/1000) + "\n§fBan ID §5» §d#" + id.get(i) + "\n\n§fYou can write a ban appeal by opening a ticket here:\n§dhttps://www.colonymc.net/appeal";
		}
		else {
			finalreason = "§5§lYour account has been \n§5§ltemporarily suspended from our network!\n§5\n§fReason §5» §d" + reason + "\n§fBanned by §5» §d" + staff + "\n§fUnban in §5»"
					+ " §d" + Time.formatted(duration.get(i)/1000) + "\n§fBan ID §5» §d#" + id.get(i) + "\n\n§fYou can write a ban appeal by opening a ticket here:\n§dhttps://www.colonymc.net/appeal";
		}
		return finalreason;
	}
}
