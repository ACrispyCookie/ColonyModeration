package net.colonymc.colonymoderationsystem.bungee.staffmanager;

import java.util.HashSet;
import java.util.Set;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.colonymoderationsystem.bungee.SpigotConnector;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class StaffManagerCommand extends Command implements TabExecutor {

	public StaffManagerCommand() {
		super("staffmanager", "", "sm", "staffm", "smanager");
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		Set<String> matches = new HashSet<>();
		String search = args[0].toLowerCase();
		for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            if(p.getName().toLowerCase().startsWith(search.toLowerCase()) && p.hasPermission("staff.store")) {
        		matches.add(p.getName());
            }
		}
		return matches;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(p.hasPermission("staff.store")) {
				if(args.length == 0) {
					SpigotConnector.openManagerMenu(p.getServer().getInfo(), p.getName());
				}
				else if(args.length == 1) {
					if(p.hasPermission("colonymc.staffmanager")) {
						if(MainDatabase.getUuid(args[0]) != null) {
							if(MainDatabase.isStaff(args[0])) {
								SpigotConnector.openManagerMenu(p.getServer().getInfo(), p.getName(), MainDatabase.getUuid(args[0]));
							}
							else {
								p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is not a staff member!")));
							}
						}
						else {
							p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player has never joined the server!")));
						}
					}
					else {
						SpigotConnector.openManagerMenu(p.getServer().getInfo(), p.getName());
					}
				}
			}
			else {
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot execute this command!")));
			}
		}
		else {
			sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&5&l» &cOnly players can execute this command!")));
		}
	}

}
