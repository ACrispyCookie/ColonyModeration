package net.colonymc.moderationsystem.bungee;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.colonyapi.bungee.DatabaseConnectEvent;
import net.colonymc.colonyapi.bungee.DatabaseDisconnectEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class DatabaseListener implements Listener {
	
	@EventHandler
	public void onJoin(LoginEvent e) {
		try {
			MainDatabase.isConnecting();
			if(!MainDatabase.isConnecting()) {
				if(!MainDatabase.isConnected()) {
					e.setCancelReason(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&5&l» &cAn error has occured while trying to connect the databases.\n&5&l» &cThe network will automatically restart when the database is back up!")).toPlainText());
					e.setCancelled(true);
				}
			}
			else {
				e.setCancelReason(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe proxy is still starting up. Please wait!")).toPlainText());
				e.setCancelled(true);
			}
		} catch(NoSuchMethodError e1) {
			e.setCancelReason(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe proxy is still starting up. Please wait!")).toPlainText());
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDatabaseConnect(DatabaseConnectEvent e) {
		System.out.println(ChatColor.translateAlternateColorCodes('&', "[ColonyModerationSystem] Database servers are back up! Restarting..."));
		ProxyServer.getInstance().stop();
	}
	
	@EventHandler
	public void onDatabaseDisconnect(DatabaseDisconnectEvent e) {
		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			p.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cA critical database error has occured! The proxy is restarting!")));
		}
		ProxyServer.getInstance().stop();
	}

}
