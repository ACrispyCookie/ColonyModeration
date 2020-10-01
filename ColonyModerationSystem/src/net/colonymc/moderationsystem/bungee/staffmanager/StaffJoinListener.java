package net.colonymc.moderationsystem.bungee.staffmanager;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class StaffJoinListener implements Listener {

	@EventHandler
	public void onJoin(PostLoginEvent e) {
		ProxiedPlayer p = e.getPlayer();
		if(p.hasPermission("staff.store")) {
			if(StaffMember.getByUuid(p.getUniqueId().toString()) != null) {
				StaffMember.getByUuid(p.getUniqueId().toString()).login();
			}
			else {
				new StaffMember(p.getUniqueId().toString(), true);
			}
		}
	}
	
	@EventHandler
	public void onLeaver(PlayerDisconnectEvent e) {
		ProxiedPlayer p = e.getPlayer();
		if(p.hasPermission("staff.store")) {
			StaffMember.getByUuid(p.getUniqueId().toString()).logout();
		}
	}
	
}
