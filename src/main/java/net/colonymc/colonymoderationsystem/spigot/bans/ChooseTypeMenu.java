package net.colonymc.colonymoderationsystem.spigot.bans;

import org.bukkit.Bukkit;
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
import net.colonymc.colonymoderationsystem.bungee.bans.PunishmentType;
import net.colonymc.colonymoderationsystem.spigot.BungeecordConnector;
import net.colonymc.colonymoderationsystem.spigot.Main;
import net.colonymc.colonymoderationsystem.spigot.reports.Report;
import net.md_5.bungee.api.ChatColor;

public class ChooseTypeMenu implements Listener, InventoryHolder {
	
	Player p;
	MenuPlayer target;
	String reason;
	int reportId;
	Inventory inv;
	BukkitTask cancel;
	BukkitTask checkIfExists;
	
	public ChooseTypeMenu(Player p) {
		this.p = p;
		this.inv = Bukkit.createInventory(this, 27, "Select a punishment...");
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
	
	public ChooseTypeMenu(Player p, MenuPlayer target, String reason, int reportId) {
		this.p = p;
		this.target = target;
		this.reason = reason;
		this.reportId = reportId;
		this.inv = Bukkit.createInventory(this, 27, "Select a punishment...");
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
	
	public ChooseTypeMenu() {
	}
	
	private void fillInventory() {
		if(target != null) {
			inv.setItem(11, new ItemStackBuilder(Material.DIAMOND_SWORD)
					.name("&dBan player")
					.lore("\n&fChoose this type if you want\n&fto &dban &fthe player from the network!")
					.build());
			inv.setItem(13, new ItemStackBuilder(Material.NETHER_STAR)
					.name("&dRecommended punishment")
					.lore("\n&fChoose this if you want\n&fto punish the player\n&fwith the &drecommended &ftype of punishment!\n \n&fRecommended punishment: &d" + getRecommended().name)
					.build());
			inv.setItem(15, new ItemStackBuilder(Material.IRON_SWORD)
					.name("&dMute player")
					.lore("\n&fChoose this type if you want\n&fto &dmute &fthe player on the network!")
					.build());
		}
		else {
			inv.setItem(11, new ItemStackBuilder(Material.DIAMOND_SWORD)
					.name("&dBan players")
					.lore("\n&fChoose this type if you want\n&fto &dban &fthe players from the network!")
					.build());
			inv.setItem(13, new ItemStackBuilder(Material.NETHER_STAR)
					.name("&dUse recommended punishment")
					.lore("\n&fChoose this if you want\n&fto punish the players\n&fwith their &drecommended &ftype of punishment!")
					.build());
			inv.setItem(15, new ItemStackBuilder(Material.IRON_SWORD)
					.name("&dMute players")
					.lore("\n&fChoose this type if you want\n&fto &dmute &fthe players on the network!")
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
		if(e.getInventory().getHolder() instanceof ChooseTypeMenu) {
			e.setCancelled(true);
			ChooseTypeMenu menu = (ChooseTypeMenu) e.getInventory().getHolder();
			if(e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
				menu.cancel.cancel();
				menu.cancel = new BukkitRunnable() {
					@Override
					public void run() {
						menu.p.closeInventory();
						menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cProcess cancelled due to inactivity in the last 2 minutes!"));
					}
				}.runTaskLaterAsynchronously(Main.getInstance(), 2400);
				if(e.getSlot() == 11) {
					menu.p.closeInventory();
					if(menu.target == null) {
						new ChoosePlayerMenu(menu.p, PunishmentType.BAN, true);
					}
					else if(MainDatabase.getTimesMuted(menu.target.getName()) < 3){
						new ChooseDurationMenu(menu.p, menu.target, menu.reason, PunishmentType.BAN, menu.reportId);
					}
					else {
						menu.p.playSound(menu.p.getLocation(), Sound.LEVEL_UP, 2, 1);
						menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe player has been permanently banned due to their bad history!"));
						menu.p.closeInventory();
						BungeecordConnector.sendPunishment(menu.target.getUuid(), menu.p, menu.reason, menu.reportId);
					}
				}
				else if(e.getSlot() == 13) {
					menu.p.closeInventory();
					if(menu.target == null) {
						new ChoosePlayerMenu(menu.p, null, true);
					}
					else if(MainDatabase.getTimesMuted(menu.target.getName()) < 3){
						new ChooseDurationMenu(menu.p, menu.target, menu.reason, menu.getRecommended(), menu.reportId);
					}
					else {
						menu.p.playSound(menu.p.getLocation(), Sound.LEVEL_UP, 2, 1);
						menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe player has been permanently banned due to their bad history!"));
						menu.p.closeInventory();
						BungeecordConnector.sendPunishment(menu.target.getUuid(), menu.p, menu.reason, menu.reportId);
					}
				}
				else if(e.getSlot() == 15) {
					menu.p.closeInventory();
					if(menu.target == null) {
						new ChoosePlayerMenu(menu.p, PunishmentType.MUTE, true);
					}
					else {
						if(!MainDatabase.isMuted(menu.target.getName())) {
							if(MainDatabase.getTimesMuted(menu.target.getName()) < 3){
								new ChooseDurationMenu(menu.p, menu.target, menu.reason, PunishmentType.MUTE, menu.reportId);
							}
							else {
								menu.p.playSound(menu.p.getLocation(), Sound.LEVEL_UP, 2, 1);
								menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe player has been permanently banned due to their bad history!"));
								menu.p.closeInventory();
								BungeecordConnector.sendPunishment(menu.target.getUuid(), menu.p, menu.reason, menu.reportId);
							}
						}
						else {
							menu.p.playSound(menu.p.getLocation(), Sound.NOTE_BASS, 2, 1);
							menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is already muted!"));
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if(e.getInventory().getHolder() instanceof ChooseTypeMenu) {
			ChooseTypeMenu menu = (ChooseTypeMenu) e.getInventory().getHolder();
			menu.cancel.cancel();
			if(menu.checkIfExists != null) {
				menu.checkIfExists.cancel();
			}
		}
	}
	
	public PunishmentType getRecommended() {
		int times = MainDatabase.getTimesMuted(target.getName());
		if(times == 6) {
			return PunishmentType.BAN;
		}
		else if(MainDatabase.isMuted(target.getName())) {
			return PunishmentType.BAN;
		}
		return PunishmentType.MUTE;
	}
	
}
