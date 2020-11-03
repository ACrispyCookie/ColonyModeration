package net.colonymc.moderationsystem.bungee.staffmanager;

public enum Rank {
	KNIGHT("Knight", (short) 7, '7', 367283678323408896L),
	PRINCE("Prince", (short) 4, 'e', 349847789506789379L),
	KING("King", (short) 1, '6', 349847789506789379L),
	ARCHON("Archon", (short) 9, '3', 349847789506789379L),
	OVERLORD("Overlord", (short) 14, 'c', 349847789506789379L),
	COLONY("Colony", (short) 10, '5', 349847789506789379L),
	MEDIARANK("Media", (short) 6, 'd', 349847148474662913L),
	BUILDER("Builder", (short) 4, 'e', 678263538346885145L),
	HELPER("Helper", (short) 3, 'b', 349847421175726081L),
	MODERATOR("Moderator", (short) 10, '5', 349847614486872064L),
	ADMIN("Administrator", (short) 14, 'c', 349845096096595969L),
	MANAGER("Manager", (short) 14, 'c', 711957460264681482L),
	OWNER("Owner", (short) 14, '4', 349846353364385792L);
	
	final String name;
	final short color;
	final char chatColor;
	final long discordRole;

	public String getName() {
		return name;
	}
	
	public short getColor() {
		return color;
	}
	
	public char getChatColor() {
		return chatColor;
	}
	
	public long getDicordRole() {
		return discordRole;
	}
	
	Rank(String name, short color, char chatColor, long discordRole){
		this.name = name;
		this.color = color;
		this.chatColor = chatColor;
		this.discordRole = discordRole;
	}
}
