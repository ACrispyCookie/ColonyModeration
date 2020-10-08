package net.colonymc.moderationsystem.bungee.bans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.colonyapi.Time;
import net.colonymc.moderationsystem.bungee.Main;
import net.colonymc.moderationsystem.bungee.reports.Report;
import net.colonymc.moderationsystem.bungee.twofa.FreezeSession;
import net.colonymc.moderationsystem.bungee.twofa.LinkedPlayer;
import net.colonymc.moderationsystem.bungee.twofa.VerifiedPlayer;
import net.dv8tion.jda.api.entities.Member;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

public class JoinListener implements Listener {
	
	static ScheduledTask updateName;
	
	@EventHandler
	public void onJoin(LoginEvent e) {
		checkIfBanned(e);
		updateDatabase(e);
	}

	@EventHandler
	public void onJoin(ServerConnectEvent e) {
		addToMaps(e);
	}
	
	@EventHandler
	public void onJoin(ServerSwitchEvent e) {
		sendReports(e);
	}
	
	public void sendReports(ServerSwitchEvent e) {
		ProxiedPlayer p = e.getPlayer();
		if(p.hasPermission("staff.store")) {
			if(Report.reports.size() > 0) {
				ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
					@Override
					public void run() {
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&lOPEN REPORTS")));
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &fThere are still &d" + Report.reports.size() + " &freports open!")));
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &fType &d/reports &fto process the reports!")));
					}
				}, 2, TimeUnit.SECONDS);
			}
		}
	}
	
	public void sendReports(ServerConnectEvent e) {
		ProxiedPlayer p = e.getPlayer();
		if(p.hasPermission("staff.store")) {
			if(Report.reports.size() > 0) {
				ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
					@Override
					public void run() {
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&lOPEN REPORTS")));
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &fThere are still &d" + Report.reports.size() + " &freports open!")));
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &fType &d/reports &fto process the reports!")));
					}
				}, 2, TimeUnit.SECONDS);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void addToMaps(ServerConnectEvent e) {
		ProxiedPlayer p = e.getPlayer();
		if(p.getServer() == null) {
			ResultSet discordVerified = MainDatabase.getResultSet("SELECT * FROM VerifiedPlayers WHERE playerUuid='" + p.getUniqueId().toString() + "';");
			try {
				if(discordVerified.next()) {
					long discordId = discordVerified.getLong("userDiscordID");
					LinkedPlayer.linked.add(new LinkedPlayer(p, Main.getUser(discordId)));
					if(p.hasPermission("staff.store")) {
						VerifiedPlayer verp = VerifiedPlayer.getByLinkedPlayer(LinkedPlayer.getByPlayer(p));
						if(verp != null && !verp.getCurrentIP().equals(p.getAddress().getHostString())) {
							verp.remove();
							new FreezeSession(LinkedPlayer.getByPlayer(p), e.getTarget());
						}
						else if(verp == null) {
							new FreezeSession(LinkedPlayer.getByPlayer(p), e.getTarget());
						}
					}
				}
				else {
					if(p.hasPermission("staff.store")) {
						new FreezeSession(p, e.getTarget());
					}
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void updateDatabase(LoginEvent e) {
		PendingConnection c = e.getConnection();
		if(!e.isCancelled()) {
			ResultSet pInfo = MainDatabase.getResultSet("SELECT * FROM PlayerInfo WHERE uuid='" + c.getUniqueId().toString() + "';");
			try {
				if(pInfo.next()) {
					MainDatabase.sendStatement("UPDATE PlayerInfo SET ip='" + c.getAddress().getHostString() + "',lastJoinTime=" + System.currentTimeMillis() + ",name='" + c.getName() 
					+ "',skin='" + getSkin(c.getUniqueId().toString()) + "' WHERE uuid='" + c.getUniqueId().toString() + "';");
				}
				else {
					MainDatabase.sendStatement("INSERT INTO PlayerInfo (name, uuid, ip, timesBanned, timesMuted, firstJoinTime, lastJoinTime, skin) VALUES "
							+ "('" + c.getName() + "', '" + c.getUniqueId().toString() + "', '" + c.getAddress().getHostString() + "', 0, 0, " 
							+ System.currentTimeMillis() + ", " + System.currentTimeMillis() + ", '" + getSkin(c.getUniqueId().toString()) + "');");
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		ResultSet discordVerified = MainDatabase.getResultSet("SELECT * FROM VerifiedPlayers WHERE playerUuid='" + c.getUniqueId().toString() + "';");
		try {
			if(discordVerified.next()) {
				long discordId = discordVerified.getLong("userDiscordID");
				MainDatabase.sendStatement("UPDATE DiscordInfo SET discordTag='" + Main.getUser(discordId).getAsTag() 
						+ "' WHERE discordId=" + discordId + ";");
				setRankText(Main.getMember(Main.getUser(discordId)), c.getName(), c.getUniqueId());
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private void checkIfBanned(LoginEvent e) {
		PendingConnection c = e.getConnection();
		ResultSet banned = MainDatabase.getResultSet("SELECT * FROM ActiveBans WHERE uuid='" + c.getUniqueId().toString() + "';");
		try {
			if(Ban.getByUuid(c.getUniqueId().toString()) != null) {
				Ban ban = Ban.getByUuid(c.getUniqueId().toString());
				e.setCancelReason(createReasonComponent(ban.getStaff(), ban.getBannedUntil() - System.currentTimeMillis(), ban.getReason(), ban.getId()));
				e.setCancelled(true);
				banned.next();
				String bannedName = MainDatabase.getName(banned.getString("uuid"));
				String bannedIp = banned.getString("bannedIp");
				if(!bannedName.equals(c.getName())) {
					ban.name = c.getName();
					MainDatabase.sendStatement("UPDATE PlayerInfo SET name='" + c.getName() + "' WHERE uuid='" + c.getUniqueId().toString() + "';");
				}
				if(!bannedIp.equals(c.getAddress().getHostString())) {
					ban.bannedIp = c.getAddress().getHostString();
					MainDatabase.sendStatement("UPDATE ActiveBans SET bannedIp='" + c.getAddress().getHostString() + "' WHERE uuid='" + c.getUniqueId().toString() + "';");
					MainDatabase.sendStatement("UPDATE PlayerInfo SET ip='" + c.getAddress().getHostString() + "' WHERE uuid='" + c.getUniqueId().toString() + "';");
				}
			}
			else if(Ban.getByIp(c.getAddress().getHostString()) != null) {
				Ban ban = Ban.getByIp(c.getAddress().getHostString());
				e.setCancelReason(createBanEvasionComponent(ban.getPlayerName(), ban.getReason(), ban.getStaff(), ban.getBannedUntil() - System.currentTimeMillis()));
				e.setCancelled(true);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void setRankText(Member member, String name, UUID uuid) {
		User u = Main.getLuckPerms().getUserManager().getUser(uuid);
		updateName = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
			if(u != null) {
				if(u.getPrimaryGroup().equals("admininstrator") || u.getPrimaryGroup().equals("owner") 
						|| u.getPrimaryGroup().equals("manager") || u.getPrimaryGroup().equals("moderator") || u.getPrimaryGroup().equals("helper") || u.getPrimaryGroup().equals("media")) {
					String rank = "[" + getFormattedRank(u.getPrimaryGroup()) + "] " + name;
					if(rank != null) {
						if(!member.getNickname().equals(rank)) {
							updateName.cancel();
							member.modifyNickname(rank).queue();
						}
					}
				}
			}
		}, 1, 1, TimeUnit.SECONDS);
	}
	
	private static String getFormattedRank(String rank) {
		String formattedRank = rank;
		if(formattedRank.equals("administrator")) {
			formattedRank = "admin";
		}
		else if(formattedRank.equals("moderator")) {
			formattedRank = "mod";
		}
		formattedRank = formattedRank.replaceAll("a", "ᴀ");
		formattedRank = formattedRank.replaceAll("d", "ᴅ");
		formattedRank = formattedRank.replaceAll("m", "ᴍ");
		formattedRank = formattedRank.replaceAll("i", "ɪ");
		formattedRank = formattedRank.replaceAll("n", "ɴ");
		formattedRank = formattedRank.replaceAll("g", "ɢ");
		formattedRank = formattedRank.replaceAll("e", "ᴇ");
		formattedRank = formattedRank.replaceAll("r", "ʀ");
		formattedRank = formattedRank.replaceAll("h", "ʜ");
		formattedRank = formattedRank.replaceAll("l", "ʟ");
		formattedRank = formattedRank.replaceAll("o", "ᴏ");
		formattedRank = formattedRank.replaceAll("w", "ᴡ");
		formattedRank = formattedRank.replaceAll("t", "ᴛ");
		formattedRank = formattedRank.replaceAll("p", "ᴘ");
		return formattedRank;
	}
	
	private TextComponent createReasonComponent(String staff, long duration, String reason, String ID) {
		String finalText = "§5§lYour account has been \n§5§ltemporarily suspended from our network!\n§5\n§fReason §5» §d" + reason + "\n§fBanned by §5» §d" + staff + "\n§fUnban in §5»"
					+ " §d" + Time.formatted(duration/1000) + "\n§fBan ID §5» §d#" + ID + "\n\n§fYou can write a ban appeal by opening a ticket here:\n§dhttps://www.colonymc.net/appeal";
		return new TextComponent(finalText);
	}
	
	private TextComponent createBanEvasionComponent(String bannedName, String reason, String staff, long duration) {
		String finalText = "§5§lAnother account with the same IP\n§5§lhas been temporarily suspended from our network!\n§5\n§fAccount's name §5» §d" + 
	bannedName + "\n§fReason §5» §d" + reason + "\n§fBanned by §5» §d" + staff + "\n§fUnban in §5»"
					+ " §d" + Time.formatted(duration/1000) 
					+ "\n§fLogin from your other\n§faccount in order to get your §dBan ID\n\n§fYou can write a ban appeal by opening a ticket here:\n§dhttps://www.colonymc.net/appeal";
		return new TextComponent(finalText);
	}
	
	private String getSkin(String uuid) {
		return getHeadValue(uuid.replaceAll("-", ""));
	}
	
	private  String getHeadValue(String uid){
	    try {
	        Gson g = new Gson();
	        String signature = getURLContent("https://sessionserver.mojang.com/session/minecraft/profile/" + uid);
	        JsonObject obj = g.fromJson(signature, JsonObject.class);
	        String value = obj.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
	        return value;
	    } catch (Exception ignored){ }
	    return null;
	}
	
	private String getURLContent(String urlStr) {
	        URL url;
	        BufferedReader in = null;
	        StringBuilder sb = new StringBuilder();
	        try{
	            url = new URL(urlStr);
	            in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8) );
	            String str;
	            while((str = in.readLine()) != null) {
	                sb.append( str );
	            }
	        } catch (Exception ignored) { }
	        finally{
	            try{
	                if(in!=null) {
	                    in.close();
	                }
	            }catch(IOException ignored) { }
	        }
	        return sb.toString();
	    }

}
