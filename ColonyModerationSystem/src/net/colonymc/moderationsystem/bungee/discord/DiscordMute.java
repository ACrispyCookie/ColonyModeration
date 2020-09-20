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

public class DiscordMute {
	
	public static ArrayList<DiscordMute> mutes = new ArrayList<DiscordMute>();
	
	DiscordUser target;
	User staff;
	String reason;
	ScheduledTask revoke;
	boolean isRevoked = false;
	long mutedUntil;
	long issuedAt;
	long messageId;
	
	public DiscordMute(DiscordUser target, User staff, String reason) {
		this.target = target;
		this.staff = staff;
		this.reason = reason;
		this.mutedUntil = decideMuteDuration() + System.currentTimeMillis();
		this.issuedAt = System.currentTimeMillis();
		mutes.add(this);
		execute();
		if(mutedUntil - System.currentTimeMillis() != -1) {
			revoke = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					revoke(false);
				}
			}, mutedUntil - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}
	}
	
	public DiscordMute(DiscordUser target, User staff, String reason, long mutedUntil, long issuedAt, long messageId) {
		this.target = target;
		this.staff = staff;
		this.reason = reason;
		this.mutedUntil = mutedUntil;
		this.issuedAt = issuedAt;
		this.messageId = messageId;
		mutes.add(this);
		if(mutedUntil != -1) {
			revoke = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					revoke(false);
				}
			}, mutedUntil - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}
	}
	
	public void execute() {
		Main.getGuild().getTextChannelById(722982663526416454L).sendMessage(DiscordMethods.getNormalMessage("Mute for the user " + target.getAsTag(), "React to this message with a ❌ to unmute the user!"
				+ "\n\nMuted by: " + staff.getAsMention() + "\nMute issued at: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(issuedAt)) + "\nMuted until: "
						+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(mutedUntil)))).queue((msg) -> {
					DiscordMute.this.messageId = msg.getIdLong();
					msg.addReaction("❌").queue();
					MainDatabase.sendStatement("INSERT INTO DiscordBans (discordId,staffId,reason,type,messageId,bannedUntil,issuedAt) VALUES "
							+ "(" + target.getIdLong() + ", " + staff.getIdLong() + ", '" + reason + "', 'mute', " + messageId + ", " 
							+ mutedUntil + ", " + issuedAt + ");");
				});
		MainDatabase.sendStatement("UPDATE DiscordInfo SET timesMuted= " + (getTimesMuted() + 1) + " WHERE discordId=" + target.getIdLong() + ";");
		Main.addRanksToPlayer(target.getIdLong(), 714160930891759677L);
	}
	
	public void revoke(boolean wasFalse) {
		revoke.cancel();
		isRevoked = true;
		if(wasFalse) {
			int timesMuted = getTimesMuted();
			MainDatabase.sendStatement("UPDATE DiscordInfo SET timesMuted= " + (timesMuted - 1) + " WHERE discordId=" + target.getIdLong() + ";");
			Main.getGuild().getTextChannelById(722982663526416454L).retrieveMessageById(messageId).complete().editMessage(DiscordMethods.getNormalMessage("User Unmuted", 
					"The user **" + target.getAsTag() + "** has been unmuted!")).queue((msg) -> {
						msg.delete().queueAfter(5, TimeUnit.SECONDS);
					});
		}
		else {
			Main.getGuild().getTextChannelById(722982663526416454L).retrieveMessageById(messageId).complete().delete().queue();
		}
		MainDatabase.sendStatement("DELETE FROM DiscordBans WHERE discordId=" + target.getIdLong() + " AND type='mute';");
		Main.removeRanksFromPlayer(target.getIdLong(), 714160930891759677L);
		mutes.remove(mutes.indexOf(this));
	}
	
	private int getTimesMuted() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM DiscordInfo WHERE discordId=" + target.getIdLong() + ";");
		try {
			if(rs.next()) {
				return rs.getInt("timesMuted");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public long decideMuteDuration() {
		switch(getTimesMuted()) {
		case 0:
			return 3600000L;
		case 1:
			return 21600000L;
		case 2:
			return 86400000L;
		case 3:
			return 604800000L;
		case 4:
			return 1209600000L;
		case 5:
			return 2592000000L;
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
		return mutedUntil;
	}
	
	public long getIssuedAt() {
		return issuedAt;
	}
	
	public boolean isRevoked() {
		return isRevoked;
	}
	
	public static DiscordMute getByMessageId(long messageId) {
		for(DiscordMute m : mutes) {
			if(m.messageId == messageId) {
				return m;
			}
		}
		return null;
	}
	
	public static DiscordMute getByTarget(User u) {
		for(DiscordMute m : mutes) {
			if(m.getTarget().getIdLong() == u.getIdLong()) {
				return m;
			}
		}
		return null;
	}
	
}
