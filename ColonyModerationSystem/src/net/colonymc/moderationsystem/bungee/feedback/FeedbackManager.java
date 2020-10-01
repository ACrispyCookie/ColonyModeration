package net.colonymc.moderationsystem.bungee.feedback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.bungee.Main;
import net.colonymc.moderationsystem.bungee.feedback.util.StaffMemberCounter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;

public class FeedbackManager {
	
	ScheduledTask task;
	StaffFeedback chosenStaff;
	long lastChange;
	ArrayList<Feedback> feedbacks = new ArrayList<Feedback>();
	
	public FeedbackManager(Configuration conf) {
		for(String key : conf.getSection("surveys").getKeys()) {
			Feedback f = new Feedback(key, conf.getString("surveys." + key + ".title")) {};
			ArrayList<String> questions = new ArrayList<>(conf.getSection("surveys." + key + ".questions").getKeys());
			for(int i = 0; i < questions.size(); i++) {
				String q = questions.get(i);
				String question = conf.getString("surveys." + key + ".questions." + q + ".value");
				String[] answers = new String[conf.getStringList("surveys." + key + ".questions." + q + ".responses").size()];
				int stars = 0;
				for(int u = 0; u < conf.getStringList("surveys." + key + ".questions." + q + ".responses").size(); u++) {
					String answer = conf.getStringList("surveys." + key + ".questions." + q + ".responses").get(u);
					if(answer.equalsIgnoreCase("%star%")) {
						answers[u] = f.getStarResponse()[stars];
						stars++;
					}
					else {
						answers[u] = answer;
					}
				}
				f.addQuestion(i, question, answers);
			}
			feedbacks.add(f);
		}
		start();
	}
	
	private void start() {
		ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				String staff = chooseStaff();
				chosenStaff = staff != null ? new StaffFeedback(chooseStaff()) : null;
				lastChange = System.currentTimeMillis();
				for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
					TextComponent ask = new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fWould you like to answer a random feedback survey?\n &5&l» &f(Reward may be available)"));
					TextComponent proceed = new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n \n &5&l» &d&lClick HERE to start!\n "));
					proceed.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/feedback new"));
					ask.addExtra(proceed);
					p.sendMessage(ask);
				}
			}
		}, 0, 360, TimeUnit.MINUTES);
	}
	
	public void pickQuestion(ProxiedPlayer p) {
		if(chosenStaff != null) {
			Feedback f = chosenStaff;
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM StaffFeedback WHERE playerUuid='" + p.getUniqueId().toString() + "' AND uuid='" + chosenStaff.getStaffUuid() + "' AND timestamp>" + lastChange + ";");
			try {
				if(!rs.next()) {
					f.ask(p);
					return;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		for(Feedback f : feedbacks) {
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM UserFeedback WHERE uuid='" + p.getUniqueId().toString() + "' AND surveyId='" + f.getId() + "';");
			try {
				if(!rs.next()) {
					f.ask(p);
					return;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou answered every available survey!")));
	}
	
	public Feedback getById(String id) {
		for(Feedback f : feedbacks) {
			if(f.getId().equalsIgnoreCase(id)) {
				return f;
			}
		}
		if(id.equals("staff")) {
			return chosenStaff;
		}
		return null;
	}
	
	private String chooseStaff() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ArrayList<String> topSurveyedStaff = new ArrayList<String>();
		ResultSet rs = MainDatabase.getResultSet("SELECT uuid, COUNT(*) FROM StaffFeedback GROUP BY uuid ORDER BY COUNT(*) DESC");
		try {
			while(rs.next()) {
				topSurveyedStaff.add(rs.getString("uuid"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		rs = MainDatabase.getResultSet("SELECT * FROM ActiveBans WHERE issuedAt = " + cal.getTimeInMillis() + " OR issuedAt > " + cal.getTimeInMillis() + ";");
		ArrayList<StaffMemberCounter> topStaff = new ArrayList<StaffMemberCounter>();
		try {
			while(rs.next()) {
				String uuid = rs.getString("staffUuid");
				if(topStaff.contains(StaffMemberCounter.getByUuid(uuid))) {
					StaffMemberCounter.getByUuid(uuid).addValue(1);
				}
				else {
					topStaff.add(new StaffMemberCounter(uuid, 1));
				}
			}
			rs = MainDatabase.getResultSet("SELECT * FROM ActiveMutes WHERE issuedAt = " + cal.getTimeInMillis() + " OR issuedAt > " + cal.getTimeInMillis() + ";");
			while(rs.next()) {
				String uuid = rs.getString("staffUuid");
				if(topStaff.contains(StaffMemberCounter.getByUuid(uuid))) {
					StaffMemberCounter.getByUuid(uuid).addValue(1);
				}
				else {
					topStaff.add(new StaffMemberCounter(uuid, 1));
				}
			}
			rs = MainDatabase.getResultSet("SELECT * FROM ArchivedBans WHERE issuedAt = " + cal.getTimeInMillis() + " OR issuedAt > " + cal.getTimeInMillis() + ";");
			while(rs.next()) {
				String uuid = rs.getString("staffUuid");
				if(topStaff.contains(StaffMemberCounter.getByUuid(uuid))) {
					StaffMemberCounter.getByUuid(uuid).addValue(1);
				}
				else {
					topStaff.add(new StaffMemberCounter(uuid, 1));
				}
			}
			rs = MainDatabase.getResultSet("SELECT * FROM ArchivedMutes WHERE issuedAt = " + cal.getTimeInMillis() + " OR issuedAt > " + cal.getTimeInMillis() + ";");
			while(rs.next()) {
				String uuid = rs.getString("staffUuid");
				if(topStaff.contains(StaffMemberCounter.getByUuid(uuid))) {
					StaffMemberCounter.getByUuid(uuid).addValue(1);
				}
				else {
					topStaff.add(new StaffMemberCounter(uuid, 1));
				}
			}
			rs = MainDatabase.getResultSet("SELECT * FROM ArchivedReports WHERE timeProcessed = " + cal.getTimeInMillis() + " OR timeProcessed > " + cal.getTimeInMillis() + ";");
			while(rs.next()) {
				String uuid = rs.getString("staffUuid");
				if(topStaff.contains(StaffMemberCounter.getByUuid(uuid))) {
					StaffMemberCounter.getByUuid(uuid).addValue(1);
				}
				else {
					topStaff.add(new StaffMemberCounter(uuid, 1));
				}
			}
			rs = MainDatabase.getResultSet("SELECT * FROM DiscordBans WHERE issuedAt = " + cal.getTimeInMillis() + " OR issuedAt > " + cal.getTimeInMillis() + ";");
			while(rs.next()) {
				String uuid = MainDatabase.getUuid(rs.getLong("staffId"));
				if(topStaff.contains(StaffMemberCounter.getByUuid(uuid))) {
					StaffMemberCounter.getByUuid(uuid).addValue(1);
				}
				else {
					topStaff.add(new StaffMemberCounter(uuid, 1));
				}
			}
			rs = MainDatabase.getResultSet("SELECT * FROM ArchivedDiscordBans WHERE issuedAt = " + cal.getTimeInMillis() + " OR issuedAt > " + cal.getTimeInMillis() + ";");
			while(rs.next()) {
				String uuid = MainDatabase.getUuid(rs.getLong("staffId"));
				if(topStaff.contains(StaffMemberCounter.getByUuid(uuid))) {
					StaffMemberCounter.getByUuid(uuid).addValue(1);
				}
				else {
					topStaff.add(new StaffMemberCounter(uuid, 1));
				}
			}
			Collections.sort(topStaff);
			topStaff.remove(StaffMemberCounter.getByUuid("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f"));
			topSurveyedStaff.remove("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f");
			for(int i = 0; i < topStaff.size(); i++) {
				StaffMemberCounter smc = topStaff.get(i);
				if(!topSurveyedStaff.contains(smc.getUuid())) {
					return smc.getUuid();
				}
				else {
					String uuid = topSurveyedStaff.get(i);
					if(!smc.getUuid().equals(uuid)) {
						return smc.getUuid();
					}
				}
			}
			StaffMemberCounter.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
