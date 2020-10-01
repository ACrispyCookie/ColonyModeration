package net.colonymc.moderationsystem.spigot.feedback;

import java.util.HashMap;

import org.bukkit.entity.Player;

import net.colonymc.api.book.survey.Survey;
import net.colonymc.api.book.survey.SurveyBookBuilder;
import net.colonymc.api.book.survey.SurveyLineBuilder;
import net.colonymc.moderationsystem.spigot.BungeecordConnector;

public class BFeedback {
	
	String id;
	Player p;
	String title;
	HashMap<Integer, String> questions = new HashMap<Integer, String>();
	HashMap<Integer, String[]> options = new HashMap<Integer, String[]>();
	HashMap<Integer, String> answers = new HashMap<Integer, String>();
	
	public BFeedback(String id, String title, HashMap<Integer, String> questions, HashMap<Integer, String[]> answers, Player p) {
		this.id = id;
		this.title = title;
		this.questions = questions;
		this.options = answers;
		this.p = p;
	}
	
	public void ask() {
		Survey survey = new Survey(questions.size()) {
			@Override
			public void onComplete(HashMap<String, String> answers, Player p) {
				for(String s : answers.keySet()) {
					BFeedback.this.answers.put(Integer.parseInt(s), answers.get(s));
				}
				complete();
			}
		};
		for(Integer q : questions.keySet()) {
			SurveyBookBuilder builder = new SurveyBookBuilder();
			builder.addLine(new SurveyLineBuilder(title + "\n \n").build());
			builder.addLine(new SurveyLineBuilder(questions.get(q) + "\n \n").build());
			for(int i = 0; i < options.get(q).length; i++) {
				builder.addLine(new SurveyLineBuilder(" &5&l» &0" + options.get(q)[i] + "\n").button(true).key(String.valueOf(q)).value(String.valueOf(i)).build());
			}
			survey.addBook(builder.build());
		}
		survey.addBook(new SurveyBookBuilder()
				.addLine(new SurveyLineBuilder("Thank you for your feedback! :)").build())
				.build());
		survey.open(p);
	}
	
	public void complete() {
		BungeecordConnector.answerQuestion(p, id, answers);
	}

}
