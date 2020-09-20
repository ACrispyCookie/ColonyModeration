package net.colonymc.moderationsystem.bungee.bans;

public enum PunishmentType {
	BAN("Ban"),
	MUTE("Mute");
	
	public String name;
	PunishmentType(String name){
		this.name = name;
	}
}
