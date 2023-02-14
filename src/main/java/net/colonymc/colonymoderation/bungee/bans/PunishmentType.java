package net.colonymc.colonymoderation.bungee.bans;

public enum PunishmentType {
	BAN("Ban"),
	MUTE("Mute");
	
	public final String name;
	PunishmentType(String name){
		this.name = name;
	}
}
