package net.colonymc.moderationsystem.bungee.bans;

import java.util.ArrayList;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class KickCommand extends Command implements TabExecutor {

	public KickCommand() {
		super("kick");
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if(args.length == 1) {
			ArrayList<String> names = new ArrayList<>();
			for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
				names.add(p.getName());
			}
			return names;
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender.hasPermission("staff.store")) {
			if(args.length >= 2) {
				if(ProxyServer.getInstance().getPlayer(args[0]) != null) {
					ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
					StringBuilder reason = new StringBuilder(args[1]);
					for(int i = 0; i < args.length; i++) {
						if(i > 1) {
							reason.append(" ").append(args[i]);
						}
					}
					if(target.hasPermission("*") && !sender.hasPermission("*")) {
						sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot kick this player!")));
					}
					else {
						target.disconnect(new TextComponent("§5§lYou have been kicked"
								+ "\n§5§lfrom our network!"
								+ "\n"
								+ "\n§fReason §5» §d" + reason 
								+ "\n§fKicked by §5» §d" + sender.getName() 
								+ "\n"
								+ "\n§dYou can now reconnect."));
					}
				}
				else {
					sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is not online!")));
				}
			}
			else {
				sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/kick <player> <reason>")));
			}
		}
	}

}
