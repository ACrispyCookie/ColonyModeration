package net.colonymc.colonymoderation.spigot.reports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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

import net.colonymc.colonyspigotapi.api.itemstack.ItemStackBuilder;
import net.colonymc.colonyspigotapi.api.itemstack.SkullItemBuilder;
import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderation.spigot.BungeecordConnector;
import net.colonymc.colonymoderation.spigot.Main;
import net.colonymc.colonymoderation.spigot.bans.ChooseReasonMenu;
import net.colonymc.colonymoderation.spigot.bans.MenuPlayer;

public class ProcessReport implements Listener, InventoryHolder {

	Inventory inv;
	Player p;
	Report r;
	BukkitTask update;
	BukkitTask checkIfExists;
	
	public ProcessReport(Report r, Player p) {
		this.r = r;
		this.p = p;
		this.inv = Bukkit.createInventory(this, 54, "Processing [Report #" + r.getId() + "]...");
		MenuPlayer.forceLoad();
		fillInventory();
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
				updateServers();
				inv.setItem(13, new SkullItemBuilder()
						.playerUuid(UUID.fromString(r.getReportedUuid()))
						.name("&fReport &d#" + r.getId())
						.lore("\n&fReported Player: &d" + r.getReportedName() + "\n&fReporter player: &d" + r.getReporterName() + "\n&fReason: &d" + r.getReason() + "\n\n&fStatus: " +
						((MenuPlayer.getByUuid(r.getReportedUuid())).isOnline() ? "&aOnline" : "&cOffline"))
						.build());
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 0, 1);
	}
	
	public ProcessReport() {
		
	}
	
	private void fillInventory() {
		inv.setItem(49, new ItemStackBuilder(Material.ARROW).name("&dBack").build());
		inv.setItem(29, new ItemStackBuilder(Material.STAINED_CLAY)
				.name("&aMark report as true")
				.lore("\n&fMark this report as true\n&fand punish the player!")
				.durability((short) 5)
				.build());
		if(hasValidPunishments()) {
			inv.setItem(31, new ItemStackBuilder(Material.STAINED_CLAY)
					.name("&6Mark report as already punished")
					.lore("\n&fMark this report as already punished\n&fon another report!")
					.durability((short) 4)
					.build());
		}
		inv.setItem(33, new ItemStackBuilder(Material.STAINED_CLAY)
				.name("&cMark report as false")
				.lore("\n&fMark this report as false\n&fand notify the reporter!")
				.durability((short) 14)
				.build());
	}
	
	public void updateServers() {
		BungeecordConnector.requestPlayers(p);
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
		if(e.getInventory().getHolder() instanceof ProcessReport) {
			ProcessReport menu = (ProcessReport) e.getInventory().getHolder();
			e.setCancelled(true);
			if(e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
				if(e.getSlot() == 29) {
					menu.p.closeInventory();
					new ChooseReasonMenu(menu.p, MenuPlayer.getByUuid(menu.r.getReportedUuid()), menu.r.getId());
				}
				else if(e.getSlot() == 31 && menu.hasValidPunishments()) {
					menu.p.closeInventory();
					new SelectExistingMenu(menu.r, menu.p);
				}
				else if(e.getSlot() == 33) {
					menu.p.closeInventory();
					BungeecordConnector.markFalse(menu.p, menu.r.id);
				}
				else if(e.getSlot() == 49) {
					menu.p.closeInventory();
					new ReportsMenu(menu.p);
				}
			}
		}
	}
	
	public boolean hasValidPunishments() {
		ResultSet bans = MainDatabase.getResultSet("SELECT * FROM ActiveBans WHERE uuid='" + r.getReportedUuid() + "';");
		ResultSet mutes = MainDatabase.getResultSet("SELECT * FROM ActiveMutes WHERE uuid='" + r.getReportedUuid() + "';");
		try {
			if(bans.next() || mutes.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if(e.getInventory().getHolder() instanceof ProcessReport) {
			ProcessReport menu = (ProcessReport) e.getInventory().getHolder();
			menu.update.cancel();
			menu.checkIfExists.cancel();
		}
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}
}
