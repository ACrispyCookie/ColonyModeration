package net.colonymc.colonymoderation.bungee.discord;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class DiscordMethods {
	
	public static MessageEmbed getNormalMessage(String title, String description) {
		EmbedBuilder eb = new EmbedBuilder();
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat();
		eb.setColor(new Color(156, 39, 176));
		eb.setTitle("**" + title + "**");
		eb.setDescription(description);
		eb.setThumbnail("https://i.imgur.com/L8Qkp0O.png");
		eb.setFooter("ColonyMC » " + sdf.format(date));
		return eb.build();
	}
	
	public static MessageEmbed getNormalMessage(String title, String description, String thumbnail) {
		EmbedBuilder eb = new EmbedBuilder();
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
		eb.setColor(new Color(156, 39, 176));
		eb.setTitle("**" + title + "**");
		eb.setDescription(description);
		eb.setThumbnail(thumbnail);
		eb.setFooter("ColonyMC » " + sdf.format(date));
		return eb.build();
	}

}
