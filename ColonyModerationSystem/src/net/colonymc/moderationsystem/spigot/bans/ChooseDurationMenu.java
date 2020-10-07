package net.colonymc.moderationsystem.spigot.bans;

import java.util.ArrayList;

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

import net.colonymc.api.itemstacks.ItemStackBuilder;
import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.colonyapi.Time;
import net.colonymc.moderationsystem.bungee.bans.PunishmentType;
import net.colonymc.moderationsystem.spigot.BungeecordConnector;
import net.colonymc.moderationsystem.spigot.Main;
import net.colonymc.moderationsystem.spigot.reports.Report;

public class ChooseDurationMenu implements Listener, InventoryHolder {
	
	Player p;
	ArrayList<String> targetNames;
	String targetName;
	String reason;
	PunishmentType type;
	Inventory inv;
	int reportId;
	BukkitTask cancel;
	BukkitTask checkIfExists;
	int seconds = 0;
	
	public ChooseDurationMenu(Player p, ArrayList<String> targetNames, String reason, PunishmentType type) {
		this.p = p;
		this.targetNames = targetNames;
		this.reason = reason;
		this.type = type;
		this.inv = Bukkit.createInventory(this, 27, "Select a duration...");
		fillInventory();
		openInventory();
		cancel = new BukkitRunnable() {
			@Override
			public void run() {
				if(checkIfExists != null) {
					checkIfExists.cancel();
				}
				p.closeInventory();
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cProcess cancelled due to inactivity in the last 2 minutes!"));
			}
		}.runTaskLaterAsynchronously(Main.getInstance(), 2400);
	}
	
	public ChooseDurationMenu(Player p, String targetName, String reason, PunishmentType type, int reportId) {
		this.p = p;
		this.targetName = targetName;
		this.reason = reason;
		this.type = type;
		this.reportId = reportId;
		this.inv = Bukkit.createInventory(this, 27, "Select a duration...");
		fillInventory();
		openInventory();
		if(reportId != -1) {
			checkIfExists = new BukkitRunnable() {
				@Override
				public void run() {
					if(Report.getById(reportId) == null) {
						if(p.getOpenInventory().getTopInventory().equals(inv)) {
							p.closeInventory();
						}
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe report has been processed by another staff!"));
						p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
						cancel();
					}
				}
			}.runTaskTimerAsynchronously(Main.getInstance(), 0, 1);
		}
		cancel = new BukkitRunnable() {
			@Override
			public void run() {
				p.closeInventory();
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cProcess cancelled due to inactivity in the last 2 minutes!"));
			}
		}.runTaskLaterAsynchronously(Main.getInstance(), 2400);
	}

	public ChooseDurationMenu() {
	}
	
	private void fillInventory() {
		inv.setItem(1, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&a+1 second").durability((short) 5).build());
		inv.setItem(2, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&a+1 minute").durability((short) 5).build());
		inv.setItem(3, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&a+1 hour").durability((short) 5).build());
		inv.setItem(4, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&a+1 day").durability((short) 5).build());
		inv.setItem(5, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&a+1 week").durability((short) 5).build());
		inv.setItem(6, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&a+1 month").durability((short) 5).build());
		inv.setItem(7, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&a+1 year").durability((short) 5).build());
		inv.setItem(19, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&c-1 second").durability((short) 14).build());
		inv.setItem(20, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&c-1 minute").durability((short) 14).build());
		inv.setItem(21, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&c-1 hour").durability((short) 14).build());
		inv.setItem(22, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&c-1 day").durability((short) 14).build());
		inv.setItem(23, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&c-1 week").durability((short) 14).build());
		inv.setItem(24, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&c-1 month").durability((short) 14).build());
		inv.setItem(25, new ItemStackBuilder(Material.STAINED_GLASS_PANE).name("&c-1 year").durability((short) 14).build());
		inv.setItem(13, new ItemStackBuilder(Material.WATCH)
				.name("&fDuration: &d" + Time.formatted((long) seconds * 1000))
				.lore("\n&fClick the buttons above and below to\n&fchange the duration of the punishment!\n\n&dClick here &fto confirm the duration and execute\n&fthe punishment!")
				.build());
		if(targetName == null) {
			inv.setItem(12, new ItemStackBuilder(Material.NETHER_STAR).name("&dRecommended duration for each player").lore("\n&fClick this if you want the duration\n&ffor each player to be calculated seperately!").build());
		}
		else {
			inv.setItem(12, new ItemStackBuilder(Material.NETHER_STAR)
					.name("&dRecommended duration for each player")
					.lore("\n&fClick this if you want the duration\n&ffor the punishment to be calculated automatically!\n \n&fRecommended Duration: &d" + Time.formatted(getRecommendedDuration()))
					.build());
		}
	}
	
	public void openInventory() {
		new BukkitRunnable() {
			@Override
			public void run() {
				p.openInventory(inv);
			}
		}.runTaskLaterAsynchronously(Main.getInstance(), 1);
	}
	
	public Inventory getInventory() {
		return inv;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getInventory().getHolder() instanceof ChooseDurationMenu) {
			ChooseDurationMenu menu = (ChooseDurationMenu) e.getInventory().getHolder();
			if(e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
				menu.cancel.cancel();
				menu.cancel = new BukkitRunnable() {
					@Override
					public void run() {
						menu.p.closeInventory();
						menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cProcess cancelled due to inactivity in the last 2 minutes!"));
					}
				}.runTaskLaterAsynchronously(Main.getInstance(), 2400);
				e.setCancelled(true);
				Player p = menu.p;
				int slot = e.getSlot();
				if(slot == 19) {
					if(menu.seconds - 1 >= 0) {
						menu.seconds--;
					}
					else {
						menu.seconds = 0;
					}
				}
				else if(slot == 20) {
					if(menu.seconds - 60 >= 0) {
						menu.seconds = menu.seconds - 60;
					}
					else {
						menu.seconds = 0;
					}
				}
				else if(slot == 21) {
					if(menu.seconds - 3600 >= 0) {
						menu.seconds = menu.seconds - 3600;
					}
					else {
						menu.seconds = 0;
					}
				}
				else if(slot == 22) {
					if(menu.seconds - 86400 >= 0) {
						menu.seconds = menu.seconds - 86400;
					}
					else {
						menu.seconds = 0;
					}
				}
				else if(slot == 23) {
					if(menu.seconds - 604800 >= 0) {
						menu.seconds = menu.seconds - 604800;
					}
					else {
						menu.seconds = 0;
					}
				}
				else if(slot == 24) {
					if(menu.seconds - 2592000 >= 0) {
						menu.seconds = menu.seconds - 2592000;
					}
					else {
						menu.seconds = 0;
					}
				}
				else if(slot == 25) {
					if(menu.seconds - 31536000 >= 0) {
						menu.seconds = menu.seconds - 31536000;
					}
					else {
						menu.seconds = 0;
					}
				}
				else if(slot == 1) {
					if(menu.seconds + 1 <= 63072000) {
						menu.seconds++;
					}
					else if(menu.seconds == 63072000) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe maximum duration for a punishment is 2 years!"));
					}
					else {
						menu.seconds = 63072000;
					}
				}
				else if(slot == 2) {
					if(menu.seconds + 60 <= 63072000) {
						menu.seconds = menu.seconds + 60;
					}
					else if(menu.seconds == 63072000) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe maximum duration for a punishment is 2 years!"));
					}
					else {
						menu.seconds = 63072000;
					}
				}
				else if(slot == 3) {
					if(menu.seconds + 3600 <= 63072000) {
						menu.seconds = menu.seconds + 3600;
					}
					else if(menu.seconds == 63072000) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe maximum duration for a punishment is 2 years!"));
					}
					else {
						menu.seconds = 63072000;
					}
				}
				else if(slot == 4) {
					if(menu.seconds + 86400 <= 63072000) {
						menu.seconds = menu.seconds + 86400;
					}
					else if(menu.seconds == 63072000) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe maximum duration for a punishment is 2 years!"));
					}
					else {
						menu.seconds = 63072000;
					}
				}
				else if(slot == 5) {
					if(menu.seconds + 604800 <= 63072000) {
						menu.seconds = menu.seconds + 604800;
					}
					else if(menu.seconds == 63072000) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe maximum duration for a punishment is 2 years!"));
					}
					else {
						menu.seconds = 63072000;
					}
				}
				else if(slot == 6) {
					if(menu.seconds + 2592000 <= 63072000) {
						menu.seconds = menu.seconds + 2592000;
					}
					else if(menu.seconds == 63072000) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe maximum duration for a punishment is 2 years!"));
					}
					else {
						menu.seconds = 63072000;
					}
				}
				else if(slot == 7) {
					if(menu.seconds + 31536000 <= 63072000) {
						menu.seconds = menu.seconds + 31536000;
					}
					else if(menu.seconds == 63072000) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe maximum duration for a punishment is 2 years!"));
					}
					else {
						menu.seconds = 63072000;
					}
				}
				else if(e.getSlot() == 13) {
					if(menu.seconds > 0) {
						p.playSound(p.getLocation(), Sound.LEVEL_UP, 2, 1);
						p.closeInventory();
						if(menu.targetName == null) {
							BungeecordConnector.sendPunishment(menu.targetNames, p, menu.reason, (long) menu.seconds * 1000, menu.type);
						}
						else {
							BungeecordConnector.sendPunishment(menu.targetName, p, menu.reason, (long) menu.seconds * 1000, menu.type, menu.reportId);
						}
					}
					else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cPlease choose a duration bigger than 0 seconds!"));
					}
				}
				else if(e.getSlot() == 12) {
					if(menu.targetName == null) {
						p.playSound(p.getLocation(), Sound.LEVEL_UP, 2, 1);
						p.closeInventory();
						BungeecordConnector.sendPunishment(menu.targetNames, p, menu.reason, -1, menu.type);
					}
					else {
						p.playSound(p.getLocation(), Sound.LEVEL_UP, 2, 1);
						p.closeInventory();
						BungeecordConnector.sendPunishment(menu.targetName, p, menu.reason, menu.getRecommendedDuration(), menu.type, menu.reportId);
					}
				}
				menu.inv.setItem(13, new ItemStackBuilder(Material.WATCH)
						.name("&fDuration: &d" + Time.formatted((long) menu.seconds * 1000))
						.lore("\n&fClick the buttons above and below to\n&fchange the duration of the punishment!\n\n&dClick here &fto confirm the duration and execute\n&fthe punishment!")
						.build());
			}
		}
	}
	
	private long getRecommendedDuration() {
		if(type == PunishmentType.BAN) {
			int timesBanned = MainDatabase.getTimesBanned(targetName);
			switch(timesBanned) {
			case 0:
				return 1209600000L;
			case 1:
				return 2592000000L;
			case 2:
				return 5184000000L;
			case 3:
				return -1;
			}
		}
		else if(type == PunishmentType.MUTE) {
			int timesMuted = MainDatabase.getTimesMuted(targetName);
			switch(timesMuted) {
			case 0:
				return 3600000L;
			case 1:
				return 21600000L;
			case 2:
				return 86400000L;
			case 3:
				return 604800000L;
			case 4:
				return 1209600000L;
			case 5:
				return 2592000000L;
			}
		}
		return 0;
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if(e.getInventory().getHolder() instanceof ChooseDurationMenu) {
			ChooseDurationMenu menu = (ChooseDurationMenu) e.getInventory().getHolder();
			menu.cancel.cancel();
			if(menu.checkIfExists != null) {
				menu.checkIfExists.cancel();
			}
		}
	}
}
