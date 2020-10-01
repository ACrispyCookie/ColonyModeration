package net.colonymc.moderationsystem.spigot.staffmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.bungee.staffmanager.Rank;
import net.colonymc.moderationsystem.bungee.staffmanager.StaffAction;
import net.colonymc.moderationsystem.spigot.staffmanager.utils.DPunishment;
import net.colonymc.moderationsystem.spigot.staffmanager.utils.Promotion;
import net.colonymc.moderationsystem.spigot.staffmanager.utils.Punishment;
import net.colonymc.moderationsystem.spigot.staffmanager.utils.PunishmentType;
import net.colonymc.moderationsystem.spigot.staffmanager.utils.Report;
import net.colonymc.moderationsystem.spigot.staffmanager.utils.Session;

public class BStaffMember {
	
	String uuid;
	Rank rank;
	long joinTimestamp;
	long leaveTimestamp;
	ArrayList<Promotion> proms = new ArrayList<Promotion>();
	ArrayList<Promotion> dems = new ArrayList<Promotion>();
	ArrayList<Session> sessions = new ArrayList<Session>();
	ArrayList<Punishment> bans = new ArrayList<Punishment>();
	ArrayList<DPunishment> dbans = new ArrayList<DPunishment>();
	ArrayList<Report> reports = new ArrayList<Report>();
	static ArrayList<BStaffMember> staff = new ArrayList<BStaffMember>();
	static ArrayList<BStaffMember> allStaff = new ArrayList<BStaffMember>();
	
	public BStaffMember(String uuid, long joinTimestamp, long leaveTimestamp) {
		this.uuid = uuid;
		this.rank = decideRank();
		this.joinTimestamp = joinTimestamp;
		this.leaveTimestamp = leaveTimestamp;
		allStaff.add(this);
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
	
	public ArrayList<Report> getReportsAfter(long timestamp) {
		ArrayList<Report> r = new ArrayList<Report>();
		for(Report rr : reports) {
			if(rr.getTimestamp() >= timestamp) {
				r.add(rr);
			}
		}
		return r;
	}
	
	public ArrayList<Punishment> getPunishmentsAfter(long timestamp) {
		ArrayList<Punishment> r = new ArrayList<Punishment>();
		for(Punishment rr : bans) {
			if(rr.getTimestamp() >= timestamp) {
				r.add(rr);
			}
		}
		return r;
	}
	
	public ArrayList<DPunishment> getDiscordPunishmentsAfter(long timestamp) {
		ArrayList<DPunishment> r = new ArrayList<DPunishment>();
		for(DPunishment rr : dbans) {
			if(rr.getTimestamp() >= timestamp) {
				r.add(rr);
			}
		}
		return r;
	}
	
	public int getPlaytimeBetween(long start, long end) {
		int s = 0;
		for(Session se : sessions) {
			if(se.getLeave() >= start && se.getLeave() <= end) {
				s = s + se.getDuration();
			}
		}
		return s;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public Rank getRank() {
		return rank;
	}
	
	public long getJoin() {
		return joinTimestamp;
	}
	
	public long getLeave() {
		return leaveTimestamp;
	}
	
	public boolean isStaff() {
		return (leaveTimestamp == 0);
	}
	
	public ArrayList<Punishment> getPunishments() {
		return bans;
	}
	
	public ArrayList<DPunishment> getDiscordPunishments() {
		return dbans;
	}
	
	public ArrayList<Report> getReports() {
		return reports;
	}
	
	public ArrayList<Promotion> getPromotions() {
		return proms;
	}
	
	public ArrayList<Promotion> getDemotions() {
		return dems;
	}
	
	public ArrayList<Session> getSessions() {
		return sessions;
	}
	
	public void loadAll() {
		loadProms();
		loadSessions();
		loadBans();
		loadDBans();
		loadReports();
	}
	
	private void loadProms() {
		proms.clear();
		dems.clear();
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM StaffActions WHERE uuid='" + uuid + "'");
		try {
			while(rs.next()) {
				if(rs.getString("action").equals("PROMOTE")) {
					proms.add(new Promotion(Rank.valueOf(rs.getString("rankAfter")), StaffAction.PROMOTE, rs.getString("actionBy"), rs.getLong("timestamp")));
				}
				else {
					dems.add(new Promotion(Rank.valueOf(rs.getString("rankAfter")), StaffAction.DEMOTE, rs.getString("actionBy"), rs.getLong("timestamp")));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadSessions() {
		sessions.clear();
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM StaffSessions WHERE uuid='" + uuid + "'");
		try {
			while(rs.next()) {
				sessions.add(new Session(rs.getLong("loginTimestamp"), rs.getLong("logoutTimestamp")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadBans() {
		bans.clear();
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM ActiveBans WHERE staffUuid='" + uuid + "'");
		try {
			while(rs.next()) {
				bans.add(new Punishment(rs.getString("ID"), PunishmentType.INGAME_BAN, rs.getString("uuid"), rs.getString("reason"), rs.getLong("issuedAt")));
			}
			rs = MainDatabase.getResultSet("SELECT * FROM ActiveMutes WHERE staffUuid='" + uuid + "'");
			while(rs.next()) {
				bans.add(new Punishment(rs.getString("ID"), PunishmentType.INGAME_MUTE, rs.getString("uuid"), rs.getString("reason"), rs.getLong("issuedAt")));
			}
			rs = MainDatabase.getResultSet("SELECT * FROM ArchivedBans WHERE staffUuid='" + uuid + "'");
			while(rs.next()) {
				bans.add(new Punishment(rs.getString("ID"), PunishmentType.INGAME_BAN, rs.getString("uuid"), rs.getString("reason"), rs.getLong("issuedAt")));
			}
			rs = MainDatabase.getResultSet("SELECT * FROM ArchivedMutes WHERE staffUuid='" + uuid + "'");
			while(rs.next()) {
				bans.add(new Punishment(rs.getString("ID"), PunishmentType.INGAME_MUTE, rs.getString("uuid"), rs.getString("reason"), rs.getLong("issuedAt")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadDBans() {
		dbans.clear();
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM DiscordBans WHERE staffId='" + MainDatabase.getDiscordId(uuid) + "'");
		try {
			while(rs.next()) {
				dbans.add(new DPunishment(rs.getString("type").equals("ban") ? PunishmentType.DISCORD_BAN : PunishmentType.DISCORD_MUTE, rs.getLong("discordId"), rs.getString("reason"), rs.getLong("issuedAt")));
			}
			rs = MainDatabase.getResultSet("SELECT * FROM ArchivedDiscordBans WHERE staffId='" + MainDatabase.getDiscordId(uuid) + "'");
			while(rs.next()) {
				dbans.add(new DPunishment(rs.getString("type").equals("ban") ? PunishmentType.DISCORD_BAN : PunishmentType.DISCORD_MUTE, rs.getLong("discordId"), rs.getString("reason"), rs.getLong("issuedAt")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadReports() {
		reports.clear();
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM ArchivedReports WHERE staffUuid='" + uuid + "'");
		try {
			while(rs.next()) {
				reports.add(new Report(rs.getInt("id"), rs.getString("playerUuid"), rs.getString("reporterUuid"), rs.getString("reason"), rs.getLong("timeProcessed"), rs.getBoolean("punished")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static BStaffMember getByUuid(String uuid) {
		for(BStaffMember m : staff) {
			if(m.getUuid().equals(uuid)) {
				return m;
			}
		}
		return null;
	}
	
	public static void loadStaff() {
		try {
			allStaff.clear();
			staff.clear();
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM StaffInfo;");
			while(rs.next()) {
				new BStaffMember(rs.getString("uuid"), rs.getLong("joinTimestamp"), rs.getLong("leaveTimestamp"));
			}
			Collections.sort(allStaff, new BStaffMemberComparator());
			for(BStaffMember m : allStaff) {
				if(m.isStaff()) {
					staff.add(m);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
