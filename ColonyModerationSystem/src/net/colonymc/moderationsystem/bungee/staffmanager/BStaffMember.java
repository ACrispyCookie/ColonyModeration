package net.colonymc.moderationsystem.bungee.staffmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.spigot.staffmanager.BStaffMemberComparator;
import net.colonymc.moderationsystem.spigot.staffmanager.utils.DPunishment;
import net.colonymc.moderationsystem.spigot.staffmanager.utils.Feedback;
import net.colonymc.moderationsystem.spigot.staffmanager.utils.Feedback.FEEDBACK_TYPE;
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
	ArrayList<Feedback> feedback = new ArrayList<Feedback>();
	static BStaffMember smod;
	static BStaffMember smow;
	static BStaffMember smom;
	static BStaffMember wsmod;
	static BStaffMember wsmow;
	static BStaffMember wsmom;
	static ArrayList<BStaffMember> staff = new ArrayList<BStaffMember>();
	static ArrayList<BStaffMember> allStaff = new ArrayList<BStaffMember>();
	
	public BStaffMember(String uuid, long joinTimestamp, long leaveTimestamp) {
		this.uuid = uuid;
		this.rank = decideRank();
		this.joinTimestamp = joinTimestamp;
		this.leaveTimestamp = leaveTimestamp;
		allStaff.add(this);
		loadAll();
	}
	
	public void loadAll() {
		loadProms();
		loadSessions();
		loadBans();
		loadDBans();
		loadReports();
		loadFeedback();
	}
	
	public int calculateBetween(long start, long end) {
		long time = start;
		if(start == -1) {
			Calendar d = Calendar.getInstance();
			d.set(Calendar.HOUR_OF_DAY, 0);
			d.set(Calendar.MINUTE, 0);
			d.set(Calendar.SECOND, 0);
			d.set(Calendar.MILLISECOND, 0);
			time = d.getTimeInMillis();
		}
		else if(start == -2) {
			Calendar w = Calendar.getInstance();
			w.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			w.set(Calendar.HOUR_OF_DAY, 0);
			w.set(Calendar.MINUTE, 0);
			w.set(Calendar.SECOND, 0);
			w.set(Calendar.MILLISECOND, 0);
			time = w.getTimeInMillis();
		}
		else if(start == -3) {
			Calendar m = Calendar.getInstance();
			m.set(Calendar.DAY_OF_MONTH, 0);
			m.set(Calendar.HOUR_OF_DAY, 0);
			m.set(Calendar.MINUTE, 0);
			m.set(Calendar.SECOND, 0);
			m.set(Calendar.MILLISECOND, 0);
			time = m.getTimeInMillis();
		}
		ArrayList<Feedback> feedback = getFeedbackAfter(time);
		if(Feedback.getFromArray(feedback, FEEDBACK_TYPE.TOTAL) < 20) {
			int tBans = countBansAfter(time);
			int tReports = getReportsAfter(time).size();
			int tPlaytime = getPlaytimeBetween(time, end);
			int totalBanPoints = tBans * 20;
			int totalReportPoints = tReports * 20;
			int totalPlaytimePoints = tPlaytime / 60;
			return (totalBanPoints + totalReportPoints + totalPlaytimePoints);
		}
		else {
			int tBans = countBansAfter(time);
			int tReports = getReportsAfter(time).size();
			int tPlaytime = getPlaytimeBetween(time, end);
			double tFairF = 5 - Feedback.getFromArray(feedback, FEEDBACK_TYPE.FAIR);
			double tActiveF = 5 - ((Feedback.getFromArray(feedback, FEEDBACK_TYPE.ACTIVE) + Feedback.getFromArray(feedback, FEEDBACK_TYPE.FRIENDLY) + Feedback.getFromArray(feedback, FEEDBACK_TYPE.HELPFUL))/3);
			int totalBanPoints = (int) (tBans * 20 - tFairF * tBans);
			int totalReportPoints = (int) (tReports * 20 - tFairF * tReports);
			int totalPlaytimePoints = (int) (tPlaytime / 60 - tActiveF * (tPlaytime/60));
			return (totalBanPoints + totalReportPoints + totalPlaytimePoints);
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
	
	public ArrayList<Feedback> getFeedbackAfter(long timestamp) {
		ArrayList<Feedback> r = new ArrayList<Feedback>();
		for(Feedback rr : feedback) {
			if(rr.getAfter() >= timestamp) {
				r.add(rr);
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
			if(smod != null && smod.equals(this)) {
				title = "\n&d&kO&r &d&lSTAFF MEMBER OF THE DAY &kO&r";
			}
		}
		if(nextW - System.currentTimeMillis() > 7200000) {
			if(smow != null && smow.equals(this)) {
				title = title + "\n&d&kO&r &d&lSTAFF MEMBER OF THE WEEK &kO&r";
			}
		}
		if(nextM - System.currentTimeMillis() > 7200000) {
			if(smom != null && smom.equals(this)) {
				title = title + "\n&d&kO&r &d&lSTAFF MEMBER OF THE MONTH &kO&r";
			}
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
			if(smod != null && smod.equals(this)) {
				title = "\n&d&kO&r &d&lSTAFF MEMBER OF THE DAY &kO&r";
			}
			if(wsmod != null && wsmod.equals(this)) {
				title = title + "\n&c&kO&r &c&lWORST STAFF MEMBER OF THE DAY &kO&r";
			}
		}
		if(nextW - System.currentTimeMillis() > 7200000) {
			if(smow != null && smow.equals(this)) {
				title = title + "\n&d&kO&r &d&lSTAFF MEMBER OF THE WEEK &kO&r";
			}
			if(wsmow != null && wsmow.equals(this)) {
				title = title + "\n&c&kO&r &c&lWORST STAFF MEMBER OF THE WEEK &kO&r";
			}
		}
		if(nextM - System.currentTimeMillis() > 7200000) {
			if(smom != null && smom.equals(this)) {
				title = title + "\n&d&kO&r &d&lSTAFF MEMBER OF THE MONTH &kO&r";
			}
			if(wsmom != null && wsmom.equals(this)) {
				title = title + "\n&c&kO&r &c&lWORST STAFF MEMBER OF THE MONTH &kO&r";
			}
		}
		return title;
	}
	
	public boolean hasTitles() {
		if(getFullTitles().equals("")) {
			return false;
		}
		return true;
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
	
	public ArrayList<Feedback> getFeedback() {
		return feedback;
	}
	
	public ArrayList<Feedback> getDailyF() {
		Calendar time = Calendar.getInstance();
		time.set(Calendar.HOUR_OF_DAY, 0);
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);
		time.set(Calendar.MILLISECOND, 0);
		ArrayList<Feedback> feed = new ArrayList<Feedback>();
		for(Feedback f : feedback) {
			if(f.getAfter() >= time.getTimeInMillis()) {
				feed.add(f);
			}
		}
		return feed;
	}
	
	public ArrayList<Feedback> getWeeklyF() {
		Calendar time = Calendar.getInstance();
		time.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		time.set(Calendar.HOUR_OF_DAY, 0);
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);
		time.set(Calendar.MILLISECOND, 0);
		ArrayList<Feedback> feed = new ArrayList<Feedback>();
		for(Feedback f : feedback) {
			if(f.getAfter() >= time.getTimeInMillis()) {
				feed.add(f);
			}
		}
		return feed;
	}
	
	public ArrayList<Feedback> getMonthlyF() {
		Calendar time = Calendar.getInstance();
		time.set(Calendar.DAY_OF_MONTH, 0);
		time.set(Calendar.HOUR_OF_DAY, 0);
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);
		time.set(Calendar.MILLISECOND, 0);
		ArrayList<Feedback> feed = new ArrayList<Feedback>();
		for(Feedback f : feedback) {
			if(f.getAfter() >= time.getTimeInMillis()) {
				feed.add(f);
			}
		}
		return feed;
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
			FEEDBACK_TYPE count = (i == 0 ? FEEDBACK_TYPE.ACTIVE : i == 1 ? FEEDBACK_TYPE.HELPFUL : i == 2 ? FEEDBACK_TYPE.FRIENDLY : i == 3 ? FEEDBACK_TYPE.FAIR : i == 4 ? FEEDBACK_TYPE.TOTAL : null);
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM StaffFeedback WHERE uuid='" + uuid + "'");
			try {
				int totalStars = 0;
				int dStars = 0;
				int wStars = 0;
				int mStars = 0;
				int tC = 0;
				int dC = 0;
				int wC = 0;
				int mC = 0;
				while(rs.next()) {
					totalStars = totalStars + rs.getInt(count.name().toLowerCase());
					tC++;
					if(rs.getLong("timestamp") >= d.getTimeInMillis()) {
						dStars = dStars + rs.getInt(count.name().toLowerCase());
						dC++;
					}
					if(rs.getLong("timestamp") >= w.getTimeInMillis()) {
						wStars = wStars + rs.getInt(count.name().toLowerCase());
						wC++;
					}
					if(rs.getLong("timestamp") >= m.getTimeInMillis()) {
						mStars = mStars + rs.getInt(count.name().toLowerCase());
						mC++;
					}
				}
				if(i < 4) {
					feedback.add(new Feedback(count, 0, (double) totalStars / (tC == 0 ? 1 : tC)));
					feedback.add(new Feedback(count, d.getTimeInMillis(), (double) dStars / (dC == 0 ? 1 : dC)));
					feedback.add(new Feedback(count, w.getTimeInMillis(), (double) wStars / (mC == 0 ? 1 : wC)));
					feedback.add(new Feedback(count, m.getTimeInMillis(), (double) mStars / (mC == 0 ? 1 : mC)));
				}
				else {
					feedback.add(new Feedback(count, 0, (double) tC));
					feedback.add(new Feedback(count, d.getTimeInMillis(), (double) dC));
					feedback.add(new Feedback(count, w.getTimeInMillis(), (double) wC));
					feedback.add(new Feedback(count, m.getTimeInMillis(), (double) mC));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
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
	
	public static BStaffMember getSMOfDay() {
		return smod;
	}
	
	public static BStaffMember getSMOfWeek() {
		return smow;
	}
	
	public static BStaffMember getSMOfMonth() {
		return smom;
	}
	
	public static BStaffMember getWSMOfDay() {
		return wsmod;
	}
	
	public static BStaffMember getWSMOfWeek() {
		return wsmow;
	}
	
	public static BStaffMember getWSMOfMonth() {
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
				smod = rs.getString("daily") == null ? null : BStaffMember.getByUuid(rs.getString("daily"));
				smow = rs.getString("weekly") == null ? null : BStaffMember.getByUuid(rs.getString("weekly"));
				smom = rs.getString("monthly") == null ? null : BStaffMember.getByUuid(rs.getString("monthly"));
				wsmod = rs.getString("wDaily") == null ? null : BStaffMember.getByUuid(rs.getString("wDaily"));
				wsmow = rs.getString("wWeekly") == null ? null : BStaffMember.getByUuid(rs.getString("wWeekly"));
				wsmom = rs.getString("wMonthly") == null ? null : BStaffMember.getByUuid(rs.getString("wMonthly"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
