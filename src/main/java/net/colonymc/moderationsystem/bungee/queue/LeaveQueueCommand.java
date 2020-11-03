package net.colonymc.moderationsystem.bungee.queue;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LeaveQueueCommand extends Command {

	public LeaveQueueCommand() {
		super("leavequeue", "", "leaveplay");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(args.length == 1 && p.hasPermission("*")) {
				if(ProxyServer.getInstance().getPlayer(args[0]) != null) {
					ProxiedPlayer queued = ProxyServer.getInstance().getPlayer(args[0]);
					if(Queue.isInQueue(queued)) {
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou removed the player &d" + queued.getName() + " &ffrom the queue for the server &d" + Queue.getByPlayer(queued).getName() + "&f!")));
						Queue.getByPlayer(queued).removePlayer(queued);
					}
					else {
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is not in a queue!")));
					}
				}
				else {
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is not online!")));
				}
			}
			else {
				if(Queue.isInQueue(p)) {
					Queue.getByPlayer(p).removePlayer(p);
				}
				else {
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou are not currently in a queue!")));
				}
			}
		}
		else {
			if(args.length == 1) {
				if(ProxyServer.getInstance().getPlayer(args[0]) != null) {
					ProxiedPlayer queued = ProxyServer.getInstance().getPlayer(args[0]);
					if(Queue.isInQueue(queued)) {
						sender.sendMessage(new TextComponent(" » You removed the player " + queued.getName() + " from the queue for the server " + Queue.getByPlayer(queued).getName() + "!"));
						Queue.getByPlayer(queued).removePlayer(queued);
					}
					else {
						sender.sendMessage(new TextComponent(" » This player is not in a queue!"));
					}
				}
				else {
					sender.sendMessage(new TextComponent(" » This player is not online!"));
				}
			}
			else {
				sender.sendMessage(new TextComponent(" » Usage: /leavequeue <player>"));
			}
		}
	}

}
