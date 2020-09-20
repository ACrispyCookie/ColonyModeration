package net.colonymc.moderationsystem.bungee.staffmanager;

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
					
				}
				else if(args.length == 1) {
					
				}
				else if(args.length == 2) {
					
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
