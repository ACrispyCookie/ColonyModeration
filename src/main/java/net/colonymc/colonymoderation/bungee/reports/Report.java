package net.colonymc.colonymoderation.bungee.reports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderation.bungee.bans.Punishment;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Report {
	
	public static final ArrayList<Report> reports = new ArrayList<>();

	final String reportedName;
	final String reporterName;
	final String reportedUuid;
	final String reporterUuid;
	final String reason;
	int id;
	final long reportedOn;
	
	public Report(String reportedName, String reporterName, String reportedUuid, String reporterUuid, String reason, long reportedOn) {
		this.reportedName = reportedName;
		this.reporterName = reporterName;
		this.reportedUuid = reportedUuid;
		this.reporterUuid = reporterUuid;
		this.reason = reason;
		this.reportedOn = reportedOn;
		reports.add(this);
		insertOnDatabase();
		send();
	}
	
	public Report(String reportedName, String reporterName, String reportedUuid, String reporterUuid, String reason, long reportedOn, int id) {
		this.reportedName = reportedName;
		this.reporterName = reporterName;
		this.reportedUuid = reportedUuid;
		this.reporterUuid = reporterUuid;
		this.reason = reason;
		this.reportedOn = reportedOn;
		this.id = id;
		reports.add(this);
	}
	
	public void insertOnDatabase() {
		ResultSet active = MainDatabase.getResultSet("SELECT * FROM ActiveReports ORDER BY id DESC");
		int activeId = -1;
		int archivedId = -1;
		try {
			if(active.next()) {
				activeId = active.getInt("id");
			}
			ResultSet archived = MainDatabase.getResultSet("SELECT * FROM ArchivedReports ORDER BY id DESC");
			if(archived.next()) {
				archivedId = archived.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(activeId == -1 && archivedId == -1) {
			id = 1;
		}
		else {
			id = Math.max(activeId, archivedId) + 1;
		}
		MainDatabase.sendStatement("INSERT INTO ActiveReports (id, playerUuid, reporterUuid, reason, timeReported) VALUES "
				+ "(" +  id + ", '" + reportedUuid + "', '" + reporterUuid + "', '" + reason + "', " + reportedOn + ");");
	}
	
	public void send() {
		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			if(p.hasPermission("staff.store")) {
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
						"\n &5&lNEW REPORT!\n &5&l» &fPlayer &d" + reporterName + " &fhas reported &d" + reportedName + " &ffor &d" + reason + "&f!\n ")));
			}
		}
	}
	
	public void process(CommandSender pl, Punishment p) {
		MainDatabase.sendStatement("DELETE FROM ActiveReports WHERE id=" + id + ";");
		if(p == null) {
			pl.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou marked the report &d#" + this.getId() + " &fas &cfalse&f!")));
			announceToStaff(" &5&lREPORT CLOSED");
			announceToStaff(" &fThe report &d[#" + id + "] &fagainst &d" + reportedName + " &fwas closed by &d" + pl.getName() + "&f!");
			announceToStaff(" &fResult: &cfalse&f, Punishment reason: &7None");
			if(ProxyServer.getInstance().getPlayer(reporterName) != null) {
				ProxyServer.getInstance().getPlayer(reporterName).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYour report against the player &d" + 
				reportedName + " &fturned out to be &cfalse&f!")));
			}
			MainDatabase.sendStatement("INSERT INTO ArchivedReports (id, playerUuid, reporterUuid, reason, timeReported, staffUuid, timeProcessed, punished, punish) VALUES "
					+ "(" +  id + ", '" + reportedUuid + "', '" + reporterUuid + "', '" + reason + "', " + reportedOn + ", '"
							+ (pl instanceof ProxiedPlayer ? ((ProxiedPlayer) pl).getUniqueId().toString() : null) + "', " + System.currentTimeMillis() + ", 0, 'NONE');");
		}
		else {
			pl.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou marked the report &d#" + this.getId() 
			+ " &fas &atrue &fand punished &d" + this.getReportedName() + " &ffor the reason &d" + p.getReason() + "&f!")));
			announceToStaff(" &5&lREPORT CLOSED");
			announceToStaff(" &fThe report &d[#" + id + "] &fagainst &d" + reportedName + " &fwas closed by &d" + pl.getName() + "&f!");
			announceToStaff(" &fResult: &atrue&f, Punishment reason: &d" + p.getReason());
			if(ProxyServer.getInstance().getPlayer(reporterName) != null) {
				ProxyServer.getInstance().getPlayer(reporterName).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYour report against the player &d" + 
				reportedName + " &fturned out to be &atrue &fand the player has been punished!")));
			}
			MainDatabase.sendStatement("INSERT INTO ArchivedReports (id, playerUuid, reporterUuid, reason, timeReported, staffUuid, timeProcessed, punished, punish) VALUES "
					+ "(" +  id + ", '" + reportedUuid + "', '" + reporterUuid + "', '" + reason + "', " + reportedOn + ", '"
							+ (pl instanceof ProxiedPlayer ? ((ProxiedPlayer) pl).getUniqueId().toString() : null) + "', " + System.currentTimeMillis() + ", 1, '" + p.getId() + "');");
		}
		reports.remove(this);
	}
	
	public void processAlready(ProxiedPlayer pl, String punishId) {
		MainDatabase.sendStatement("DELETE FROM ActiveReports WHERE id=" + id + ";");
		pl.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou marked the report &d#" + this.getId() 
		+ " &fas &6already punished &fwith the punish ID &d#" + punishId + "&f!")));
		announceToStaff(" &5&lREPORT CLOSED");
		announceToStaff(" &fThe report &d[#" + id + "] &fagainst &d" + reportedName + " &fwas closed by &d" + pl.getName() + "&f!");
		announceToStaff(" &fResult: &6already punished&f, Punishment ID: &d" + punishId);
		if(ProxyServer.getInstance().getPlayer(reporterName) != null) {
			ProxyServer.getInstance().getPlayer(reporterName).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYour report against the player &d" + 
			reportedName + " &fturned out to be &atrue &fand the player has been punished!")));
		}
		MainDatabase.sendStatement("INSERT INTO ArchivedReports (id, playerUuid, reporterUuid, reason, timeReported, staffUuid, timeProcessed, punished, punish) VALUES "
				+ "(" +  id + ", '" + reportedUuid + "', '" + reporterUuid + "', '" + reason + "', " + reportedOn + ", '"
						+ pl.getUniqueId().toString() + "', " + System.currentTimeMillis() + ", 1, '" + punishId + "');");
		reports.remove(this);
	}
	
	public String getReporterName() {
		return reporterName;
	}
	
	public String getReportedName() {
		return reportedName;
	}
	
	public String getReporterUuid() {
		return reporterUuid;
	}
	
	public String getReportedUuid() {
		return reportedUuid;
	}
	
	public String getReason() {
		return reason;
	}
	
	public int getId() {
		return id;
	}
	
	public long getTimeReported() {
		return reportedOn;
	}
	
	private void announceToStaff(String msg) {
		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			if(p.hasPermission("staff.store")) {
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', msg)));
			}
		}
	}
	
	public static Report getById(int id) {
		for(Report r : reports) {
			if(r.getId() == id) {
				return r;
			}
		}
		return null;
	}
	
	public static ArrayList<Report> getByPlayer(ProxiedPlayer p) {
		ArrayList<Report> playerReports = new ArrayList<>();
		for(Report r : reports) {
			if(r.getReportedUuid().equals(p.getUniqueId().toString())) {
				playerReports.add(r);
			}
		}
		return playerReports;
	}
	
	public static ArrayList<Report> getByName(String name) {
		ArrayList<Report> playerReports = new ArrayList<>();
		for(Report r : reports) {
			if(r.getReportedName().equals(name)) {
				playerReports.add(r);
			}
		}
		return playerReports;
	}
	
	public static ArrayList<Report> getByUuid(String uuid) {
		ArrayList<Report> playerReports = new ArrayList<>();
		for(Report r : reports) {
			if(r.getReportedUuid().equals(uuid)) {
				playerReports.add(r);
			}
		}
		return playerReports;
	}

}
