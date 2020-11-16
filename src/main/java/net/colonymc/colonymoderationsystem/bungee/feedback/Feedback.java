package net.colonymc.colonymoderationsystem.bungee.feedback;

import java.util.HashMap;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderationsystem.bungee.SpigotConnector;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class Feedback {
	
	final String id;
	final String title;
	final HashMap<Integer,String> questions = new HashMap<>();
	final HashMap<Integer,String[]> options = new HashMap<>();
	
	public Feedback(String id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public void ask(ProxiedPlayer p) {
		SpigotConnector.openSurveyMenu(p.getServer().getInfo(), p.getName(), this);
	}
	
	public void answer(String playerUuid, String jsonString) {
		MainDatabase.sendStatement("INSERT INTO UserFeedback (uuid, surveyId, answer, timestamp) VALUES ('" + playerUuid + "', '" + getId() + "', '" + jsonString + "', " + System.currentTimeMillis() + ")");
	}
	
	public void addQuestion(int i, String s, String[] r) {
		this.questions.put(i, s);
		this.options.put(i, r);
	}
	
	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public HashMap<Integer,String> getQuestions() {
		return questions;
	}
	
	public HashMap<Integer,String[]> getOptions() {
		return options;
	}
	
	protected String[] getStarResponse() {
		return new String[] {"&d⭐&0⭐⭐⭐⭐ &d(1)", "&d⭐⭐&0⭐⭐⭐ &d(2)", "&d⭐⭐⭐&0⭐⭐ &d(3)", "&d⭐⭐⭐⭐&0⭐ &d(4)", "&d⭐⭐⭐⭐⭐ &d(5)"};
	}
	
}
