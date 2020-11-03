package net.colonymc.colonymoderationsystem.bungee.bans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.colonymoderationsystem.Messages;
import net.colonymc.colonymoderationsystem.bungee.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class UnmuteCommand extends Command implements TabExecutor {

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		Set<String> matches = new HashSet<>();
		String search = args[0].toLowerCase();
		for (Mute m : Mute.mutes) {
            if(m.getPlayerName().toLowerCase().startsWith(search.toLowerCase())) {
        		matches.add(m.getPlayerName());
            }
		}
		return matches;
	}
	
	public UnmuteCommand() {
		super("unmute");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender.hasPermission("staff.store")) {
			if(args.length == 1) {
				try {
					ResultSet rs = MainDatabase.getResultSet("SELECT uuid FROM PlayerInfo WHERE name='" + args[0] + "';");
					if(rs.next()) {
						String uuid = rs.getString("uuid");
						unmutePlayer(args[0], uuid, sender);
					}
					else {
						sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe player &d" + args[0] + " &fdoes not exist!")));
					}
				} catch (SQLException e) {
					e.printStackTrace();
					sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error has occured, please try again! If this error continues to show up please report it to an admin!")));
				}
			}
			else {
				sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/unmute <player>")));
			}
		}
		else {
			sender.sendMessage(new TextComponent(Messages.noPerm));
		}
	}

	public void unmutePlayer(String target, String uuid, CommandSender sender) {
		ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
			try {
				ResultSet result = MainDatabase.getResultSet("SELECT uuid FROM ActiveMutes WHERE uuid='" + uuid + "';");
				if(result.next()) {
					sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe player &d" + Mute.getByUuid(uuid).name + " &fhas been unmuted!")));
					Mute.getByUuid(uuid).revoke(true);
				}
				else {
					sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe player &d" + target + " &fis not muted!")));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error has occured please report it to the admins!")));
				Main.writeToLog("Failed to unmute " + target + " by the staff member " + sender.getName() + ". (on UnmuteCommand)");
			}
		});
	}
}
