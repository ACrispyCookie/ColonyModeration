package net.colonymc.colonymoderationsystem.spigot.bans;

import java.sql.ResultSet;
import java.sql.SQLException;
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

import net.colonymc.colonyspigotapi.api.itemstack.ItemStackBuilder;
import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderationsystem.bungee.bans.PunishmentType;
import net.colonymc.colonymoderationsystem.spigot.BungeecordConnector;
import net.colonymc.colonymoderationsystem.spigot.Main;
import net.colonymc.colonymoderationsystem.spigot.reports.Report;

public class ChooseReasonMenu implements Listener, InventoryHolder {
	
	Player p;
	ArrayList<MenuPlayer> targets;
	PunishmentType type;
	MenuPlayer target;
	int reportId;
	boolean cancelled;
	BukkitTask cancel;
	BukkitTask checkIfExists;
	Inventory inv;
	
	public ChooseReasonMenu(Player p, MenuPlayer target, int reportId) {
		this.p = p;
		this.target = target;
		this.reportId = reportId;
		this.inv = Bukkit.createInventory(this, 45, "Punishing " + target.getName() + "...");
		fillInventory();
		openInventory();
		if(reportId != -1) {
			checkIfExists = new BukkitRunnable() {
				@Override
				public void run() {
					if(Report.getById(reportId) == null) {
						p.closeInventory();
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
				if(checkIfExists != null) {
					checkIfExists.cancel();
				}
				p.closeInventory();
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cProcess cancelled due to inactivity in the last 2 minutes!"));
			}
		}.runTaskLaterAsynchronously(Main.getInstance(), 2400);
	}
	
	public ChooseReasonMenu(Player p, ArrayList<MenuPlayer> targets, PunishmentType type) {
		this.p = p;
		this.targets = targets;
		this.type = type;
		if(targets.size() == 1) {
			this.inv = Bukkit.createInventory(this, 45, "Punishing " + targets.size() + " player...");
		}
		else {
			this.inv = Bukkit.createInventory(this, 45, "Punishing " + targets.size() + " players...");
		}
		fillInventory();
		openInventory();
		cancel = new BukkitRunnable() {
			@Override
			public void run() {
				p.closeInventory();
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cProcess cancelled due to inactivity in the last 2 minutes!"));
			}
		}.runTaskLaterAsynchronously(Main.getInstance(), 2400);
	}
	
	private void fillInventory() {
		if(p.hasPermission("mod.bans")) {
			inv.setItem(19, new ItemStackBuilder(Material.DIAMOND_SWORD)
					.name("&dCheating ban")
					.lore("\n&fBan someone who is using\n&fany kinds of &dcheats&f!")
					.build());
			inv.setItem(24, new ItemStackBuilder(Material.DIAMOND_SWORD)
					.name("&dBug abusing ban")
					.lore("\n&fBan someone who is\n&fis abusing &dbugs&f!")
					.build());
		}
		else {
			inv.setItem(19, new ItemStackBuilder(Material.BARRIER)
					.name("&cCheating Ban")
					.lore("\n&cYou must have the &b&l&k:&b&lModerator&k:&r &crank!\n&fBan someone who is using\n&fany kinds of &dcheats&f!")
					.build());
			inv.setItem(24, new ItemStackBuilder(Material.BARRIER)
					.name("&cBug abusing ban")
					.lore("\n&cYou must have the &b&l&k:&b&lModerator&k:&r &crank!\n&fBan someone who is\n&fis abusing &dbugs&f!")
					.build());
		}
		inv.setItem(13, new ItemStackBuilder(Material.SIGN)
				.name("&5&lInformation")
				.lore("\n&fAll the &dpunishments &fare automatic.\n \n&fWhen you select a\n&fpunishment the player will be"
						+ "\n&fmuted/banned depending on their\n&fprevious punishments.\n \n&fJust select the type of the punishment!")
				.build());
		inv.setItem(20, new ItemStackBuilder(Material.IRON_SWORD)
				.name("&dOffensive Mute/Ban")
				.lore("\n&fMute/Ban someone who is using\n&flanguage to &doffend a player&f!")
				.build());
		inv.setItem(21, new ItemStackBuilder(Material.IRON_SWORD)
				.name("&dNegative behaviour Mute/Ban")
				.lore("\n&fMute/Ban someone who is using\n&fbehaving &dnegatively&f!")
				.build());
		inv.setItem(22, new ItemStackBuilder(Material.IRON_SWORD)
				.name("&dAdvertising Mute/Ban")
				.lore("\n&fMute/Ban someone who is using\n&dadvertising servers/URLs &fin the chat!")
				.build());
		inv.setItem(23, new ItemStackBuilder(Material.IRON_SWORD)
				.name("&dSpamming Mute/Ban")
				.lore("\n&fMute/Ban someone who is\n&dspamming &fthe chat with messages!")
				.build());
		if(p.hasPermission("*")) {
			inv.setItem(25, new ItemStackBuilder(Material.NAME_TAG)
					.name("&dCustom Punishments")
					.lore("\n&fPunish someone who is\n&fbreaking another &drule&f!\n \n&4&lWARNING:\n&fOnly use this in special occasions")
					.build());
		}
		else {
			inv.setItem(25, new ItemStackBuilder(Material.BARRIER)
					.name("&cCustom Punishments")
					.lore("\n&cYou must have the &c&l&k:&c&lAdmin&k:&r rank!\n&fPunish someone who is\n&fbreaking another &drule&f!\n \n&4&lWARNING:\n&fOnly use this in special occasions")
					.build());
		}
		
	}

	public ChooseReasonMenu() {
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
		if(e.getInventory().getHolder() instanceof ChooseReasonMenu) {
			ChooseReasonMenu menu = (ChooseReasonMenu) e.getInventory().getHolder();
			if(e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
				menu.cancel.cancel();
				menu.cancel = new BukkitRunnable() {
					@Override
					public void run() {
						if(menu.checkIfExists != null) {
							menu.checkIfExists.cancel();
						}
						menu.p.closeInventory();
						menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cProcess cancelled due to inactivity in the last 2 minutes!"));
					}
				}.runTaskLaterAsynchronously(Main.getInstance(), 2400);
				e.setCancelled(true);
				Player p = menu.p;
				if(e.getSlot() == 19) {
					if(p.hasPermission("mod.bans")) {
						if(menu.target == null) {
							menu.p.closeInventory();
							new ChooseDurationMenu(menu.p, menu.targets, "Cheating - Hacking", menu.type);
						}
						else {
							p.playSound(p.getLocation(), Sound.LEVEL_UP, 2, 1);
							menu.p.closeInventory();
							BungeecordConnector.sendPunishment(menu.target.getUuid(), p, "Cheating - Hacking", menu.reportId);
						}
					}
					else {
						p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot use this type of punishments!"));
					}
				}
				else if(e.getSlot() == 20) {
					if(!MainDatabase.isMuted(menu.target.getName()) || isGettingBanned(menu.target.getName())) {
						if(menu.target == null) {
							menu.p.closeInventory();
							new ChooseDurationMenu(menu.p, menu.targets, "Offensive language", menu.type);
						}
						else {
							p.playSound(p.getLocation(), Sound.LEVEL_UP, 2, 1);
							menu.p.closeInventory();
							BungeecordConnector.sendPunishment(menu.target.getUuid(), p, "Offensive language", menu.reportId);
						}
					}
					else {
						p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is already muted!"));
					}
				}
				else if(e.getSlot() == 21) {
					if(!MainDatabase.isMuted(menu.target.getName()) || isGettingBanned(menu.target.getName())) {
						if(menu.target == null) {
							menu.p.closeInventory();
							new ChooseDurationMenu(menu.p, menu.targets, "Negative behaviour", menu.type);
						}
						else {
							p.playSound(p.getLocation(), Sound.LEVEL_UP, 2, 1);
							menu.p.closeInventory();
							BungeecordConnector.sendPunishment(menu.target.getUuid(), p, "Negative behaviour", menu.reportId);
						}
					}
					else {
						p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is already muted!"));
					}
				}
				else if(e.getSlot() == 22) {
					if(!MainDatabase.isMuted(menu.target.getName()) || isGettingBanned(menu.target.getName())) {
						if(menu.target == null) {
							menu.p.closeInventory();
							new ChooseDurationMenu(menu.p, menu.targets, "Advertising", menu.type);
						}
						else {
							p.playSound(p.getLocation(), Sound.LEVEL_UP, 2, 1);
							menu.p.closeInventory();
							BungeecordConnector.sendPunishment(menu.target.getUuid(), p, "Advertising", menu.reportId);
						}
					}
					else {
						p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is already muted!"));
					}
				}
				else if(e.getSlot() == 23) {
					if(!MainDatabase.isMuted(menu.target.getName()) || isGettingBanned(menu.target.getName())) {
						if(menu.target == null) {
							menu.p.closeInventory();
							new ChooseDurationMenu(menu.p, menu.targets, "Spamming", menu.type);
						}
						else {
							p.playSound(p.getLocation(), Sound.LEVEL_UP, 2, 1);
							menu.p.closeInventory();
							BungeecordConnector.sendPunishment(menu.target.getUuid(), p, "Spamming", menu.reportId);
						}
					}
					else {
						p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is already muted!"));
					}
				}
				else if(e.getSlot() == 24) {
					if(p.hasPermission("mod.bans")) {
						if(menu.target == null) {
							menu.p.closeInventory();
							new ChooseDurationMenu(menu.p, menu.targets, "Bug Abusing", menu.type);
						}
						else {
							p.playSound(p.getLocation(), Sound.LEVEL_UP, 2, 1);
							menu.p.closeInventory();
							BungeecordConnector.sendPunishment(menu.target.getUuid(), p, "Bug Abusing", menu.reportId);
						}
					}
					else {
						p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot use this type of punishments!"));
					}
				}
				else if(e.getSlot() == 25) {
					if(p.hasPermission("*")) {
						menu.cancelled = true;
						menu.p.closeInventory();
						Main.getSignGui().open(p, new String[] {"", "^^^^^^^^^^^^^^^", "Enter the reason", "of the punishment"}, (player, lines) -> {
							String msg = lines[0].replaceAll("\"", "");
							if(!msg.isEmpty()) {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou set the custom reason to &d[" + msg + "]&f!"));
								if(menu.target == null) {
									new ChooseDurationMenu(player, menu.targets, msg, menu.type);
								}
								else {
									new ChooseTypeMenu(player, menu.target, msg, menu.reportId);
								}
							}
							else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou didn't enter a reason!"));
								p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
							}
							menu.cancel.cancel();
							if(menu.checkIfExists != null) {
								menu.checkIfExists.cancel();
							}
						});
					}
					else {
						p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot use this type of punishments!"));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if(e.getInventory().getHolder() instanceof ChooseReasonMenu) {
			ChooseReasonMenu menu = (ChooseReasonMenu) e.getInventory().getHolder();
			if(!cancelled) {
				menu.cancel.cancel();
				if(menu.checkIfExists != null) {
					menu.checkIfExists.cancel();
				}
			}
		}
	}
	
	private boolean isGettingBanned(String name) {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM PlayerInfo WHERE name='" + name + "';");
		try {
			if(rs.next()) {
				if(rs.getInt("timesMuted") == 6) {
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}