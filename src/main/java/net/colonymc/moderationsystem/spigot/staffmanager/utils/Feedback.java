package net.colonymc.moderationsystem.spigot.staffmanager.utils;

import java.util.ArrayList;

public class Feedback {
	
	public enum FEEDBACK_TYPE{
		ACTIVE,
		FAIR,
		FRIENDLY,
		HELPFUL,
		TOTAL
    }
	
	final FEEDBACK_TYPE type;
	final long after;
	final double stars;
	
	public Feedback(FEEDBACK_TYPE type, long after, double stars) {
		this.type = type;
		this.after = after;
		this.stars = stars;
	}
	
	public FEEDBACK_TYPE getType() {
		return type;
	}
	
	public long getAfter() {
		return after;
	}
	
	public double getStars() {
		return stars;
	}
	
	public static double getFromArray(ArrayList<Feedback> f, FEEDBACK_TYPE type) {
		if(type != FEEDBACK_TYPE.TOTAL) {
			int counter = 0;
			double stars = 0;
			for(Feedback ff : f) {
				if(ff.getType() == type) {
					counter++;
					stars = stars + ff.getStars();
				}
			}
			return stars/counter;
		}
		else {
			double counter = 0;
			for(Feedback ff : f) {
				if(ff.getType() == type) {
					counter = counter + ff.getStars();
				}
			}
			return counter;
		}
	}

}
