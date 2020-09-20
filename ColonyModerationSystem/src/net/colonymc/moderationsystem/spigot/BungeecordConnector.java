package net.colonymc.moderationsystem.spigot;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import net.colonymc.moderationsystem.bungee.bans.PunishmentType;
import net.colonymc.moderationsystem.spigot.bans.ChoosePlayerMenu;
import net.colonymc.moderationsystem.spigot.bans.ChooseReasonMenu;
import net.colonymc.moderationsystem.spigot.bans.ChooseTypeMenu;
import net.colonymc.moderationsystem.spigot.reports.ArchivedReportsMenu;
import net.colonymc.moderationsystem.spigot.reports.ReportMenu;
import net.colonymc.moderationsystem.spigot.reports.ReportsMenu;
import net.colonymc.moderationsystem.spigot.twofa.Freeze;

public class BungeecordConnector implements PluginMessageListener {
	
	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] data) {
		if(!channel.equals("BanChannel") && !channel.equals("ReportChannel") && !channel.equals("DiscordChannel")) {
			return;
		}
		ByteArrayDataInput in = ByteStreams.newDataInput(data);
		String subchannel = in.readUTF();
		if(channel.equals("BanChannel")) {
			if(subchannel.equals("BanMenu")) {
				String playerName = in.readUTF();
				String target = in.readUTF();
				if(target.equals("")) {
					new ChoosePlayerMenu(Bukkit.getPlayerExact(playerName), null, false);
				}
				else {
					new ChooseReasonMenu(Bukkit.getPlayerExact(playerName), target, -1);
				}
			}
			if(subchannel.equals("MassBanMenu")) {
				String playerName = in.readUTF();
				new ChooseTypeMenu(Bukkit.getPlayerExact(playerName));
			}
		}
		else if(channel.equals("DiscordChannel")) {
			if(subchannel.equals("FreezePlayer")) {
				String playerName = in.readUTF();
				new Freeze(playerName, true);
			}
			if(subchannel.equals("FreezeUnlinked")) {
				String playerName = in.readUTF();
				new Freeze(playerName, false);
			}
			else if(subchannel.equals("UnfreezePlayer")) {
				String playerUuid = in.readUTF();
				if(Freeze.getByUuid(playerUuid) != null) {
					Freeze.getByUuid(playerUuid).stop(true);
				}
			}
		}
		else if(channel.equals("ReportChannel")) {
			if(subchannel.equals("ReportMenu")) {
				String playerName = in.readUTF();
				String reported = in.readUTF();
				new ReportMenu(Bukkit.getPlayerExact(playerName), reported);
			}
			else if(subchannel.equals("ReportsMenu")) {
				String playerName = in.readUTF();
				new ReportsMenu(Bukkit.getPlayerExact(playerName));
			}
			else if(subchannel.equals("ArchivedReportsMenu")) {
				String playerName = in.readUTF();
				new ArchivedReportsMenu(Bukkit.getPlayerExact(playerName));
			}
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
	
	public static void servers(Player staffMember, String server) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("PlayerList");
			out.writeUTF(server);
		} catch (IOException e) {
			e.printStackTrace();
		}
		staffMember.sendPluginMessage(Main.getInstance(), "BungeeCord", bytes.toByteArray());
	}
	
	public static void sendPunishment(String playerName, Player staffMember, String reason, int reportId) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("Punish");
			out.writeUTF(playerName);
			out.writeUTF(staffMember.getName());
			out.writeUTF(reason);
			out.writeInt(reportId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		staffMember.sendPluginMessage(Main.getInstance(), "BanChannel", bytes.toByteArray());
	}
	
	public static void sendPunishment(String playerName, Player staffMember, String reason, long duration, PunishmentType type, int reportId) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("CustomPunish");
			out.writeUTF(playerName);
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
	
	public static void sendPunishment(ArrayList<String> playerNames, Player staffMember, String reason, long duration, PunishmentType type) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			String names = "";
			for(int i= 0; i < playerNames.size(); i++) {
				if(i + 1 == playerNames.size()) {
					names = names + playerNames.get(i);
				}
				else {
					names = names + playerNames.get(i) + ",";
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
			out.writeUTF(names);
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
