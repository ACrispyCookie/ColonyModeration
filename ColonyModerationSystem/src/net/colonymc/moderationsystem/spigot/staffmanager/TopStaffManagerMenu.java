package net.colonymc.moderationsystem.spigot.staffmanager;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class TopStaffManagerMenu implements Listener, InventoryHolder {

	Inventory inv;
	Player p;
	
	public TopStaffManagerMenu(Player p) {
		this.p = p;
		
	}
	
	public TopStaffManagerMenu() {
		
	}
	
	@Override
	public Inventory getInventory() {
		return inv;
	}

}
