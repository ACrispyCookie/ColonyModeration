package net.colonymc.moderationsystem.bungee.staffmanager;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.Messages;
import net.colonymc.moderationsystem.bungee.SpigotConnector;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PromoteCommand extends Command {

	public PromoteCommand() {
		super("promote");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(p.hasPermission("*")) {
				if(args.length == 1) {
					if(!MainDatabase.getUuid(args[0]).equals("Not Found")) {
						String uuid = MainDatabase.getUuid(args[0]);
						if(MainDatabase.getDiscordId(uuid) != 0) {
							SpigotConnector.openActionMenu(p.getServer().getInfo(), p.getName(), uuid, StaffAction.PROMOTE.name());
						}
						else {
							p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is not linked on discord!")));
						}
					}
					else {
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player has never joined the server!")));
					}
				}
				else {
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/promote <player>")));
				}
			}
			else {
				p.sendMessage(new TextComponent(Messages.noPerm));
			}
		}
		else {
			sender.sendMessage(new TextComponent(Messages.onlyPlayers));
		}
	}

}
