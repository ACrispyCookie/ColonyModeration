package net.colonymc.moderationsystem.spigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.colonymc.colonyapi.spigot.DatabaseConnectEvent;

public class SpigotDatabaseListener implements Listener {

	@EventHandler
	public void onDatabaseConnect(DatabaseConnectEvent e) {
		Main.getInstance().startReportsUpdating();
	}
}
