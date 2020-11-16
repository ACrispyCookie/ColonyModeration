package net.colonymc.colonymoderationsystem.bungee.staffmanager;

import java.util.HashSet;
import java.util.Set;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderationsystem.Messages;
import net.colonymc.colonymoderationsystem.bungee.SpigotConnector;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class PromoteCommand extends Command implements TabExecutor {

	public PromoteCommand() {
		super("promote");
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		Set<String> matches = new HashSet<>();
		String search = args[0].toLowerCase();
		for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            if(p.getName().toLowerCase().startsWith(search.toLowerCase())) {
        		matches.add(p.getName());
            }
		}
		return matches;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(p.hasPermission("colonymc.staffmanager")) {
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
