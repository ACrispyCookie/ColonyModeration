package net.colonymc.colonymoderation.bungee.discord;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderation.Messages;
import net.colonymc.colonymoderation.bungee.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class RoleCommand extends Command {

	public RoleCommand() {
		super("dcrole");
	}

	final TextComponent usage = new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/dcrole add/remove <player> <role ID>"));
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender.hasPermission("*")) {
			if(args.length == 3) {
				if(args[0].equals("add")) {
					try {
						ResultSet rs = MainDatabase.getResultSet("SELECT * FROM VerifiedPlayers WHERE playerUuid='" + MainDatabase.getUuid(args[1]) + "';");
						if(rs.next()) {
							long userIdLong = rs.getLong("userDiscordID");
							Guild guild = Main.getMember(Main.getUser(userIdLong)).getGuild();
							long roleId = Long.parseLong(args[2]);
							if(guild.getRoleById(roleId) != null) {
								if(!Main.getMember(Main.getUser(userIdLong)).getRoles().contains(guild.getRoleById(roleId))) {
									Main.addRanksToPlayer(userIdLong, Long.parseLong(args[2]));
									sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou successfully added the role &d" 
									+ guild.getRoleById(roleId).getName() + " &fto the player &d" + args[1] + "&f!")));
								}
								else {
									sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player already has this role!")));
								}
							}
							else {
								sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis role doesn't exists!")));
							}
						}
						else {
							sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player hasn't linked their account to their discord profile!")));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				else if(args[0].equals("remove")) {
					try {
						ResultSet rs =  MainDatabase.getResultSet("SELECT * FROM VerifiedPlayers WHERE playerUuid='" + MainDatabase.getUuid(args[1]) + "';");
						if(rs.next()) {
							long userIdLong = rs.getLong("userDiscordID");
							Guild guild = Main.getMember(Main.getUser(userIdLong)).getGuild();
							long roleId = Long.parseLong(args[2]);
							if(guild.getRoleById(roleId) != null) {
								if(Main.getMember(Main.getUser(userIdLong)).getRoles().contains(guild.getRoleById(roleId))) {
									Main.removeRanksFromPlayer(userIdLong, Long.parseLong(args[2]));
									sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou removed the role &d" 
									+ guild.getRoleById(roleId).getName() + " &ffrom the player &d" + args[1] + "&f!")));
								}
								else {
									sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis user doesn't have this role!")));
								}
							}
							else {
								sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis role doesn't exist!")));
							}
						}
						else {
							sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player hasn't linked their account to their discord profile!")));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				else {
					sender.sendMessage(usage);
				}
			}
			else {
				sender.sendMessage(usage);
			}
		}
		else {
			sender.sendMessage(new TextComponent(Messages.noPerm));
		}
	}
}
