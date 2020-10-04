package net.colonymc.moderationsystem.bungee.staffmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.spigot.staffmanager.BStaffMemberComparator;
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
	int wOvr;
	int mOvr;
	int dOvr;
	ArrayList<Promotion> proms = new ArrayList<Promotion>();
	ArrayList<Promotion> dems = new ArrayList<Promotion>();
	ArrayList<Session> sessions = new ArrayList<Session>();
	ArrayList<Punishment> bans = new ArrayList<Punishment>();
	ArrayList<DPunishment> dbans = new ArrayList<DPunishment>();
	ArrayList<Report> reports = new ArrayList<Report>();
	HashMap<String, Double> dFeedback = new HashMap<String, Double>();
	HashMap<String, Double> wFeedback = new HashMap<String, Double>();
	HashMap<String, Double> mFeedback = new HashMap<String, Double>();
	HashMap<String, Double> feedback = new HashMap<String, Double>();
	static String smod;
	static String smow;
	static String smom;
	static String wsmod;
	static String wsmow;
	static String wsmom;
	static ArrayList<BStaffMember> staff = new ArrayList<BStaffMember>();
	static ArrayList<BStaffMember> allStaff = new ArrayList<BStaffMember>();
	
	public BStaffMember(String uuid, long joinTimestamp, long leaveTimestamp) {
		this.uuid = uuid;
		this.rank = decideRank();
		this.joinTimestamp = joinTimestamp;
		this.leaveTimestamp = leaveTimestamp;
		allStaff.add(this);
		loadAll();
		calculate();
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
	
	public int countBansAfter(long timestamp) {
		int r = 0;
		for(Punishment rr : bans) {
			if(rr.getTimestamp() >= timestamp) {
				r++;
			}
		}
		for(DPunishment rr : dbans) {
			if(rr.getTimestamp() >= timestamp) {
				r++;
			}
		}
		return r;
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
			if(start < se.getJoin() && se.getJoin() < end && end < se.getLeave()) {
				s = s + (int) ((end - se.getJoin())/ 1000);
			}
			else if(se.getJoin() < start && start < se.getLeave() && se.getLeave() < end) {
				s = s + (int) ((se.getLeave() - start)/ 1000);
			}
			else if(start < se.getJoin() && se.getJoin() < se.getLeave() && se.getLeave() < end) {
				s = s + (int) ((se.getLeave() - se.getJoin())/1000);
			}
		}
		return s;
	}
	
	public String getTitles() {
		long nextD = 0;
		long nextW = 0;
		long nextM = 0;
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM NextTopStaff WHERE id=0;");
		try {
			if(rs.next()) {
				nextD = rs.getLong("nextDay");
				nextW = rs.getLong("nextWeek");
				nextM = rs.getLong("nextMonth");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String title = "";
		if(nextD - System.currentTimeMillis() > 7200000) {
			if(smod.equals(uuid)) {
				title = "\n&d&kO&r &d&lSTAFF MEMBER OF THE DAY &kO&r";
			}
		}
		if(nextW - System.currentTimeMillis() > 7200000) {
			if(smow.equals(uuid)) {
				title = title + "\n&d&kO&r &d&lSTAFF MEMBER OF THE WEEK &kO&r";
			}
		}
		if(nextM - System.currentTimeMillis() > 7200000) {
			if(smom.equals(uuid)) {
				title = title + "\n&d&kO&r &d&lSTAFF MEMBER OF THE MONTH &kO&r";
			}
		}
		if(!title.equals("")) {
			title = title + "\n ";
		}
		return title;
	}
	
	public String getFullTitles() {
		long nextD = 0;
		long nextW = 0;
		long nextM = 0;
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM NextTopStaff WHERE id=0;");
		try {
			if(rs.next()) {
				nextD = rs.getLong("nextDay");
				nextW = rs.getLong("nextWeek");
				nextM = rs.getLong("nextMonth");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String title = "";
		if(nextD - System.currentTimeMillis() > 7200000) {
			if(smod.equals(uuid)) {
				title = "\n&d&kO&r &d&lSTAFF MEMBER OF THE DAY &kO&r";
			}
			if(wsmod.equals(uuid)) {
				title = title + "\n&c&kO&r &c&lWORST STAFF MEMBER OF THE DAY &kO&r";
			}
		}
		if(nextW - System.currentTimeMillis() > 7200000) {
			if(smow.equals(uuid)) {
				title = title + "\n&d&kO&r &d&lSTAFF MEMBER OF THE WEEK &kO&r";
			}
			if(wsmow.equals(uuid)) {
				title = title + "\n&c&kO&r &c&lWORST STAFF MEMBER OF THE WEEK &kO&r";
			}
		}
		if(nextM - System.currentTimeMillis() > 7200000) {
			if(smom.equals(uuid)) {
				title = title + "\n&d&kO&r &d&lSTAFF MEMBER OF THE MONTH &kO&r";
			}
			if(wsmom.equals(uuid)) {
				title = title + "\n&c&kO&r &c&lWORST STAFF MEMBER OF THE MONTH &kO&r";
			}
		}
		if(!title.equals("")) {
			title = title + "\n ";
		}
		return title;
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
	
	public HashMap<String, Double> getFeedback() {
		return feedback;
	}
	
	public HashMap<String, Double> getDailyF() {
		return dFeedback;
	}
	
	public HashMap<String, Double> getWeeklyF() {
		return wFeedback;
	}
	
	public HashMap<String, Double> getMonhtlyF() {
		return mFeedback;
	}
	
	public int getDailyOvr() {
		return dOvr;
	}
	
	public int getWeeklyOvr() {
		return wOvr;
	}
	
	public int getMonthlyOvr() {
		return mOvr;
	}
	
	public void loadAll() {
		loadProms();
		loadSessions();
		loadBans();
		loadDBans();
		loadReports();
		loadFeedback();
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
	
	private void loadFeedback() {
		dFeedback.clear();
		wFeedback.clear();
		mFeedback.clear();
		Calendar d = Calendar.getInstance();
		d.set(Calendar.HOUR_OF_DAY, 0);
		d.set(Calendar.MINUTE, 0);
		d.set(Calendar.SECOND, 0);
		d.set(Calendar.MILLISECOND, 0);
		Calendar w = Calendar.getInstance();
		w.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		w.set(Calendar.HOUR_OF_DAY, 0);
		w.set(Calendar.MINUTE, 0);
		w.set(Calendar.SECOND, 0);
		w.set(Calendar.MILLISECOND, 0);
		Calendar m = Calendar.getInstance();
		m.set(Calendar.DAY_OF_MONTH, 0);
		m.set(Calendar.HOUR_OF_DAY, 0);
		m.set(Calendar.MINUTE, 0);
		m.set(Calendar.SECOND, 0);
		m.set(Calendar.MILLISECOND, 0);
		for(int i = 0; i < 5; i++) {
			String count = (i == 0 ? "active" : i == 1 ? "helpful" : i == 2 ? "friendly" : i == 3 ? "fair" : i == 4 ? "total" : "");
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM StaffFeedback WHERE uuid='" + uuid + "'");
			try {
				int tC = 0;
				int totalStars = 0;
				int dC = 0;
				int dStars = 0;
				int wC = 0;
				int wStars = 0;
				int mC = 0;
				int mStars = 0;
				while(rs.next()) {
					totalStars = totalStars + rs.getInt(count);
					tC++;
					if(rs.getLong("timestamp") >= d.getTimeInMillis()) {
						dStars = dStars + rs.getInt(count);
						dC++;
					}
					if(rs.getLong("timestamp") >= w.getTimeInMillis()) {
						wStars = wStars + rs.getInt(count);
						wC++;
					}
					if(rs.getLong("timestamp") >= m.getTimeInMillis()) {
						mStars = mStars + rs.getInt(count);
						mC++;
					}
				}
				if(i < 4) {
					feedback.put(count, (double) totalStars / (tC == 0 ? 1 : tC));
					dFeedback.put(count, (double) dStars / (dC == 0 ? 1 : dC));
					wFeedback.put(count, (double) wStars / (wC == 0 ? 1 : wC));
					mFeedback.put(count, (double) mStars / (mC == 0 ? 1 : mC));
				}
				else {
					feedback.put(count, (double) tC);
					dFeedback.put(count, (double) dC);
					wFeedback.put(count, (double) wC);
					mFeedback.put(count, (double) mC);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void calculate() {
		Calendar d = Calendar.getInstance();
		d.set(Calendar.HOUR_OF_DAY, 0);
		d.set(Calendar.MINUTE, 0);
		d.set(Calendar.SECOND, 0);
		d.set(Calendar.MILLISECOND, 0);
		Calendar w = Calendar.getInstance();
		w.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		w.set(Calendar.HOUR_OF_DAY, 0);
		w.set(Calendar.MINUTE, 0);
		w.set(Calendar.SECOND, 0);
		w.set(Calendar.MILLISECOND, 0);
		Calendar m = Calendar.getInstance();
		m.set(Calendar.DAY_OF_MONTH, 0);
		m.set(Calendar.HOUR_OF_DAY, 0);
		m.set(Calendar.MINUTE, 0);
		m.set(Calendar.SECOND, 0);
		m.set(Calendar.MILLISECOND, 0);
		if(dFeedback.get("total") < 20) {
			int tBans = countBansAfter(d.getTimeInMillis());
			int tReports = getReportsAfter(d.getTimeInMillis()).size();
			int tPlaytime = getPlaytimeBetween(d.getTimeInMillis(), System.currentTimeMillis());
			int totalBanPoints = tBans * 20;
			int totalReportPoints = tReports * 20;
			int totalPlaytimePoints = tPlaytime / 60;
			dOvr = (totalBanPoints + totalReportPoints + totalPlaytimePoints);
		}
		else {
			int tBans = countBansAfter(d.getTimeInMillis());
			int tReports = getReportsAfter(d.getTimeInMillis()).size();
			int tPlaytime = getPlaytimeBetween(d.getTimeInMillis(), System.currentTimeMillis());
			double tFairF = 5 - dFeedback.get("fair");
			double tActiveF = 5 - ((dFeedback.get("active") + dFeedback.get("friendly") + dFeedback.get("helpful"))/3);
			int totalBanPoints = (int) (tBans * 20 - tFairF * tBans);
			int totalReportPoints = (int) (tReports * 20 - tFairF * tReports);
			int totalPlaytimePoints = (int) (tPlaytime / 60 - tActiveF * (tPlaytime/60));
			dOvr = (totalBanPoints + totalReportPoints + totalPlaytimePoints);
		}
		if(wFeedback.get("total") < 20) {
			int tBans = countBansAfter(w.getTimeInMillis());
			int tReports = getReportsAfter(w.getTimeInMillis()).size();
			int tPlaytime = getPlaytimeBetween(w.getTimeInMillis(), System.currentTimeMillis());
			int totalBanPoints = tBans * 20;
			int totalReportPoints = tReports * 20;
			int totalPlaytimePoints = tPlaytime / 60;
			wOvr = (totalBanPoints + totalReportPoints + totalPlaytimePoints);
		}
		else {
			int tBans = countBansAfter(w.getTimeInMillis());
			int tReports = getReportsAfter(w.getTimeInMillis()).size();
			int tPlaytime = getPlaytimeBetween(w.getTimeInMillis(), System.currentTimeMillis());
			double tFairF = 5 - wFeedback.get("fair");
			double tActiveF = 5 - ((wFeedback.get("active") + wFeedback.get("friendly") + wFeedback.get("helpful"))/3);
			int totalBanPoints = (int) (tBans * 20 - tFairF * tBans);
			int totalReportPoints = (int) (tReports * 20 - tFairF * tReports);
			int totalPlaytimePoints = (int) (tPlaytime / 60 - tActiveF * (tPlaytime/60));
			wOvr = (totalBanPoints + totalReportPoints + totalPlaytimePoints);
		}
		if(mFeedback.get("total") < 20) {
			int tBans = countBansAfter(m.getTimeInMillis());
			int tReports = getReportsAfter(m.getTimeInMillis()).size();
			int tPlaytime = getPlaytimeBetween(m.getTimeInMillis(), System.currentTimeMillis());
			int totalBanPoints = tBans * 20;
			int totalReportPoints = tReports * 20;
			int totalPlaytimePoints = tPlaytime / 60;
			mOvr = (totalBanPoints + totalReportPoints + totalPlaytimePoints);
		}
		else {
			int tBans = countBansAfter(m.getTimeInMillis());
			int tReports = getReportsAfter(m.getTimeInMillis()).size();
			int tPlaytime = getPlaytimeBetween(m.getTimeInMillis(), System.currentTimeMillis());
			double tFairF = 5 - mFeedback.get("fair");
			double tActiveF = 5 - ((mFeedback.get("active") + mFeedback.get("friendly") + mFeedback.get("helpful"))/3);
			int totalBanPoints = (int) (tBans * 20 - tFairF * tBans);
			int totalReportPoints = (int) (tReports * 20 - tFairF * tReports);
			int totalPlaytimePoints = (int) (tPlaytime / 60 - tActiveF * (tPlaytime/60));
			mOvr = (totalBanPoints + totalReportPoints + totalPlaytimePoints);
		}
	}
	
	public static BStaffMember getByUuid(String uuid) {
		for(BStaffMember m : staff) {
			if(m.getUuid().equals(uuid)) {
				return m;
			}
		}
		loadStaff();
		for(BStaffMember m : staff) {
			if(m.getUuid().equals(uuid)) {
				return m;
			}
		}
		return null;
	}
	
	public static ArrayList<BStaffMember> getStaff() {
		return staff;
	}
	
	public static ArrayList<BStaffMember> getAllStaff() {
		return allStaff;
	}
	
	public static String getSMOfDay() {
		return smod;
	}
	
	public static String getSMOfWeek() {
		return smow;
	}
	
	public static String getSMOfMonth() {
		return smom;
	}
	
	public static String getWSMOfDay() {
		return wsmod;
	}
	
	public static String getWSMOfWeek() {
		return wsmow;
	}
	
	public static String getWSMOfMonth() {
		return wsmom;
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
			rs = MainDatabase.getResultSet("SELECT * FROM NextTopStaff WHERE id=0;");
			if(rs.next()) {
				smod = rs.getString("daily");
				smow = rs.getString("weekly");
				smom = rs.getString("monthly");
				wsmod = rs.getString("wDaily");
				wsmow = rs.getString("wWeekly");
				wsmom = rs.getString("wMonthly");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
