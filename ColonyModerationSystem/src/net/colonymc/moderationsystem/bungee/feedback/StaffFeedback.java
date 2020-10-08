package net.colonymc.moderationsystem.bungee.feedback;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import net.colonymc.colonyapi.MainDatabase;

public class StaffFeedback extends Feedback {

	String staffUuid;
	
	public StaffFeedback(String staffUuid) {
		super("staff", "    &5&lStaff Survey\n &dFor " + MainDatabase.getName(staffUuid));
		this.staffUuid = staffUuid;
		addQuestion(0, "How active has this staff member been?", getStarResponse());
		addQuestion(1, "How helpful has this staff member been?", getStarResponse());
		addQuestion(2, "How friendly has this staff member been?", getStarResponse());
		addQuestion(3, "How fair has this staff member been?", getStarResponse());
	}
	
	@Override
	public void answer(String playerUuid, String jsonString) {
		Gson g = new Gson();
		JsonArray json = g.fromJson(jsonString, JsonArray.class);
		MainDatabase.sendStatement("INSERT INTO StaffFeedback (uuid, playerUuid, active, helpful, friendly, fair, timestamp) VALUES "
				+ "('" + staffUuid + "', '" + playerUuid + "', " + (Integer.parseInt(json.get(0).getAsJsonObject().get("0").getAsString()) + 1) + ", " + (Integer.parseInt(json.get(1).getAsJsonObject().get("1").getAsString()) + 1) 
				+ ", " + (Integer.parseInt(json.get(2).getAsJsonObject().get("2").getAsString()) + 1) 
				+ ", " + (Integer.parseInt(json.get(3).getAsJsonObject().get("3").getAsString()) + 1) + ", " + System.currentTimeMillis() + ")");
	}
	
	public String getStaffUuid() {
		return staffUuid;
	}

}