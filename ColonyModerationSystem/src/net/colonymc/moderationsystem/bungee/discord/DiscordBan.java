package net.colonymc.moderationsystem.bungee.discord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.bungee.Main;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class DiscordBan {
	
	public static ArrayList<DiscordBan> bans = new ArrayList<DiscordBan>();
	
	DiscordUser target;
	User staff;
	String reason;
	ScheduledTask revoke;
	boolean isRevoked = false;
	long bannedUntil;
	long issuedAt;
	long messageId;
	
	public DiscordBan(DiscordUser target, User staff, String reason) {
		this.target = target;
		this.staff = staff;
		this.reason = reason;
		this.bannedUntil = decideBanDuration() + System.currentTimeMillis();
		this.issuedAt = System.currentTimeMillis();
		bans.add(this);
		execute();
		if(bannedUntil - System.currentTimeMillis() != -1) {
			revoke = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					revoke(false);
				}
			}, bannedUntil - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}
	}
	
	public DiscordBan(DiscordUser target, User staff, String reason, long bannedUntil, long issuedAt, long messageId) {
		this.target = target;
		this.staff = staff;
		this.reason = reason;
		this.bannedUntil = bannedUntil;
		this.issuedAt = issuedAt;
		this.messageId = messageId;
		bans.add(this);
		if(bannedUntil != -1) {
			revoke = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					revoke(false);
				}
			}, bannedUntil - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}
	}
	
	public void execute() {
		Main.getGuild().getTextChannelById(722982688801161247L).sendMessage(DiscordMethods.getNormalMessage("Ban for the user " + target.getAsTag(), "React to this message with a ❌ to unban the user!"
				+ "\n\nBanned by: " + staff.getAsMention() + "\nBan issued at: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(issuedAt)) + "\nBanned until: "
						+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(bannedUntil)))).queue((msg) -> {
					DiscordBan.this.messageId = msg.getIdLong();
					msg.addReaction("❌").queue();
					MainDatabase.sendStatement("INSERT INTO DiscordBans (discordId,staffId,reason,type,messageId,bannedUntil,issuedAt) VALUES "
							+ "(" + target.getIdLong() + ", " + staff.getIdLong() + ", '" + reason + "', 'ban', " + messageId + ", " 
							+ bannedUntil + ", " + issuedAt + ");");
				});
		MainDatabase.sendStatement("UPDATE DiscordInfo SET timesBanned= " + (getTimesBanned() + 1) + " WHERE discordId=" + target.getIdLong() + ";");
		Main.getGuild().ban(target.getId(), 0, reason).queue();
	}
	
	public void revoke(boolean wasFalse) {
		revoke.cancel();
		isRevoked = true;
		if(wasFalse) {
			int timesBanned = getTimesBanned();
			MainDatabase.sendStatement("UPDATE DiscordInfo SET timesBanned= " + (timesBanned - 1) + " WHERE discordId=" + target.getIdLong() + ";");
			Main.getGuild().getTextChannelById(722982688801161247L).retrieveMessageById(messageId).complete().editMessage(DiscordMethods.getNormalMessage("User Unbanned", 
					"The user **" + target.getAsTag() + "** has been unbanned!")).queue((msg) -> {
						msg.delete().queueAfter(5, TimeUnit.SECONDS);
					});
		}
		else {
			Main.getGuild().getTextChannelById(722982688801161247L).retrieveMessageById(messageId).complete().delete().queue();
		}
		MainDatabase.sendStatement("DELETE FROM DiscordBans WHERE discordId=" + target.getIdLong() + " AND type='ban';");
		Main.getGuild().unban(target.getId()).queue();
		bans.remove(bans.indexOf(this));
	}
	
	private int getTimesBanned() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM DiscordInfo WHERE discordId=" + target.getIdLong() + ";");
		try {
			if(rs.next()) {
				return rs.getInt("timesBanned");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public long decideBanDuration() {
		switch(getTimesBanned()) {
		case 0:
			return 1209600000L;
		case 1:
			return 2592000000L;
		case 2:
			return 5184000000L;
		case 3:
			return -1;
		}
		return -1;
	}
	
	public DiscordUser getTarget() {
		return target;
	}
	
	public User getStaff() {
		return staff;
	}
	
	public String getReason() {
		return reason;
	}
	
	public long getBannedUntil() {
		return bannedUntil;
	}
	
	public long getIssuedAt() {
		return issuedAt;
	}
	
	public boolean isRevoked() {
		return isRevoked;
	}
	
	public static DiscordBan getByTarget(User u) {
		for(DiscordBan m : bans) {
			if(m.getTarget().getIdLong() == u.getIdLong()) {
				return m;
			}
		}
		return null;
	}
	
	public static DiscordBan getByMessageId(long messageId) {
		for(DiscordBan m : bans) {
			if(m.messageId == messageId) {
				return m;
			}
		}
		return null;
	}
	
}
