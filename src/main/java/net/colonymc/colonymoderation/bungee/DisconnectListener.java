package net.colonymc.colonymoderation.bungee;

import net.colonymc.colonymoderation.bungee.twofa.FreezeSession;
import net.colonymc.colonymoderation.bungee.twofa.LinkedPlayer;
import net.colonymc.colonymoderation.bungee.twofa.VerifiedPlayer;
import net.dv8tion.jda.api.entities.Activity;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class DisconnectListener implements Listener {

	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent e) {
		ProxiedPlayer p = e.getPlayer();
		if(LinkedPlayer.getByPlayer(p) != null) {
			if(FreezeSession.getByPlayer(LinkedPlayer.getByPlayer(p)) != null) {
				FreezeSession.getByPlayer(LinkedPlayer.getByPlayer(p)).cancel();
			}
			if(VerifiedPlayer.getByLinkedPlayer(LinkedPlayer.getByPlayer(p)) != null) {
				VerifiedPlayer.getByLinkedPlayer(LinkedPlayer.getByPlayer(p)).startTask();
			}
			LinkedPlayer.getByPlayer(p).remove();
		}
		discordCheck(e);
	}
	
	private void discordCheck(PlayerDisconnectEvent e) {
		if(ProxyServer.getInstance().getOnlineCount() == 2) {
			Main.getJDA().getPresence().setActivity(Activity.watching("1 player online"));
		}
		else {
			Main.getJDA().getPresence().setActivity(Activity.watching((ProxyServer.getInstance().getOnlineCount() - 1) + " players online"));
		}
	}
	
}
