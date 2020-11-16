package net.colonymc.colonymoderationsystem.spigot.reports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.colonymc.colonyspigotapi.itemstacks.ItemStackBuilder;
import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderationsystem.spigot.BungeecordConnector;
import net.colonymc.colonymoderationsystem.spigot.Main;

public class SelectExistingMenu implements Listener,InventoryHolder {

	Inventory inv;
	Player p;
	Report r;
	BukkitTask update;
	BukkitTask checkIfExists;
	final HashMap<Integer, String> slots = new HashMap<>();
	
	public SelectExistingMenu(Report r, Player p) {
		this.r = r;
		this.p = p;
		this.inv = Bukkit.createInventory(this, 36, "Select a valid punishment...");
		fillInventory();
		inv.setItem(31, new ItemStackBuilder(Material.ARROW).name("&dBack").build());
		openInventory();
		checkIfExists = new BukkitRunnable() {
			@Override
			public void run() {
				if(Report.getById(r.getId()) == null) {
					if(p.getOpenInventory().getTopInventory().equals(inv)) {
						p.closeInventory();
					}
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe report has been processed by another staff!"));
					p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
					cancel();
				}
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 0, 1);
		update = new BukkitRunnable() {
			@Override
			public void run() {
				fillInventory();
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 0, 10);
	}
	
	public SelectExistingMenu() {
		
	}
	
	private void fillInventory() {
		try {
			int i = 0;
			ResultSet bans = MainDatabase.getResultSet("SELECT * FROM ActiveBans WHERE uuid='" + r.getReportedUuid() + "';");
			while(bans.next()) {
				inv.setItem(i, new ItemStackBuilder(Material.DIAMOND_SWORD)
						.name("&fBan for &d" + bans.getString("reason"))
						.lore("\n&fBanned by: &d" + MainDatabase.getName(bans.getString("staffUuid")) + "\n&fBanned at: &d" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(bans.getLong("issuedAt")))
						+ "\n&fBanned until: &d" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(bans.getLong("bannedUntil")))
						+ "\n&fBan ID: &d#" + bans.getString("ID"))
						.build());
				slots.put(i, bans.getString("ID"));
				i++;
			}
			ResultSet mutes = MainDatabase.getResultSet("SELECT * FROM ActiveMutes WHERE uuid='" + r.getReportedUuid() + "';");
			while(mutes.next()) {
				inv.setItem(i, new ItemStackBuilder(Material.IRON_SWORD)
						.name("&fMute for &d" + mutes.getString("reason"))
						.lore("\n&fMuted by: &d" + MainDatabase.getName(mutes.getString("staffUuid")) + "\n&fMuted at: &d" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(mutes.getLong("issuedAt")))
						+ "\n&fMuted until: &d" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(mutes.getLong("mutedUntil")))
						+ "\n&fMute ID: &d#" + mutes.getString("ID"))
						.build());
				slots.put(i, mutes.getString("ID"));
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getInventory().getHolder() instanceof SelectExistingMenu) {
			SelectExistingMenu menu = (SelectExistingMenu) e.getInventory().getHolder();
			e.setCancelled(true);
			if(e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
				if(menu.slots.containsKey(e.getSlot())) {
					menu.p.closeInventory();
					BungeecordConnector.markAlready(menu.p, menu.r.id, menu.slots.get(e.getSlot()));
				}
				else if(e.getSlot() == 31) {
					menu.p.closeInventory();
					new ProcessReport(menu.r, menu.p);
				}
			}
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if(e.getInventory().getHolder() instanceof SelectExistingMenu) {
			SelectExistingMenu menu = (SelectExistingMenu) e.getInventory().getHolder();
			menu.update.cancel();
			menu.checkIfExists.cancel();
		}
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}
}
