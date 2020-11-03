package net.colonymc.colonymoderationsystem.spigot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.colonymc.colonyapi.spigot.DatabaseConnectEvent;
import net.colonymc.colonyapi.spigot.DatabaseDisconnectEvent;

public class DatabaseListener implements Listener {
	
	@EventHandler
	public void onDatabaseConnect(DatabaseConnectEvent e) {
		System.out.println(ChatColor.translateAlternateColorCodes('&', "[ColonyModerationSystem] Database servers are back up! Restarting..."));
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
	}
	
	@EventHandler
	public void onDatabaseConnect(DatabaseDisconnectEvent e) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.kickPlayer(ChatColor.translateAlternateColorCodes('&', " &5&l» &cA critical database error has occured! The proxy is restarting!"));
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
	}

}
