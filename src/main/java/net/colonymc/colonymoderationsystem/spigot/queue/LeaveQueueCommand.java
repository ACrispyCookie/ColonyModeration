package net.colonymc.colonymoderationsystem.spigot.queue;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.colonymc.colonymoderationsystem.spigot.BungeecordConnector;

public class LeaveQueueCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			BungeecordConnector.removeQueue(p);
		}
		else {
			sender.sendMessage(" » Please use the bungeecord console to add players to the queue!");
		}
		return false;
	}

}
