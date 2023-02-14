package net.colonymc.colonymoderation.bungee.discord;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderation.bungee.Main;
import net.colonymc.colonymoderation.bungee.twofa.FreezeSession;
import net.colonymc.colonymoderation.bungee.twofa.LinkedPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ProxyServer;

public class DiscordListeners extends ListenerAdapter {
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e) {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM DiscordInfo WHERE discordId=" + e.getMember().getIdLong() + ";");
		try {
			if(!rs.next()) {
				MainDatabase.sendStatement("INSERT INTO DiscordInfo (discordTag, discordId, timesBanned, timesMuted) VALUES "
						+ "('" + e.getMember().getUser().getAsTag()  + "', " + e.getMember().getIdLong() + ", 0, 0)");
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		e.getGuild().getTextChannelById(694167165263151124L).sendMessageEmbeds(DiscordMethods.getNormalMessage("Welcome " + e.getUser().getName() + ",",
				"\nto the __ColonyMC Discord Server__! You can join the server @ **play.colonymc.net**, and you can check out our store @ https://store.colonymc.net\n\nCheck " 
				+ e.getGuild().getTextChannelById(693952891051966564L).getAsMention() + " to get started!")).queue();
		Main.getGuild().getVoiceChannelById(705743260953477188L).getManager().setName("✅ Total Users: " + Main.getGuild().getMemberCount()).queue();
		ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
			Member m = e.getMember();
			ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
				if (Main.getGuild().getMembers().contains(m) && m.getRoles().contains(Main.getGuild().getRoleById(673567494010568714L)) && !m.getRoles().contains(Main.getGuild().getRoleById(367283678323408896L))) {
					Main.getGuild().kick(m);
				}
			}, 172800, TimeUnit.SECONDS);
		}, 172800, TimeUnit.SECONDS);
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent e) {
		if(e.getMessageIdLong() == 673569401618038815L) {
			Main.removeRanksFromPlayer(e.getUserIdLong(), 673567494010568714L);
			Main.addRanksToPlayer(e.getUserIdLong(), 367283678323408896L);
		}
		else {
			try {
				ResultSet rs = MainDatabase.getResultSet("SELECT * FROM ActiveReactions WHERE messageID=" + e.getMessageIdLong() + ";");
				if(rs.next() && rs.getString("messageType").equals("verification")) {
					if(e.getReaction().getEmoji().asUnicode().getName().equals("✅")) {
						if(FreezeSession.getByPlayer(LinkedPlayer.getByUser(e.getUser())) == null) {
							e.getReaction().removeReaction(e.getUser()).queue();
						}
					}
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent e) {
		User u = e.getUser();
		Member m = e.getMember();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date(m.getTimeJoined().toEpochSecond()*1000);
		e.getGuild().getTextChannelById(673569121832796196L).retrieveMessageById(673569401618038815L).complete().getReactions().get(0).removeReaction(e.getUser()).queue();
		MainDatabase.sendStatement("DELETE FROM VerifiedPlayers WHERE userDiscordID=" + e.getMember().getIdLong() + ";");
		e.getGuild().getTextChannelById(665897128891121704L).sendMessageEmbeds(DiscordMethods.getNormalMessage(u.getName() + " left...",
				"The user " + u.getAsMention() + " just left! The user first joined the server at " + sdf.format(date))).queue();
		Main.getGuild().getVoiceChannelById(705743260953477188L).getManager().setName("✅ Total Users: " + Main.getGuild().getMemberCount()).queue();
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		String[] args = e.getMessage().getContentRaw().split(" ");
		if(args[0].startsWith("!") && !e.getAuthor().equals(e.getJDA().getSelfUser())) {
			if(e.getChannel().getIdLong() == 645636511496142869L) {
				if(args.length == 1) {
					switch (args[0]) {
						case "!ip": {
							EmbedBuilder eb = new EmbedBuilder();
							eb.setTitle("**In-game IP**");
							eb.setColor(new Color(156, 39, 176));
							eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
							eb.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
							eb.setDescription("The IP of our in-game server is **play.colonymc.net**!");
							e.getChannel().sendMessageEmbeds(eb.build()).queue();
							break;
						}
						case "!online": {
							EmbedBuilder eb = new EmbedBuilder();
							eb.setTitle("**Online Players**");
							eb.setColor(new Color(156, 39, 176));
							eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
							eb.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
							if (ProxyServer.getInstance().getOnlineCount() == 1) {
								eb.setDescription("There is currently **1** player online!");
							} else {
								eb.setDescription("There are currently **" + ProxyServer.getInstance().getOnlineCount() + "** players online!");
							}
							e.getChannel().sendMessageEmbeds(eb.build()).queue();
							break;
						}
						case "!website": {
							EmbedBuilder eb = new EmbedBuilder();
							eb.setTitle("**Website Link**");
							eb.setColor(new Color(156, 39, 176));
							eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
							eb.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
							eb.setDescription("Our website is https://colonymc.net/");
							e.getChannel().sendMessageEmbeds(eb.build()).queue();
							break;
						}
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
				e.getChannel().sendMessageEmbeds(eb.build()).queue((result) -> result.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		}
	}

}
