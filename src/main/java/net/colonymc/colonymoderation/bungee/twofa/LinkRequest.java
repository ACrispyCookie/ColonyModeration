package net.colonymc.colonymoderation.bungee.twofa;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderation.bungee.Main;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class LinkRequest {
	
	final User discordUser;
	final ProxiedPlayer p;
	final int timeLeft;
	final String token;
	final ScheduledTask task;
	public static final ArrayList<LinkRequest> requests = new ArrayList<>();
	
	public LinkRequest(User discordUser, ProxiedPlayer p, String token) {
		this.discordUser = discordUser;
		this.p = p;
		this.timeLeft = 120;
		this.token = token;
		task = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), this::expire, 120, TimeUnit.SECONDS);
		requests.add(this);
	}
	
	public void sendRequest() {
		TextComponent accept = new TextComponent(ChatColor.translateAlternateColorCodes('&', " &a&l[ACCEPT]"));
		accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/link " + discordUser.getIdLong() + " " + token));
		TextComponent deny = new TextComponent(ChatColor.translateAlternateColorCodes('&', " &c&l[DENY]"));
		deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/link " + discordUser.getIdLong() + " ."));
		accept.addExtra(deny);
		if(p.isConnected()) {
			p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&lNEW DISCORD LINK REQUEST!")));
			p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &fThe profile &d" + discordUser.getAsTag() + " &fwants to link their profile to this account!")));
			p.sendMessage(accept);
		}
	}
	
	public void accept() {
		task.cancel();
		requests.remove(this);
		p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have linked this account with the discord profile &d" + discordUser.getAsTag() + "&f!")));
		Main.addRanksToPlayer(discordUser.getIdLong(), 599345400062672896L);
		MainDatabase.sendStatement("INSERT INTO VerifiedPlayers (playerUuid, userDiscordID) VALUES ('" + p.getUniqueId().toString() + "', " + 
		discordUser.getIdLong() + ");");
		LinkedPlayer.linked.add(new LinkedPlayer(p, discordUser));
		Main.getMember(discordUser).modifyNickname(getRankText(p.getUniqueId()) + p.getName()).queue();
	}
	
	public void reject() {
		task.cancel();
		requests.remove(this);
		p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou rejected &d" + discordUser.getAsTag() + "'s &flink request for discord!")));
		
	}
	
	public void expire() {
		requests.remove(this);
		p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe link request from &d" + discordUser.getAsTag() + " &fhas expired!")));
	}
	
	public static String getRankText(UUID uuid) {
		String rank = "";
		net.luckperms.api.model.user.User u = Main.getLuckPerms().getUserManager().getUser(uuid);
		if(u.getPrimaryGroup().equals("admin") || u.getPrimaryGroup().equals("owner") 
				|| u.getPrimaryGroup().equals("manager") || u.getPrimaryGroup().equals("mod") || u.getPrimaryGroup().equals("helper") || u.getPrimaryGroup().equals("famous")) {
			rank = "[" + getFormattedRank(u.getPrimaryGroup()) + "] ";
		}
		return rank;
	}
	
	private static String getFormattedRank(String rank) {
		String formattedRank = rank;
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
	
	public static ArrayList<LinkRequest> getRequestByPlayer(ProxiedPlayer p) {
		ArrayList<LinkRequest> list = new ArrayList<>();
		for(LinkRequest r : requests) {
			if(r.p.equals(p)) {
				list.add(r);
			}
		}
		return list;
	}
	
	public static ArrayList<LinkRequest> getRequestByUserID(long id) {
		ArrayList<LinkRequest> list = new ArrayList<>();
		for(LinkRequest r : requests) {
			if(r.discordUser.getIdLong() == id) {
				list.add(r);
			}
		}
		return list;
	}
	
	public static LinkRequest getByUserID(long id, ProxiedPlayer p) {
		for(LinkRequest r : requests) {
			if(r.discordUser.getIdLong() == id && r.p.equals(p)) {
				return r;
			}
		}
		return null;
	}

}
