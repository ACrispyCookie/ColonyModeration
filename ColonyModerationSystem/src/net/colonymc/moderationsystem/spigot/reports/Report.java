package net.colonymc.moderationsystem.spigot.reports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.entity.Player;

import net.colonymc.colonyapi.MainDatabase;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Report{

	public static ArrayList<Report> reports = new ArrayList<Report>();
	public static ArrayList<Report> archived = new ArrayList<Report>();
	String reportedName;
	String reporterName;
	String reportedUuid;
	String reporterUuid;
	String reason;
	int id;
	long reportedOn;
	long processedOn;
	boolean processed = false;
	String processorName;
	String processorUuid;
	boolean punished;
	String punishmentId;
	
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
	
	public Report(String reportedName, String reporterName, String reportedUuid, String reporterUuid, String reason, long reportedOn, int id, long timeProcessed, String processorName, String processorUuid, boolean punished, String punishId) {
		this.reportedName = reportedName;
		this.reporterName = reporterName;
		this.reportedUuid = reportedUuid;
		this.reporterUuid = reporterUuid;
		this.reason = reason;
		this.reportedOn = reportedOn;
		this.processedOn = timeProcessed;
		this.id = id;
		this.processed = true;
		this.processorName = processorName;
		this.processorUuid = processorUuid;
		this.punished = punished;
		this.punishmentId = punishId;
		archived.add(this);
	}
	
	public Report() {
	}

	public void process(Player p) {
		if(!processed) {
			p.closeInventory();
			new ProcessReport(this, p);
		}
	}
	
	public String getReportedName() {
		return reportedName;
	}
	
	public String getReportedUuid() {
		return reportedUuid;
	}
	
	public String getReporterName() {
		return reporterName;
	}
	
	public String getReporterUuid() {
		return reporterUuid;
	}
	
	public String getReason() {
		return reason;
	}
	
	public int getId() {
		return id;
	}
	
	public long getTimeReport() {
		return reportedOn;
	}
	
	public boolean isProcessed() {
		return processed;
	}
	
	public String getProcessorName() {
		return processorName;
	}
	
	public String getProcessorUuid() {
		return processorUuid;
	}
	
	public long getTimeProcessed() {
		return processedOn;
	}
	
	public boolean wasPunished() {
		return punished;
	}
	
	public String getPunishId() {
		return punishmentId;
	}
	
	public static Report getById(int id) {
		for(Report r : reports) {
			if(r.getId() == id) {
				return r;
			}
		}
		return null;
	}
	
	public static Report getArchivedById(int id) {
		for(Report r : archived) {
			if(r.getId() == id) {
				return r;
			}
		}
		return null;
	}
	
	public static ArrayList<Report> getByPlayer(ProxiedPlayer p) {
		ArrayList<Report> playerReports = new ArrayList<Report>();
		for(Report r : reports) {
			if(r.getReportedUuid().equals(p.getUniqueId().toString())) {
				playerReports.add(r);
			}
		}
		return playerReports;
	}
	
	public static ArrayList<Report> getByName(String name) {
		ArrayList<Report> playerReports = new ArrayList<Report>();
		for(Report r : reports) {
			if(r.getReportedName().equals(name)) {
				playerReports.add(r);
			}
		}
		return playerReports;
	}
	
	public static ArrayList<Report> getByUuid(String uuid) {
		ArrayList<Report> playerReports = new ArrayList<Report>();
		for(Report r : reports) {
			if(r.getReportedUuid().equals(uuid)) {
				playerReports.add(r);
			}
		}
		return playerReports;
	}
	
	public static void updateReports() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM ActiveReports");
		try {
			while(rs.next()) {
				if(Report.getById(rs.getInt("id")) == null) {
					new Report(MainDatabase.getName(rs.getString("playerUuid")), MainDatabase.getName(rs.getString("reporterUuid")), rs.getString("playerUuid"), rs.getString("reporterUuid"), rs.getString("reason"), rs.getLong("timeReported"), rs.getInt("id"));
				}
			}
			for(int i = 0; i < reports.size(); i++) {
				Report r = reports.get(0);
				ResultSet rr = MainDatabase.getResultSet("SELECT * FROM ActiveReports WHERE id=" + r.getId());
				if(!rr.next()) {
					reports.remove(r);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateArchivedReports() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM ArchivedReports");
		try {
			while(rs.next()) {
				if(Report.getArchivedById(rs.getInt("id")) == null) {
					new Report(MainDatabase.getName(rs.getString("playerUuid")), MainDatabase.getName(rs.getString("reporterUuid")), rs.getString("playerUuid"), rs.getString("reporterUuid"), rs.getString("reason"), rs.getLong("timeReported"), rs.getInt("id"),
							rs.getLong("timeProcessed"), MainDatabase.getName(rs.getString("staffUuid")), rs.getString("staffUuid"), rs.getBoolean("punished"), rs.getString("punish"));
				}
			}
			for(int i = 0; i < archived.size(); i++) {
				Report r = archived.get(0);
				ResultSet rr = MainDatabase.getResultSet("SELECT * FROM ArchivedReports WHERE id=" + r.getId());
				if(!rr.next()) {
					archived.remove(r);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
