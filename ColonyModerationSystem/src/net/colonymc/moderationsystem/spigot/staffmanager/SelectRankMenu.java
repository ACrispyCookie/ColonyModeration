package net.colonymc.moderationsystem.spigot.staffmanager;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.colonymc.api.itemstacks.ItemStackBuilder;
import net.colonymc.moderationsystem.bungee.staffmanager.Rank;
import net.colonymc.moderationsystem.bungee.staffmanager.StaffAction;
import net.colonymc.moderationsystem.spigot.BungeecordConnector;
import net.colonymc.moderationsystem.spigot.Main;

public class SelectRankMenu implements Listener, InventoryHolder {

	Player p;
	Inventory inv;
	BStaffMember b;
	StaffAction action;
	HashMap<Integer, Rank> ranks = new HashMap<Integer, Rank>();
	
	public SelectRankMenu(Player p, BStaffMember b, StaffAction action) {
		this.p = p;
		this.b = b;
		this.action = action;
		this.inv = Bukkit.createInventory(this, 54, "Select a rank...");
		fillInventory();
		openInventory();
	}
	
	public SelectRankMenu() {
		
	}
	
	public void fillInventory() {
		int notShowing = p.getUniqueId().toString().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f") ? 0 : 1;
		if(action == StaffAction.PROMOTE) {
			int i = Rank.values().length - 1 - notShowing;
			for(Rank rank : Rank.values()) {
				if(rank == Rank.OWNER && notShowing == 1) {
					continue;
				}
				int slot = 11 + i + ((i/5) * 4);
				ItemStack item = null;
				if(rank.ordinal() > b.getRank().ordinal()) {
					item = new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability(rank.getColor()).name("&" + String.valueOf(rank.getChatColor()) + rank.getName() + " Rank").build();
					ranks.put(slot, rank);
				}
				else {
					item = new ItemStackBuilder(Material.BARRIER).name(" ").build();
				}
				inv.setItem(slot, item);
				i--;
			}
		}
		else {
			int i = Rank.values().length - 1 - notShowing;
			for(Rank rank : Rank.values()) {
				if(rank == Rank.OWNER && notShowing == 1) {
					continue;
				}
				int slot = 11 + i + ((i/5) * 4);
				ItemStack item = null;
				if(rank.ordinal() < b.getRank().ordinal()) {
					item = new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability(rank.getColor()).name("&" + String.valueOf(rank.getChatColor()) + rank.getName() + " Rank").build();
					ranks.put(slot, rank);
				}
				else {
					item = new ItemStackBuilder(Material.BARRIER).name(" ").build();
				}
				inv.setItem(slot, item);
				i--;
			}
		}
		inv.setItem(49, new ItemStackBuilder(Material.BARRIER).name("&cCancel action").build());
		
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
			if(e.getInventory().getHolder() instanceof SelectRankMenu) {
				SelectRankMenu m = (SelectRankMenu) e.getInventory().getHolder();
				Player p = m.p;
				e.setCancelled(true);
				if(e.getSlot() == 49) {
					p.closeInventory();
					new StaffManagerPlayerMenu(p, m.b);
				}
				else if(m.ranks.containsKey(e.getSlot())) {
					p.closeInventory();
					BungeecordConnector.actionOnStaff(p, m.b.getUuid(), m.action, m.ranks.get(e.getSlot()));
				}
			}
		}
	}
}
