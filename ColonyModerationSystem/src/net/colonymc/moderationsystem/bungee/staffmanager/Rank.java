package net.colonymc.moderationsystem.bungee.staffmanager;

public enum Rank {
	KNIGHT("Knight"),
	PRINCE("Prince"),
	KING("King"),
	ARCHON("Archon"),
	OVERLORD("Overlord"),
	COLONY("Colony"),
	HELPER("Helper"),
	MODERATOR("Moderator"),
	ADMIN("Administrator"),
	MANAGER("Manager"),
	OWNER("Owner");
	
	String name;

	Rank(String name){
		this.name = name;
	}
}
