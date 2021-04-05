package net.colonymc.colonymoderationsystem.spigot;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.colonymc.colonymoderationsystem.bungee.bans.PunishmentType;
import net.colonymc.colonymoderationsystem.bungee.staffmanager.BStaffMember;
import net.colonymc.colonymoderationsystem.bungee.staffmanager.Rank;
import net.colonymc.colonymoderationsystem.bungee.staffmanager.StaffAction;
import net.colonymc.colonymoderationsystem.spigot.bans.ChoosePlayerMenu;
import net.colonymc.colonymoderationsystem.spigot.bans.ChooseReasonMenu;
import net.colonymc.colonymoderationsystem.spigot.bans.ChooseTypeMenu;
import net.colonymc.colonymoderationsystem.spigot.bans.MenuPlayer;
import net.colonymc.colonymoderationsystem.spigot.feedback.BFeedback;
import net.colonymc.colonymoderationsystem.spigot.reports.ArchivedReportsMenu;
import net.colonymc.colonymoderationsystem.spigot.reports.ReportMenu;
import net.colonymc.colonymoderationsystem.spigot.reports.ReportsMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.AddStaffMemberMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.AllStaffManagerMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.SelectRankMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.StaffManagerMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.StaffManagerPlayerMenu;
import net.colonymc.colonymoderationsystem.spigot.twofa.Freeze;

public class BungeecordConnector implements PluginMessageListener {
	
	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] data) {
		if(!channel.equals("BanChannel") && !channel.equals("ReportChannel") && !channel.equals("DiscordChannel") && !channel.equals("ManagerChannel") && !channel.equals("FeedbackChannel")) {
			return;
		}
		ByteArrayDataInput in = ByteStreams.newDataInput(data);
		String subchannel = in.readUTF();
		switch (channel) {
			case "BanChannel":
				if (subchannel.equals("BanMenu")) {
					String playerName = in.readUTF();
					String target = in.readUTF();
					if (target.equals("")) {
						new ChoosePlayerMenu(Bukkit.getPlayerExact(playerName), null, false);
					} else {
						MenuPlayer.load(target);
						new ChooseReasonMenu(Bukkit.getPlayerExact(playerName), MenuPlayer.getByUuid(target), -1);
					}
				}
				if (subchannel.equals("MassBanMenu")) {
					String playerName = in.readUTF();
					new ChooseTypeMenu(Bukkit.getPlayerExact(playerName));
				}
				break;
			case "DiscordChannel":
				if (subchannel.equals("FreezePlayer")) {
					String playerName = in.readUTF();
					new Freeze(playerName, true);
				}
				if (subchannel.equals("FreezeUnlinked")) {
					String playerName = in.readUTF();
					new Freeze(playerName, false);
				} else if (subchannel.equals("UnfreezePlayer")) {
					String playerUuid = in.readUTF();
					if (Freeze.getByUuid(playerUuid) != null) {
						Freeze.getByUuid(playerUuid).stop(true);
					}
				}
				break;
			case "ReportChannel":
				switch (subchannel) {
					case "ReportMenu": {
						String playerName = in.readUTF();
						String reported = in.readUTF();
						new ReportMenu(Bukkit.getPlayerExact(playerName), reported);
						break;
					}
					case "ReportsMenu": {
						String playerName = in.readUTF();
						new ReportsMenu(Bukkit.getPlayerExact(playerName));
						break;
					}
					case "ArchivedReportsMenu": {
						String playerName = in.readUTF();
						new ArchivedReportsMenu(Bukkit.getPlayerExact(playerName));
						break;
					}
				}
				break;
			case "ManagerChannel":
				switch (subchannel) {
					case "ManagerMenu": {
						String playerName = in.readUTF();
						new StaffManagerMenu(Bukkit.getPlayerExact(playerName)).openInventory();
						break;
					}
					case "ManagerMenuPlayer": {
						String playerName = in.readUTF();
						String target = in.readUTF();
						new StaffManagerPlayerMenu(Bukkit.getPlayerExact(playerName), BStaffMember.getByUuid(target), new AllStaffManagerMenu(p)).openInventory();
						break;
					}
					case "ActionMenu": {
						String playerName = in.readUTF();
						String target = in.readUTF();
						String action = in.readUTF();
						if (BStaffMember.getByUuid(target) != null) {
							new SelectRankMenu(Bukkit.getPlayerExact(playerName), BStaffMember.getByUuid(target), StaffAction.valueOf(action),
									new StaffManagerPlayerMenu(Bukkit.getPlayerExact(playerName), BStaffMember.getByUuid(target), new StaffManagerMenu(Bukkit.getPlayerExact(playerName)))).openInventory();
						} else {
							new AddStaffMemberMenu(Bukkit.getPlayerExact(playerName), target).openInventory();
						}
						break;
					}
				}
				break;
			case "FeedbackChannel":
				if (subchannel.equals("AskQuestion")) {
					String playerName = in.readUTF();
					String id = in.readUTF();
					String title = in.readUTF();
					String jsonQuestion = in.readUTF();
					String jsonAnswer = in.readUTF();
					HashMap<Integer, String> questions = new HashMap<>();
					HashMap<Integer, String[]> options = new HashMap<>();
					Gson g = new Gson();
					JsonArray jq = g.fromJson(jsonQuestion, JsonArray.class);
					JsonArray ja = g.fromJson(jsonAnswer, JsonArray.class);
					for (int i = 0; i < jq.size(); i++) {
						JsonArray jr = ja.get(i).getAsJsonObject().get(String.valueOf(i)).getAsJsonArray();
						String[] responses = new String[jr.size()];
						for (int c = 0; c < jr.size(); c++) {
							responses[c] = jr.get(c).getAsString();
						}
						questions.put(i, jq.get(i).getAsJsonObject().get(String.valueOf(i)).getAsString());
						options.put(i, responses);
					}
					BFeedback f = new BFeedback(id, title, questions, options, Bukkit.getPlayerExact(playerName));
					f.ask();
				}
				break;
		}
	}
	
	public static void answerQuestion(Player p, String id, HashMap<Integer, String> answers) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("AnswerQuestion");
			out.writeUTF(p.getUniqueId().toString());
			out.writeUTF(id);
			JsonArray j = new JsonArray();
			for(Integer i : answers.keySet()) {
				JsonObject obj = new JsonObject();
				obj.addProperty(String.valueOf(i), answers.get(i));
				j.add(obj);
			}
			out.writeUTF(j.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		p.sendPluginMessage(Main.getInstance(), "FeedbackChannel", bytes.toByteArray());
	}
	
	public static void voteStaff(Player p, String staffUuid, int stars) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("VoteStaff");
			out.writeUTF(staffUuid);
			out.writeUTF(String.valueOf(stars));
		} catch (IOException e) {
			e.printStackTrace();
		}
		p.sendPluginMessage(Main.getInstance(), "ManagerChannel", bytes.toByteArray());
	}
	
	public static void actionOnStaff(Player p, String actedUuid, StaffAction action, Rank rank) {
		if(action == StaffAction.PROMOTE) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bytes);
			try {
				out.writeUTF("PromotePlayer");
				out.writeUTF(p.getUniqueId().toString());
				out.writeUTF(actedUuid);
				out.writeUTF(rank.name());
			} catch (IOException e) {
				e.printStackTrace();
			}
			p.sendPluginMessage(Main.getInstance(), "ManagerChannel", bytes.toByteArray());
		}
		else {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bytes);
			try {
				out.writeUTF("DemotePlayer");
				out.writeUTF(p.getUniqueId().toString());
				out.writeUTF(actedUuid);
				out.writeUTF(rank.name());
			} catch (IOException e) {
				e.printStackTrace();
			}
			p.sendPluginMessage(Main.getInstance(), "ManagerChannel", bytes.toByteArray());
		}
	}
	
	public static void sendQueue(Player p, String server) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("QueuePlayer");
			out.writeUTF(p.getName());
			out.writeUTF(server);
		} catch (IOException e) {
			e.printStackTrace();
		}
		p.sendPluginMessage(Main.getInstance(), "QueueChannel", bytes.toByteArray());
	}
	
	public static void removeQueue(Player p) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("RemovePlayer");
			out.writeUTF(p.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		p.sendPluginMessage(Main.getInstance(), "QueueChannel", bytes.toByteArray());
	}
	
	public static void requestPlayers(Player staffMember) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("PlayerList");
			out.writeUTF(staffMember.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		staffMember.sendPluginMessage(Main.getInstance(), "BanChannel", bytes.toByteArray());
	}
	
	public static void sendPunishment(String playerUuid, Player staffMember, String reason, int reportId) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("Punish");
			out.writeUTF(playerUuid);
			out.writeUTF(staffMember.getName());
			out.writeUTF(reason);
			out.writeInt(reportId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		staffMember.sendPluginMessage(Main.getInstance(), "BanChannel", bytes.toByteArray());
	}
	
	public static void sendPunishment(String playerUuid, Player staffMember, String reason, long duration, PunishmentType type, int reportId) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("CustomPunish");
			out.writeUTF(playerUuid);
			out.writeUTF(staffMember.getName());
			out.writeUTF(reason);
			out.writeLong(duration);
			out.writeUTF(type.name());
			out.writeInt(reportId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		staffMember.sendPluginMessage(Main.getInstance(), "BanChannel", bytes.toByteArray());
	}
	
	public static void sendPunishment(ArrayList<String> playerUuids, Player staffMember, String reason, long duration, PunishmentType type) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			StringBuilder names = new StringBuilder();
			for(int i= 0; i < playerUuids.size(); i++) {
				if(i + 1 == playerUuids.size()) {
					names.append(playerUuids.get(i));
				}
				else {
					names.append(playerUuids.get(i)).append(",");
				}
			}
			out.writeUTF("MassPunish");
			out.writeUTF(staffMember.getName());
			out.writeUTF(reason);
			out.writeLong(duration);
			if(type == null) {
				out.writeUTF("");
			}
			else {
				out.writeUTF(type.name());
			}
			out.writeUTF(names.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		staffMember.sendPluginMessage(Main.getInstance(), "BanChannel", bytes.toByteArray());
	}
	
	public static void sendReport(String playerName, Player reporterName, String reason) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("NewReport");
			out.writeUTF(playerName);
			out.writeUTF(reporterName.getName());
			out.writeUTF(reason);
		} catch (IOException e) {
			e.printStackTrace();
		}
		reporterName.sendPluginMessage(Main.getInstance(), "ReportChannel", bytes.toByteArray());
	}
	
	public static void markFalse(Player player, int id) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("MarkFalseReport");
			out.writeUTF(player.getName());
			out.writeInt(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
		player.sendPluginMessage(Main.getInstance(), "ReportChannel", bytes.toByteArray());
	}
	
	public static void markAlready(Player player, int reportId, String id) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("MarkAlreadyReport");
			out.writeUTF(player.getName());
			out.writeInt(reportId);
			out.writeUTF(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
		player.sendPluginMessage(Main.getInstance(), "ReportChannel", bytes.toByteArray());
	}

}
