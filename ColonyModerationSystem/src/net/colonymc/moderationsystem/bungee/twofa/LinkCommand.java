package net.colonymc.moderationsystem.bungee.twofa;

import java.awt.Color;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.colonymc.moderationsystem.bungee.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ProxyServer;

public class LinkCommand extends ListenerAdapter {
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		String[] args = e.getMessage().getContentRaw().split(" ");
		if(args.length == 2) {
			if(args[0].equals("!link") && !e.getAuthor().equals(e.getJDA().getSelfUser())) {
				if(e.getChannel().getIdLong() == 645636511496142869L) {
					if(!e.getMember().getRoles().contains(Main.getJDA().getGuildById(349836835670851584L).getRoleById(599345400062672896L))) {
						if(args[1].length() <= 16) {
							if(ProxyServer.getInstance().getPlayer(args[1]) != null) {
									if(LinkedPlayer.getByPlayer(ProxyServer.getInstance().getPlayer(args[1])) == null) {
										if(LinkRequest.getByUserID(e.getAuthor().getIdLong(), ProxyServer.getInstance().getPlayer(args[1])) == null) {
											String token = getToken();
											new LinkRequest(e.getAuthor(), ProxyServer.getInstance().getPlayer(args[1]), token).sendRequest();
											EmbedBuilder eb = new EmbedBuilder();
											eb.setTitle("**Link Request**");
											eb.setColor(new Color(156, 39, 176));
											eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
											eb.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
											eb.setDescription("A message has been sent to **" + args[1] + "** in Minecraft!\nClick **[ACCEPT]** to accept it or **[DENY]** to deny it!\nThe request will expire in **60 seconds**!");
											e.getChannel().sendMessage(eb.build()).queue();
										}
										else {
											EmbedBuilder eb = new EmbedBuilder();
											eb.setTitle("**Already Sent**");
											eb.setColor(new Color(156, 39, 176));
											eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
											eb.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
											eb.setDescription("You have already sent a request to **" + args[1] + "**!\n Please respond to it or wait for it to **expire**!");
											e.getChannel().sendMessage(eb.build()).queue();
										}
									}
									else {
										EmbedBuilder eb = new EmbedBuilder();
										eb.setTitle("**Player Already Linked**");
										eb.setColor(new Color(156, 39, 176));
										eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
										eb.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
										eb.setDescription("The player **" + args[1] + "** is already linked to another discord!\n If you own the account and you want to relink it,\ndo **/unlink** ingame and link it again!");
										e.getChannel().sendMessage(eb.build()).queue();
									}
							}
							else {
								EmbedBuilder eb = new EmbedBuilder();
								eb.setTitle("**Player Not Online**");
								eb.setColor(new Color(156, 39, 176));
								eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
								eb.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
								eb.setDescription("The player **" + args[1] + "** is currently not online!\n Please join **play.colonymc.net:25570** in order to link your account!");
								e.getChannel().sendMessage(eb.build()).queue();
							}
						}
						else {
							EmbedBuilder eb = new EmbedBuilder();
							eb.setTitle("**Player Not Valid**");
							eb.setColor(new Color(156, 39, 176));
							eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
							eb.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
							eb.setDescription("The username **" + args[1] + "** does not exist!\n Please enter a valid one!");
							e.getChannel().sendMessage(eb.build()).queue();
							e.getChannel().sendMessage(" **»** Please enter a valid username!").queue();
						}
					}
					else {
						EmbedBuilder eb = new EmbedBuilder();
						eb.setTitle("**Discord Already Linked**");
						eb.setColor(new Color(156, 39, 176));
						eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
						eb.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
						eb.setDescription("Your discord profile is already linked to the player **" + e.getMember().getNickname().substring(e.getMember().getNickname().indexOf(' ') + 1) + "**!\nIf you want to link another account,\nfirst unlink your discord from you current account!");
						e.getChannel().sendMessage(eb.build()).queue();
					}
				}
				else {
					EmbedBuilder eb = new EmbedBuilder();
					eb.setTitle("**Invalid Channel**");
					eb.setColor(new Color(156, 39, 176));
					eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
					eb.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
					eb.setDescription("Please use commands only on " + e.getGuild().getTextChannelById(645636511496142869L).getAsMention() + "!");
					e.getChannel().sendMessage(eb.build()).queue((result) -> {
						result.delete().queueAfter(5, TimeUnit.SECONDS);
					});
				}
			}
		}
	}
	
	private String getToken() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVYXZ1234567890";
		String token = "";
		for(int i = 0; i < 5; i++) {
			Random rand = new Random();
			int index = rand.nextInt(35);
			token = token + characters.substring(index, index + 1);
		}
		for(int i = 0; i < LinkRequest.requests.size(); i++) {
			LinkRequest r = LinkRequest.requests.get(i);
			if(r.token.equals(token)) {
				for(int ii = 0; ii < 5; ii++) {
					Random rand = new Random();
					int index = rand.nextInt(35);
					token = token + characters.substring(index, index + 1);
				}
				i = -1;
			}
		}
		return token;
	}

}
