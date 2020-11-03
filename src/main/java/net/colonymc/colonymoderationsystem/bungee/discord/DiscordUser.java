package net.colonymc.colonymoderationsystem.bungee.discord;

public class DiscordUser {
	
	final long userId;
	final String userTag;
	
	public DiscordUser(long userId, String userTag) {
		this.userId = userId;
		this.userTag = userTag;
	}
	
	public long getIdLong() {
		return userId;
	}
	
	public String getId() {
		return String.valueOf(userId);
	}
	
	public String getAsTag() {
		return userTag;
	}

}
