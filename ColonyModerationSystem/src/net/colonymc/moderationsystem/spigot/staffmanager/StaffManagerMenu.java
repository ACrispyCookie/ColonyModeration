package net.colonymc.moderationsystem.spigot.staffmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import net.colonymc.api.itemstacks.ItemStackBuilder;
import net.colonymc.moderationsystem.spigot.Main;
import net.colonymc.moderationsystem.spigot.bans.SignGUI.SignGUIListener;

public class StaffManagerMenu implements Listener, InventoryHolder {

	Player p;
	Inventory inv;
	
	public StaffManagerMenu(Player p) {
		this.p = p;
		this.inv = Bukkit.createInventory(this, 45, "Staff Manager");
		fillInventory();
		openInventory();
	}
	
	public StaffManagerMenu() {
		
	}
	
	public void fillInventory() {
		inv.setItem(20, new ItemStackBuilder(Material.BOOK).name("&dAll staff members")
				.lore("\n&fSee a list of every staff member\n&fin the network and every\n&fstatistic about them!\n \n&dClick to open the menu!").build());
		inv.setItem(22, new ItemStackBuilder(Material.NETHER_STAR).name("&dMonthly staff suggestions")
				.lore("\n&fSee the best and the worst\n&fstaff members of the month!\n \n&fThe algorithm in order to pick\n&fuses: &dmonthly bans/mutes,\n&dmonthly reports closed, monthly playtime\n&dand player feedback&f!\n \n&dClick here to open the menu!").build());
		inv.setItem(24, new ItemStackBuilder(Material.SIGN).name("&dSearch a staff member")
				.lore("\n&fSearch a staff member,\n&fcheck his statistics and take\n&factions on him!\n \n&dClick here to enter a search query!").build());
	}
	
	public void openInventory() {
		new BukkitRunnable() {
			@Override
			public void run() {
				p.openInventory(inv);
			}
		}.runTaskLater(Main.getInstance(), 1);
	}
	
	@Override
	public Inventory getInventory() {
		return inv;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getInventory() != null && e.getInventory().getType() != InventoryType.PLAYER) {
			if(e.getInventory().getHolder() instanceof StaffManagerMenu) {
				e.setCancelled(true);
				Player p = (Player) e.getWhoClicked();
				if(e.getSlot() == 20) {
					new AllStaffManagerMenu(p);
				}
				else if(e.getSlot() == 22) {
					
				}
				else if(e.getSlot() == 24) {
					p.closeInventory();
					Main.getSignGui().open(p, new String[] {"", "^^^^^^^^^^^^^^^", "Enter the name", "of a player"}, new SignGUIListener() {
						@Override
						public void onSignDone(Player player, String[] lines) {
							String msg = lines[0].replaceAll("\"", "");
							if(!msg.isEmpty()) {
								new SearchStaffMenu(p, msg);
							}
							else {
								p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cPlease enter a search query!"));
							}
						}
					});
				}
			}
		}
	}

}
