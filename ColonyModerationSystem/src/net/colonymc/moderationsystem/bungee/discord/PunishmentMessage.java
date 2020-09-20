package net.colonymc.moderationsystem.bungee.discord;

import java.util.ArrayList;

import net.colonymc.moderationsystem.bungee.bans.PunishmentType;
import net.dv8tion.jda.api.entities.User;

public class PunishmentMessage {
	
	static ArrayList<PunishmentMessage> msgs = new ArrayList<PunishmentMessage>();
	
	User target;
	User staff;
	PunishmentType type;
	long messageId;
	boolean cancelled = false;
	
	public PunishmentMessage(User target, User staff, PunishmentType type, long messageId) {
		this.target = target;
		this.staff = staff;
		this.type = type;
		this.messageId = messageId;
		msgs.add(this);
	}
	
	public User getTarget() {
		return target;
	}
	
	public User getStaff() {
		return staff;
	}
	
	public long getMessageId() {
		return messageId;
	}
	
	public PunishmentType getType() {
		return type;
	}
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
		if(this.cancelled) {
			msgs.remove(msgs.indexOf(this));
		}
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public static PunishmentMessage getByMessageId(long id) {
		for(PunishmentMessage msg : msgs) {
			if(msg.messageId == id) {
				return msg;
			}
		}
		return null;
	}

}
