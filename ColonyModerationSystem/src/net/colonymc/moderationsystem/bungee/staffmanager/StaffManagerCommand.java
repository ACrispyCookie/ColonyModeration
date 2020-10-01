package net.colonymc.moderationsystem.bungee.staffmanager;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.bungee.SpigotConnector;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class StaffManagerCommand extends Command {

	public StaffManagerCommand() {
		super("staffmanager", "", new String[] {"sm", "staffm", "smanager"});
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(p.hasPermission("colonymc.staffmanager")) {
				if(args.length == 0) {
					SpigotConnector.openManagerMenu(p.getServer().getInfo(), p.getName());
				}
				else if(args.length == 1) {
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
