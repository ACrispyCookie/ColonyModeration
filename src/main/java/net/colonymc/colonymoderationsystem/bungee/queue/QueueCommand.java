package net.colonymc.colonymoderationsystem.bungee.queue;

import java.util.ArrayList;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class QueueCommand extends Command {

	public QueueCommand() {
		super("queue", "", "play");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		StringBuilder servers = new StringBuilder();
		ArrayList<String> serverList = new ArrayList<>(ProxyServer.getInstance().getServers().keySet());
		for(int i = 0; i < serverList.size(); i++) {
			if(i + 1 == serverList.size()) {
				servers.append(serverList.get(i));
			}
			else {
				servers.append(serverList.get(i)).append(", ");
			}
		}
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(args.length == 1) {
				if(serverList.contains(args[0])) {
					if(!Queue.isInQueue(p)) {
						if(!p.getServer().getInfo().getName().equals(args[0])) {
							Queue.getByServerName(args[0]).addPlayer(p);
						}
						else {
							p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou are already on this server!")));
						}
					}
					else {
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou are already in a queue for the server " + Queue.getByPlayer(p).getName() + "!")));
					}
				}
				else {
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlease enter a valid server!\n &5&l» &fAvailable servers: &d" + servers)));
				}
			}
			else if(args.length == 2 && p.hasPermission("*")) {
				if(serverList.contains(args[0])) {
					if(ProxyServer.getInstance().getPlayer(args[1]) != null) {
						if(!Queue.isInQueue(ProxyServer.getInstance().getPlayer(args[1]))) {
							if(!ProxyServer.getInstance().getPlayer(args[1]).getServer().getInfo().getName().equals(args[0])) {
								Queue.getByServerName(args[0]).addPlayer(ProxyServer.getInstance().getPlayer(args[1]));
								p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou added the player &d" + ProxyServer.getInstance().getPlayer(args[1]).getName() + " &fin the queue for the server &d" + args[0] + "&f!")));
							}
							else {
								p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is already on this server!")));
							}
						}
						else {
							p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is already in a queue for the server " + Queue.getByPlayer(ProxyServer.getInstance().getPlayer(args[1])).getName() + "!")));
						}
					}
					else {
						sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is not online!")));
					}
				}
				else {
					sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlease enter a valid server!\n &5&l» &fAvailable servers: &d" + servers)));
				}
			}
			else if(p.hasPermission("*")) {
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/queue <server> [player]\n &5&l» &fAvailable servers: &d" + servers)));
			}
			else {
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/queue <server>\n &5&l» &fAvailable servers: &d" + servers)));
			}
		}
		else {
			if(args.length == 2) {
				if(serverList.contains(args[0])) {
					if(ProxyServer.getInstance().getPlayer(args[1]) != null) {
						if(!Queue.isInQueue(ProxyServer.getInstance().getPlayer(args[1]))) {
							if(!ProxyServer.getInstance().getPlayer(args[1]).getServer().getInfo().getName().equals(args[0])) {
								Queue.getByServerName(args[0]).addPlayer(ProxyServer.getInstance().getPlayer(args[1]));
								sender.sendMessage(new TextComponent(" » You added the player " + ProxyServer.getInstance().getPlayer(args[1]).getName() + " in the queue for the server " + args[0] + "!"));
							}
							else {
								sender.sendMessage(new TextComponent(" » This player is already on this server!"));
							}
						}
						else {
							sender.sendMessage(new TextComponent(" » This player is already in a queue for the server " + Queue.getByPlayer(ProxyServer.getInstance().getPlayer(args[1])).getName() + "!"));
						}
					}
					else {
						sender.sendMessage(new TextComponent(" » This player is not online!"));
					}
				}
				else {
					sender.sendMessage(new TextComponent(" » Please enter a valid server!\n » Available servers: " + servers));
				}
			}
			else {
				sender.sendMessage(new TextComponent(" » Usage: /queue <server> <player>\n » Available servers: " + servers));
			}
		}
	}

}
