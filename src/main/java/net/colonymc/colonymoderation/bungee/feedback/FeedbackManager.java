package net.colonymc.colonymoderation.bungee.feedback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderation.bungee.Main;
import net.colonymc.colonymoderation.bungee.staffmanager.BStaffMember;
import net.colonymc.colonymoderation.bungee.staffmanager.BStaffMemberComparator;
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
	final ArrayList<Feedback> feedbacks = new ArrayList<>();
	
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
		ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
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
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ArrayList<String> topSurveyedStaff = new ArrayList<>();
		ResultSet rs = MainDatabase.getResultSet("SELECT uuid, COUNT(*) FROM StaffFeedback GROUP BY uuid ORDER BY COUNT(*) DESC");
		try {
			while(rs.next()) {
				topSurveyedStaff.add(rs.getString("uuid"));
			}
			ArrayList<BStaffMember> staff = BStaffMember.getStaff();
			staff.sort(new BStaffMemberComparator(cal.getTimeInMillis() - 21600000, cal.getTimeInMillis()));
			for(int i = 0; i < BStaffMember.getStaff().size(); i++) {
				BStaffMember smc = BStaffMember.getStaff().get(i);
				if(smc.calculateBetween(cal.getTimeInMillis() - 21600000, cal.getTimeInMillis()) == 0) {
					break;
				}
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
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
