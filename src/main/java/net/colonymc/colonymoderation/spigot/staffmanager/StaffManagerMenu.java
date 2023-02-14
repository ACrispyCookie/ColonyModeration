package net.colonymc.colonymoderation.spigot.staffmanager;

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

import net.colonymc.colonyspigotapi.api.itemstack.ItemStackBuilder;
import net.colonymc.colonymoderation.bungee.staffmanager.BStaffMember;
import net.colonymc.colonymoderation.spigot.Main;

public class StaffManagerMenu implements Listener, InventoryHolder {

	Player p;
	Inventory inv;
	
	public StaffManagerMenu(Player p) {
		this.p = p;
		this.inv = Bukkit.createInventory(this, p.hasPermission("colonymc.staffmanager") ? 45 : 27, "Staff Manager");
		fillInventory();
	}
	
	public StaffManagerMenu() {
		
	}
	
	public void fillInventory() {
		if(p.hasPermission("colonymc.staffmanager")) {
			inv.setItem(20, new ItemStackBuilder(Material.BOOK).name("&dAll staff members")
					.lore("\n&fSee a list of every staff member\n&fin the network and every\n&fstatistic about them!\n \n&dClick to open the menu!").build());
			inv.setItem(22, new ItemStackBuilder(Material.NETHER_STAR).name("&dTop staff suggestions")
					.lore("\n&fSee the best and the worst\n&fstaff members of the network!\n \n&fThe algorithm in order to pick\n&fuses: &dmonthly bans/mutes,\n&dmonthly reports closed, monthly playtime\n&dand player feedback&f!\n \n&dClick here to open the menu!").build());
			inv.setItem(24, new ItemStackBuilder(Material.SIGN).name("&dSearch a staff member")
					.lore("\n&fSearch a staff member,\n&fcheck his statistics and take\n&factions on them!\n \n&dClick here to enter a search query!").build());
		}
		else {
			inv.setItem(11, new ItemStackBuilder(Material.NETHER_STAR).name("&dMy statistics")
					.lore("\n&fSee your staff statistics from"
							+ "\n&fyour daily bans to your &dtotal"
							+ "\n&dplaytime &fand even &dfeedback"
							+ "\n&ffrom other players!"
							+ "\n "
							+ "\n&7(Note: Every 6 hours a new survey"
							+ "\n&7regarding one staff member,"
							+ "\n&7will be created and announced."
							+ "\n&7The most active staff member during"
							+ "\n&7those 6 hours is the one that will get"
							+ "\n&7picked.)"
							+ "\n "
							+ "\n&dClick here to open the menu!").build());
			inv.setItem(15, new ItemStackBuilder(Material.DIAMOND).name("&dTop staff members")
					.lore("\n&fSee the top staff member"
							+ "\n&fof the &dday, week and month"
							+ "\n&frated by their actions!"
							+ "\n "
							+ "\n&7(Note: The algorithm uses your"
							+ "\n&7ban count, playtime, user feedback"
							+ "\n&7and more, to decide and rate everyone"
							+ "\n&7fairly!)"
							+ "\n "
							+ "\n&dClick here to open the menu!").build());
		}
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
				if(p.hasPermission("colonymc.staffmanager")) {
					if(e.getSlot() == 20) {
						new AllStaffManagerMenu(p).openInventory();
					}
					else if(e.getSlot() == 22) {
						new TopStaffManagerMenu(p).openInventory();
					}
					else if(e.getSlot() == 24) {
						p.closeInventory();
						Main.getSignGui().open(p, new String[] {"", "^^^^^^^^^^^^^^^", "Enter the name", "of a player"}, (player, lines) -> {
							String msg = lines[0].replaceAll("\"", "");
							if(!msg.isEmpty()) {
								new SearchStaffMenu(p, msg).openInventory();
							}
							else {
								p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cPlease enter a search query!"));
							}
						});
					}
				}
				else {
					if(e.getSlot() == 11) {
						new StaffManagerPlayerMenu(p, BStaffMember.getByUuid(p.getUniqueId().toString()), e.getInventory().getHolder()).openInventory();
					}
					else if(e.getSlot() == 15){
						new TopStaffManagerMenu(p).openInventory();
					}
				}
			}
		}
	}

}
