package net.colonymc.moderationsystem.bungee.bans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.colonyapi.Time;
import net.colonymc.moderationsystem.bungee.reports.Report;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Punishment {
	
	final String target;
	final CommandSender staff;
	final int timesMuted;
	final int timesBanned;
	final String targetUuid;
	final String targetIP;
	final PunishmentType type;
	final long duration;
	final String reason;
	final String id;
	final int reportId;
	
	public Punishment(String target, CommandSender staff, String reason, int reportId) {
		this.targetUuid = target;
		this.target = MainDatabase.getName(target);
		this.staff = staff;
		this.reason = reason;
		this.targetIP = targetIP();
		this.timesBanned = timesBanned();
		this.timesMuted = timesMuted();
		this.type = decidePunishment();
		this.duration = decideDuration();
		this.id = getBanID();
		this.reportId = reportId;
	}
	
	public Punishment(String target, CommandSender staff, String reason, long duration, PunishmentType type, int reportId) {
		this.targetUuid = target;
		this.target = MainDatabase.getName(target);
		this.staff = staff;
		this.reason = reason;
		this.targetIP = targetIP();
		this.timesBanned = timesBanned();
		this.timesMuted = timesMuted();
		if(type == null) {
			this.type = decidePunishment();
		}
		else if(type == PunishmentType.MUTE && MainDatabase.isMuted(target)) {
			this.type = PunishmentType.BAN;
		}
		else {
			this.type = type;
		}
		this.duration = duration;
		this.id = getBanID();
		this.reportId = reportId;
	}
	
	@SuppressWarnings("deprecation")
	public void execute() {
		if(type == PunishmentType.BAN) {
			MainDatabase.sendStatement("INSERT INTO ActiveBans (uuid, bannedIp, staffUuid, reason, bannedUntil, issuedAt, ID) VALUES "
					+ "('" + targetUuid + "', '" + targetIP + "', '" + (staff instanceof ProxiedPlayer ? ((ProxiedPlayer) staff).getUniqueId().toString() : (null)) + "', '" + reason + "', " + 
					((duration == -1) ? duration : (System.currentTimeMillis() + duration)) + ", " + System.currentTimeMillis() + ", '" + id + "');");
			MainDatabase.sendStatement("UPDATE PlayerInfo SET timesBanned=" + (timesBanned + 1) + " WHERE uuid='" + targetUuid + "';");
			staff.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlayer &d" + target + " &fhas been banned for &d" + reason + "&f!")));
			ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fThe player &d" + target + " &fhas been removed from the network for &d" + reason +  "&f!\n")));
			if(ProxyServer.getInstance().getPlayer(target) != null) {
				ProxyServer.getInstance().getPlayer(target).disconnect(getTextReason());
			}
			for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
				if(p.getAddress().getHostString().equals(targetIP)) {
					p.disconnect(getEvasionReason());
				}
			}
			new Ban(target, targetUuid, targetIP, staff.getName(), (staff instanceof ProxiedPlayer ? ((ProxiedPlayer) staff).getUniqueId().toString() : staff.getName()), reason, id, 
					((duration == -1) ? duration : System.currentTimeMillis() + duration), System.currentTimeMillis());
			if(Report.getById(reportId) != null) {
				Report.getById(reportId).process(staff, this);
			}
		}
		else if(type == PunishmentType.MUTE) {
			MainDatabase.sendStatement("INSERT INTO ActiveMutes (uuid, staffUuid, reason, mutedUntil, issuedAt, ID) VALUES "
					+ "('" + targetUuid + "', '" + (staff instanceof ProxiedPlayer ? ((ProxiedPlayer) staff).getUniqueId().toString() : (null)) + "', '" + reason + "', " + 
					((duration == -1) ? duration : (System.currentTimeMillis() + duration)) + ", " + System.currentTimeMillis() + ", '" + id + "');");
			MainDatabase.sendStatement("UPDATE PlayerInfo SET timesMuted=" + (timesMuted + 1) + " WHERE uuid='" + targetUuid + "';");
			staff.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlayer &d" + target + " &fhas been muted for &d" + reason + "&f!")));
			ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fThe player &d" + target + " &fhas been muted for &d" + reason +  "&f!\n")));
			if(ProxyServer.getInstance().getPlayer(target) != null) {
				ProxyServer.getInstance().getPlayer(target).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&f&m-------------------------------------\n&r \n&r      &5&lYou" + 
						" are temporarily muted!\n&r \n&r        &fReason &5» &d" + reason + "\n&r        &fUnmute in &5» &d" + Time.formatted(duration/1000) + "\n&r        &fMuted by &5» &d" + staff.getName() +
						"\n \n&f&m-------------------------------------")));
			}
			new Mute(target, targetUuid, staff.getName(), (staff instanceof ProxiedPlayer ? ((ProxiedPlayer) staff).getUniqueId().toString() : staff.getName()), reason, id, 
					System.currentTimeMillis() + duration, System.currentTimeMillis());
			if(Report.getById(reportId) != null) {
				Report.getById(reportId).process(staff, this);
			}
		}
		if(!Report.getByUuid(targetUuid).isEmpty()) {
			for(Report r : Report.getByUuid(targetUuid)) {
				if(r.getReason().equals(reason)) {
					r.process(staff, this);
				}
			}
		}
	}

	private String targetIP() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM PlayerInfo WHERE uuid='" + targetUuid + "'");
		try {
			if(rs.next()) {
				return rs.getString("ip");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private int timesMuted() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM PlayerInfo WHERE uuid='" + targetUuid + "'");
		try {
			if(rs.next()) {
				return rs.getInt("timesMuted");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int timesBanned() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM PlayerInfo WHERE uuid='" + targetUuid + "'");
		try {
			if(rs.next()) {
				return rs.getInt("timesBanned");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private long decideDuration() {
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

	private PunishmentType decidePunishment() {
		if(timesMuted == 6 || reason.equals("Cheating - Hacking") || reason.equals("Bug Abusing") || reason.equals("§lAnticheat - Cheating")) {
			return PunishmentType.BAN;
		}
		else {
			return PunishmentType.MUTE;
		}
	}
	
	private String getBanID() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVYXZ1234567890";
		StringBuilder token = new StringBuilder();
		for(int i = 0; i < 5; i++) {
			Random rand = new Random();
			int index = rand.nextInt(35);
			token.append(characters.substring(index, index + 1));
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
			while(ids.contains(token.toString())) {
				Random rand = new Random();
				int index = rand.nextInt(35);
				token.append(characters.substring(index, index + 1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return token.toString();
	}
	
	private String getTextReason() {
		String finalreason;
		if(duration == -1) {
			finalreason = "§5§lYour account has been \n§5§lpermanently suspended from our network!\n§5\n§fReason §5» §d" + reason + "\n§fBanned by §5» §d" + staff.getName() + "\n§fUnban in §5»"
					+ " §d" + Time.formatted(duration/1000) + "\n§fBan ID §5» §d#" + id + "\n\n§fYou can write a ban appeal by opening a ticket here:\n§dhttps://www.colonymc.net/appeal";
		}
		else {
			finalreason = "§5§lYour account has been \n§5§ltemporarily suspended from our network!\n§5\n§fReason §5» §d" + reason + "\n§fBanned by §5» §d" + staff.getName() + "\n§fUnban in §5»"
					+ " §d" + Time.formatted(duration/1000) + "\n§fBan ID §5» §d#" + id + "\n\n§fYou can write a ban appeal by opening a ticket here:\n§dhttps://www.colonymc.net/appeal";
		}
		return finalreason;
	}
	
	private String getEvasionReason() {
		return "§5§lAnother account with the same IP\n§5§lhas been temporarily suspended from our network!\n§5\n§fAccount's name §5» §d" +
	target + "\n§fReason §5» §d" + reason + "\n§fBanned by §5» §d" + staff.getName() + "\n§fUnban in §5»"
					+ " §d" + Time.formatted(duration/1000)
					+ "\n§fLogin from your other\n§faccount in order to get your §dBan ID\n\n§fYou can write a ban appeal by opening a ticket here:\n§dhttps://www.colonymc.net/appeal";
	}
	
	public PunishmentType getType() {
		return type;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public String getReason() {
		return reason;
	}
	
	public String getId() {
		return id;
	}

}
