package net.colonymc.moderationsystem.bungee.discord;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.bungee.bans.PunishmentType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordPunishCommand extends ListenerAdapter {
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		String[] args = e.getMessage().getContentRaw().split(" ");
		if(args[0].startsWith("!") && !e.getAuthor().equals(e.getJDA().getSelfUser())) {
			if(e.getChannel().getIdLong() == 645636511496142869L) {
				if(args[0].equals("!ban") || args[0].equals("!mute") || args[0].equals("!punish") || args[0].equals("!tempban") || args[0].equals("!tempmute")) {
					if(args.length == 2 && !e.getMessage().getMentionedUsers().isEmpty()) {
						User target = e.getMessage().getMentionedUsers().get(0);
						if(getTimesMuted(target) == 6) {
							e.getTextChannel().sendMessage(DiscordMethods.getNormalMessage("Punishing " + target.getAsTag(), "Please react to this message to choose a reason!\n \n"
									+ "  » 1️⃣ for Offensive language\n  » 2️⃣ for Negative behaviour\n  » 3️⃣ for Advertising\n  » 4️⃣ for Spamming\n  » ❌ to cancel the process!")).queue((result) -> {
										new PunishmentMessage(target, e.getAuthor(), PunishmentType.BAN, result.getIdLong());
										result.addReaction("1️⃣").queue();
										result.addReaction("2️⃣").queue();
										result.addReaction("3️⃣").queue();
										result.addReaction("4️⃣").queue();
										result.addReaction("❌").queue();
									});
						}
						else {
							e.getTextChannel().sendMessage(DiscordMethods.getNormalMessage("Punishing " + target.getAsTag(), "Please react to this message to choose a reason!\n \n"
									+ "  » 1️⃣ for Offensive language\n  » 2️⃣ for Negative behaviour\n  » 3️⃣ for Advertising\n  » 4️⃣ for Spamming\n  » ❌ to cancel the process!")).queue((result) -> {
										new PunishmentMessage(target, e.getAuthor(), PunishmentType.MUTE, result.getIdLong());
										result.addReaction("1️⃣").queue();
										result.addReaction("2️⃣").queue();
										result.addReaction("3️⃣").queue();
										result.addReaction("4️⃣").queue();
										result.addReaction("❌").queue();
									});
						}
					}
					else {
						e.getTextChannel().sendMessage(DiscordMethods.getNormalMessage("Ban command usage", "Usage: /ban <mentioned user>")).queue();
					}
				}
			}
			else {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setTitle("**Invalid Channel**");
				eb.setColor(new Color(156, 39, 176));
				eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
				eb.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
				eb.setDescription("Please use commands only on " + e.getGuild().getTextChannelById(645636511496142869L).getAsMention() + "!");
				e.getChannel().sendMessage(eb.build()).queue((result) -> result.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		}
	}
	
	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e) {
		if(PunishmentMessage.getByMessageId(e.getMessageIdLong()) != null) {
			PunishmentMessage msg = PunishmentMessage.getByMessageId(e.getMessageIdLong());
			if(e.getUser().equals(msg.getStaff())) {
				if(e.getReactionEmote().getName().equals("❌")) {
					if(msg.isCancelled()) {
						msg.setCancelled(true);
						e.getChannel().retrieveMessageById(e.getMessageIdLong()).complete().removeReaction("1️⃣").queue();
						e.getChannel().retrieveMessageById(e.getMessageIdLong()).complete().removeReaction("2️⃣").queue();
						e.getChannel().retrieveMessageById(e.getMessageIdLong()).complete().removeReaction("3️⃣").queue();
						e.getChannel().retrieveMessageById(e.getMessageIdLong()).complete().removeReaction("4️⃣").queue();
						e.getChannel().retrieveMessageById(e.getMessageIdLong()).complete().removeReaction("❌").queue();
						e.getChannel().retrieveMessageById(e.getMessageIdLong()).complete().editMessage(DiscordMethods.getNormalMessage("Process cancelled!", 
								"The punishment process for the user " + msg.getTarget().getName() + " has been cancelled!")).queue((result) -> result.delete().queueAfter(5, TimeUnit.SECONDS));
					}
					else if(!e.getUser().equals(e.getJDA().getSelfUser())){
						e.getReaction().removeReaction(e.getUser()).queue();
					}
				}
				else {
					if(msg.isCancelled()) {
						msg.setCancelled(true);	
						e.getChannel().retrieveMessageById(e.getMessageIdLong()).complete().delete().queue();
						String action = "";
						String reason = "";
						if(msg.getType() == PunishmentType.BAN) {
							action = "Banned";
							if(e.getReactionEmote().getName().equals("1️⃣")) {
								reason = "Offensive language";
								new DiscordBan(new DiscordUser(msg.getTarget().getIdLong(), msg.getTarget().getAsTag()), msg.getStaff(), "Offensive language");
							}
							else if(e.getReactionEmote().getName().equals("2️⃣")) {
								reason = "Negative behaviour";
								new DiscordBan(new DiscordUser(msg.getTarget().getIdLong(), msg.getTarget().getAsTag()), msg.getStaff(), "Negative behaviour");
							}
							else if(e.getReactionEmote().getName().equals("3️⃣")) {
								reason = "Advertising";
								new DiscordBan(new DiscordUser(msg.getTarget().getIdLong(), msg.getTarget().getAsTag()), msg.getStaff(), "Advertising");
							}
							else if(e.getReactionEmote().getName().equals("4️⃣")) {
								reason = "Spamming";
								new DiscordBan(new DiscordUser(msg.getTarget().getIdLong(), msg.getTarget().getAsTag()), msg.getStaff(), "Spamming");
							}
						}
						else {
							action = "Muted";
							if(e.getReactionEmote().getName().equals("1️⃣")) {
								reason = "Offensive language";
								new DiscordMute(new DiscordUser(msg.getTarget().getIdLong(), msg.getTarget().getAsTag()), msg.getStaff(), "Offensive language");
							}
							else if(e.getReactionEmote().getName().equals("2️⃣")) {
								reason = "Negative behaviour";
								new DiscordMute(new DiscordUser(msg.getTarget().getIdLong(), msg.getTarget().getAsTag()), msg.getStaff(), "Negative behaviour");
							}
							else if(e.getReactionEmote().getName().equals("3️⃣")) {
								reason = "Advertising";
								action = "Banned";
								new DiscordBan(new DiscordUser(msg.getTarget().getIdLong(), msg.getTarget().getAsTag()), msg.getStaff(), "Advertising");
							}
							else if(e.getReactionEmote().getName().equals("4️⃣")) {
								reason = "Spamming";
								new DiscordMute(new DiscordUser(msg.getTarget().getIdLong(), msg.getTarget().getAsTag()), msg.getStaff(), "Spamming");
							}
						}
						e.getChannel().sendMessage(DiscordMethods.getNormalMessage("User " + action, 
								"The user " + msg.getTarget().getAsTag() + " has been punished by " + msg.getStaff().getAsMention() + " for the reason **" + reason + "**!")).queue();
					}
					else if(!e.getUser().equals(e.getJDA().getSelfUser())){
						e.getReaction().removeReaction(e.getUser()).queue();
					}
				}
			}
			else if(!e.getUser().equals(e.getJDA().getSelfUser())){
				e.getReaction().removeReaction(e.getUser()).queue();
			}
		}
		else if(DiscordBan.getByMessageId(e.getMessageIdLong()) != null || DiscordMute.getByMessageId(e.getMessageIdLong()) != null) {
			if(e.getReactionEmote().getName().equals("❌")) {
				if(DiscordBan.getByMessageId(e.getMessageIdLong()) != null) {
					DiscordBan m = DiscordBan.getByMessageId(e.getMessageIdLong());
					if(!m.isRevoked() && e.getUser().equals(m.getStaff())) {
						e.getReaction().removeReaction(e.getUser()).queue();
						m.revoke(true);
					}
					else if(!e.getUser().equals(e.getJDA().getSelfUser())){
						e.getReaction().removeReaction(e.getUser()).queue();
					}
				}
				else {
					DiscordMute m = DiscordMute.getByMessageId(e.getMessageIdLong());
					if(!m.isRevoked() && e.getUser().equals(m.getStaff())) {
						e.getReaction().removeReaction(e.getUser()).queue();
						m.revoke(true);
					}
					else if(!e.getUser().equals(e.getJDA().getSelfUser())){
						e.getReaction().removeReaction(e.getUser()).queue();
					}
				}
			}
			else if(!e.getUser().equals(e.getJDA().getSelfUser())){
				e.getReaction().removeReaction(e.getUser()).queue();
			}
		}
	}
	
	private int getTimesMuted(User target) {
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
}
