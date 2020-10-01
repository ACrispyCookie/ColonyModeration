package net.colonymc.moderationsystem.bungee.feedback;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
		JSONObject json;
		try {
			json = (JSONObject) new JSONParser().parse(jsonString);
			MainDatabase.sendStatement("INSERT INTO StaffFeedback (uuid, playerUuid, active, helpful, friendly, fair, timestamp) VALUES "
					+ "('" + staffUuid + "', '" + playerUuid + "', " + (Integer.parseInt((String) json.get("0")) + 1) + ", " + (Integer.parseInt((String) json.get("1")) + 1) + ", " + (Integer.parseInt((String) json.get("2")) + 1) 
					+ ", " + (Integer.parseInt((String) json.get("3")) + 1) + ", " + System.currentTimeMillis() + ")");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public String getStaffUuid() {
		return staffUuid;
	}

}