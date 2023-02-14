package net.colonymc.colonymoderation.bungee.chatmoderation;

import java.util.HashMap;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {
	
	final HashMap<ProxiedPlayer, Long> slowed = new HashMap<>();
	
	@EventHandler
	public void onChat(ChatEvent e) {
		if(e.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) e.getSender();
			if(!e.getMessage().startsWith("/")) {
				if(ChatCommand.toggled.contains(p.getServer().getInfo().getName()) || ChatCommand.toggled.contains("all")) {
					if(!p.hasPermission("staff.store")) {
						e.setCancelled(true);
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe chat is currently disabled for the whole network! Please wait until an admin enables it!")));
					}
				}
				else if(ChatCommand.slow.containsKey(p.getServer().getInfo().getName())) {
					if(slowed.containsKey(p)) {
						if(slowed.get(p) > System.currentTimeMillis()) {
							e.setCancelled(true);
							int timeLeft= (int) ((slowed.get(p) - System.currentTimeMillis())/1000) + 1;
							p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cPlease wait another " + timeLeft
									+ " seconds! The whole network is currently in slow mode!")));
						}
						else {
							slowed.remove(p);
							slowed.put(p, System.currentTimeMillis() + (ChatCommand.slow.get(p.getServer().getInfo().getName()) * 1000));
						}
					}
					else {
						slowed.put(p, System.currentTimeMillis() + (ChatCommand.slow.get(p.getServer().getInfo().getName()) * 1000));
					}
				}
				else if(ChatCommand.slow.containsKey("all")) {
					if(slowed.containsKey(p)) {
						if(slowed.get(p) > System.currentTimeMillis()) {
							e.setCancelled(true);
							int timeLeft= (int) ((slowed.get(p) - System.currentTimeMillis())/1000) + 1;
							p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cPlease wait another " + timeLeft
									+ " seconds! The whole network is currently in slow mode!")));
						}
						else {
							slowed.remove(p);
							slowed.put(p, System.currentTimeMillis() + (ChatCommand.slow.get("all") * 1000));
						}
					}
					else {
						slowed.put(p, System.currentTimeMillis() + (ChatCommand.slow.get("all") * 1000));
					}
				}
			}
		}
	}

}
