package net.colonymc.colonymoderation.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonPrimitive;
import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderation.bungee.bans.MassPunishment;
import net.colonymc.colonymoderation.bungee.bans.Punishment;
import net.colonymc.colonymoderation.bungee.bans.PunishmentType;
import net.colonymc.colonymoderation.bungee.feedback.Feedback;
import net.colonymc.colonymoderation.bungee.queue.Queue;
import net.colonymc.colonymoderation.bungee.reports.Report;
import net.colonymc.colonymoderation.bungee.staffmanager.Rank;
import net.colonymc.colonymoderation.bungee.staffmanager.StaffMember;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class SpigotConnector implements Listener {
	
	@EventHandler
	public void onMessage(PluginMessageEvent e) {
		if(!e.getTag().equals("BanChannel") && !e.getTag().equals("ReportChannel") && !e.getTag().equals("DiscordChannel") && !e.getTag().equals("QueueChannel") && !e.getTag().equals("ManagerChannel") && !e.getTag().equals("FeedbackChannel")) {
			return;
		}
		ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
		String subChannel = in.readUTF();
		if(e.getTag().equalsIgnoreCase("BanChannel")) {
			if(subChannel.equalsIgnoreCase("Punish")) {
				String playerUuid = in.readUTF();
				String staffMember = in.readUTF();
				String reason = in.readUTF();
				int reportId = in.readInt();
				if(reportId == -1 || Report.getById(reportId) != null) {
					new Punishment(playerUuid, ProxyServer.getInstance().getPlayer(staffMember), reason, reportId).execute();
				}
				else {
					ProxyServer.getInstance().getPlayer(staffMember).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error occured while trying to process the "
							+ "report #" + reportId + "! The report has been probably processed by another staff member!")));
				}
			}
			else if(subChannel.equalsIgnoreCase("CustomPunish")) {
				String playerUuid = in.readUTF();
				String staffMember = in.readUTF();
				String reason = in.readUTF();
				long duration = in.readLong();
				String typeName = in.readUTF();
				PunishmentType type = PunishmentType.valueOf(typeName);
				int reportId = in.readInt();
				if(reportId == -1 || Report.getById(reportId) != null) {
					new Punishment(playerUuid, ProxyServer.getInstance().getPlayer(staffMember), reason, duration, type, reportId).execute();
				}
				else {
					ProxyServer.getInstance().getPlayer(staffMember).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error occured while trying to process the "
							+ "report #" + reportId + "! The report has been probably processed by another staff member!")));
				}
			}
			else if(subChannel.equalsIgnoreCase("MassPunish")) {
				String staffMember = in.readUTF();
				String reason = in.readUTF();
				long duration = in.readLong();
				String typeName = in.readUTF();
				PunishmentType type;
				if(typeName.equals("")) {
					type = null;
				}
				else {
					type = PunishmentType.valueOf(typeName);
				}
				String[] players = in.readUTF().split(",");
				new MassPunishment(players, ProxyServer.getInstance().getPlayer(staffMember), reason, duration, type).execute();
			}
			else if(subChannel.equalsIgnoreCase("PlayerList")) {
				String player = in.readUTF();
				sendPlayers(ProxyServer.getInstance().getPlayer(player).getServer().getInfo());
			}
		}
		else if(e.getTag().equalsIgnoreCase("ReportChannel")) {
            switch (subChannel) {
                case "NewReport":
                    String reportedName = in.readUTF();
                    String reporterName = in.readUTF();
                    String reportedUuid = ProxyServer.getInstance().getPlayer(reportedName).getUniqueId().toString();
                    String reporterUuid = ProxyServer.getInstance().getPlayer(reporterName).getUniqueId().toString();
                    String reason = in.readUTF();
                    new Report(reportedName, reporterName, reportedUuid, reporterUuid, reason, System.currentTimeMillis());
                    break;
                case "MarkFalseReport": {
                    String staff = in.readUTF();
                    int id = in.readInt();
                    if (Report.getById(id) != null) {
                        Report.getById(id).process(ProxyServer.getInstance().getPlayer(staff), null);
                    } else {
                        ProxyServer.getInstance().getPlayer(staff).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error occured while trying to process the "
                                + "report #" + id + "! The report has been probably processed by another staff member!")));
                    }
                    break;
                }
                case "MarkAlreadyReport": {
                    String staff = in.readUTF();
                    int id = in.readInt();
                    String punishId = in.readUTF();
                    if (Report.getById(id) != null) {
                        Report.getById(id).processAlready(ProxyServer.getInstance().getPlayer(staff), punishId);
                    } else {
                        ProxyServer.getInstance().getPlayer(staff).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error occured while trying to process the "
                                + "report #" + id + "! The report has been probably processed by another staff member!")));
                    }
                    break;
                }
            }
		}
		else if(e.getTag().equalsIgnoreCase("QueueChannel")) {
			if(subChannel.equals("QueuePlayer")) {
				String playerName = in.readUTF();
				String serverName = in.readUTF();
				if(!Queue.isInQueue(ProxyServer.getInstance().getPlayer(playerName))) {
					if(!ProxyServer.getInstance().getPlayer(playerName).getServer().getInfo().getName().equals(serverName)) {
						Queue.getByServerName(serverName).addPlayer(ProxyServer.getInstance().getPlayer(playerName));
					}
					else {
						ProxyServer.getInstance().getPlayer(playerName).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou are already on this server!")));
					}
				}
				else {
					ProxyServer.getInstance().getPlayer(playerName).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou are already in queue for the server " + 
				Queue.getByPlayer(ProxyServer.getInstance().getPlayer(playerName)).getName() + "!")));
				}
			}
			if(subChannel.equals("RemovePlayer")) {
				String playerName = in.readUTF();
				if(Queue.isInQueue(ProxyServer.getInstance().getPlayer(playerName))) {
					Queue.getByPlayer(ProxyServer.getInstance().getPlayer(playerName)).removePlayer(ProxyServer.getInstance().getPlayer(playerName));
				}
				else {
					ProxyServer.getInstance().getPlayer(playerName).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou are not currently in a queue!")));
				}
			}
		}
		else if(e.getTag().equalsIgnoreCase("ManagerChannel")) {
			if(subChannel.equals("PromotePlayer")) {
				String playerUuid = in.readUTF();
				String target = in.readUTF();
				String rank = in.readUTF();
				if(StaffMember.getByUuid(target) != null) {
					StaffMember.getByUuid(target).promote(Rank.valueOf(rank), playerUuid);
				}
				else {
					StaffMember.addPlayer(target, playerUuid, Rank.valueOf(rank));
				}
			}
			if(subChannel.equals("DemotePlayer")) {
				String playerUuid = in.readUTF();
				String target = in.readUTF();
				String rank = in.readUTF();
				StaffMember.getByUuid(target).demote(Rank.valueOf(rank), playerUuid);
			}
			if(subChannel.equals("VoteStaff")) {
				String staffUuid = in.readUTF();
				int stars = Integer.parseInt(in.readUTF());
				MainDatabase.sendStatement("INSERT INTO StaffFeedback (uuid, stars, timestamp) VALUES ('" + staffUuid + "', " + stars + ", " + System.currentTimeMillis() + ")");
			}
		}
		else if(e.getTag().equalsIgnoreCase("FeedbackChannel")) {
			if(subChannel.equals("AnswerQuestion")) {
				String playerUuid = in.readUTF();
				String id = in.readUTF();
				String jsonString;
				Gson g = new Gson();
				JsonArray answer = g.fromJson(in.readUTF(), JsonArray.class);
				jsonString = answer.toString();
				Feedback f = Main.getFeedback().getById(id);
				f.answer(playerUuid, jsonString);
			}
		}
	}
	
	public static void openSurveyMenu(ServerInfo s, String playerName, Feedback f) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("AskQuestion");
	    out.writeUTF(playerName);
	    out.writeUTF(f.getId());
	    out.writeUTF(f.getTitle());
	    JsonArray questions = new JsonArray();
	    JsonArray answers = new JsonArray();
	    for(Integer ss : f.getQuestions().keySet()) {
	    	JsonObject obj = new JsonObject();
	    	obj.addProperty(String.valueOf(ss), f.getQuestions().get(ss));
		    questions.add(obj);
		    JsonArray a = new JsonArray();
		    for(String answer : f.getOptions().get(ss)) {
			    a.add(new JsonPrimitive(answer));
		    }
	    	JsonObject ans = new JsonObject();
	    	ans.add(String.valueOf(ss), a);
		    answers.add(ans);
	    }
    	out.writeUTF(questions.toString());
    	out.writeUTF(answers.toString());
	    s.sendData("FeedbackChannel", out.toByteArray());
	}
	
	public static void openManagerMenu(ServerInfo s, String playerName) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("ManagerMenu");
	    out.writeUTF(playerName);
	    s.sendData("ManagerChannel", out.toByteArray());
	}
	
	public static void openManagerMenu(ServerInfo s, String playerName, String target) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("ManagerMenuPlayer");
	    out.writeUTF(playerName);
	    out.writeUTF(target);
	    s.sendData("ManagerChannel", out.toByteArray());
	}
	
	public static void openActionMenu(ServerInfo s, String playerName, String target, String action) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("ActionMenu");
	    out.writeUTF(playerName);
	    out.writeUTF(target);
	    out.writeUTF(action);
	    s.sendData("ManagerChannel", out.toByteArray());
	}
	
	public static void sendPlayers(ServerInfo s) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    JsonArray json = new JsonArray();
	    for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
	    	JsonObject j = new JsonObject();
	    	j.addProperty("uuid", p.getUniqueId().toString());
	    	j.addProperty("server", p.getServer().getInfo().getName());
	    	json.add(j);
	    }
	    out.writeUTF("PlayerList");
	    out.writeUTF(json.toString());
	    s.sendData("BanChannel", out.toByteArray());
	}
	
	public static void openBanMenu(ServerInfo s, String playerName, String target) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("BanMenu");
	    out.writeUTF(playerName);
	    out.writeUTF(target);
	    s.sendData("BanChannel", out.toByteArray());
	}
	
	public static void openMassBanMenu(ServerInfo s, String playerName) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("MassBanMenu");
	    out.writeUTF(playerName);
	    s.sendData("BanChannel", out.toByteArray());
	}
	
	public static void openReportsMenu(ServerInfo s, String playerName) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("ReportsMenu");
	    out.writeUTF(playerName);
	    s.sendData("ReportChannel", out.toByteArray());
	}
	
	public static void openArchivedReportsMenu(ServerInfo s, String playerName) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("ArchivedReportsMenu");
	    out.writeUTF(playerName);
	    s.sendData("ReportChannel", out.toByteArray());
	}
	
	public static void openReportMenu(ServerInfo s, String playerName, String reported) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("ReportMenu");
	    out.writeUTF(playerName);
	    out.writeUTF(reported);
	    s.sendData("ReportChannel", out.toByteArray());
	}
	
	public static void freezeStaff(ServerInfo s, String playerName) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("FreezePlayer");
	    out.writeUTF(playerName);
	    s.sendData("DiscordChannel", out.toByteArray());
	}
	
	public static void freezeUnlinkedStaff(ServerInfo s, String playerName) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("FreezeUnlinked");
	    out.writeUTF(playerName);
	    s.sendData("DiscordChannel", out.toByteArray());
	}
	
	public static void unfreezeStaff(ServerInfo s, String playerUuid) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("UnfreezePlayer");
	    out.writeUTF(playerUuid);
	    s.sendData("DiscordChannel", out.toByteArray());
	}

}
