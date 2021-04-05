package net.colonymc.colonymoderationsystem.spigot.staffmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.colonymc.colonyspigotapi.api.itemstack.ItemStackBuilder;
import net.colonymc.colonyspigotapi.api.itemstack.SkullItemBuilder;
import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonyapi.Time;
import net.colonymc.colonymoderationsystem.bungee.staffmanager.BStaffMember;
import net.colonymc.colonymoderationsystem.spigot.Main;

public class TopStaffManagerMenu implements Listener, InventoryHolder {

	final int ONE_DAY = 86400000;
	final int TWO_DAYS = 172800000;
	final int ONE_WEEK = 604800000;
	final int TWO_WEEKS = 1209600000;
	Inventory inv;
	Player p;
	long nextD;
	long nextW;
	long nextM;
	int timeLeft;
	BukkitTask update;
	
	
	public TopStaffManagerMenu(Player p) {
		this.p = p;
		this.inv = Bukkit.createInventory(this, p.hasPermission("colonymc.staffmanager") ? 54 : 36, "Best" + (p.hasPermission("colonymc.staffmanager") ? "/Worst" : "") + " staff members");
		setup();
		update = new BukkitRunnable() {
			@Override
			public void run() {
				fillInventory();
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 0, 20);
	}
	
	public TopStaffManagerMenu() {
		
	}
	
	private void fillInventory() {
		if(System.currentTimeMillis() >= nextD || System.currentTimeMillis() >= nextW || System.currentTimeMillis() >= nextM) {
			setup();
		}
		if(p.hasPermission("colonymc.staffmanager")) {
			inv.setItem(49, new ItemStackBuilder(Material.ARROW).name("&dGo back").build());
			inv.setItem(28, new ItemStackBuilder(Material.COAL).name("&cWorst staff members").build());
			inv.setItem(34, new ItemStackBuilder(Material.COAL).name("&cWorst staff members").build());
			inv.setItem(29, getWDaily());
			inv.setItem(31, getWWeekly());
			inv.setItem(33, getWMonthly());
		}
		else {
			inv.setItem(31, new ItemStackBuilder(Material.ARROW).name("&dGo back").build());
		}
		inv.setItem(10, new ItemStackBuilder(Material.DIAMOND).name("&dBest staff members").build());
		inv.setItem(16, new ItemStackBuilder(Material.DIAMOND).name("&dBest staff members").build());
		inv.setItem(11, getDaily());
		inv.setItem(13, getWeekly());
		inv.setItem(15, getMonthly());
		p.updateInventory();
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
	public void onClick(InventoryCloseEvent e) {
		if(e.getInventory().getHolder() instanceof TopStaffManagerMenu) {
			TopStaffManagerMenu menu = (TopStaffManagerMenu) e.getInventory().getHolder();
			menu.update.cancel();
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
			if(e.getInventory().getHolder() instanceof TopStaffManagerMenu) {
				e.setCancelled(true);
				TopStaffManagerMenu menu = (TopStaffManagerMenu) e.getInventory().getHolder();
				Player p = menu.p;
				if(p.hasPermission("colonymc.staffmanager")) {
					if(e.getSlot() == 49) {
						new StaffManagerMenu(p).openInventory();
					}
					else if(e.getSlot() == 11) {
						if(menu.nextD - System.currentTimeMillis() > 7200000 & BStaffMember.getSMOfDay() != null) {
							new StaffManagerPlayerMenu(p, BStaffMember.getSMOfDay(), menu).openInventory();
						}
					}
					else if(e.getSlot() == 13) {
						if(menu.nextW - System.currentTimeMillis() > 7200000 & BStaffMember.getSMOfWeek() != null) {
							new StaffManagerPlayerMenu(p, BStaffMember.getSMOfWeek(), menu).openInventory();
						}
					}
					else if(e.getSlot() == 15) {
						if(menu.nextM - System.currentTimeMillis() > 7200000 & BStaffMember.getSMOfMonth() != null) {
							new StaffManagerPlayerMenu(p, BStaffMember.getSMOfMonth(), menu).openInventory();
						}
					}
					else if(e.getSlot() == 29) {
						if(menu.nextD - System.currentTimeMillis() > 7200000 & BStaffMember.getWSMOfDay() != null) {
							new StaffManagerPlayerMenu(p, BStaffMember.getWSMOfDay(), menu).openInventory();
						}
					}
					else if(e.getSlot() == 31) {
						if(menu.nextW - System.currentTimeMillis() > 7200000 & BStaffMember.getWSMOfWeek() != null) {
							new StaffManagerPlayerMenu(p, BStaffMember.getWSMOfWeek(), menu).openInventory();
						}
					}
					else if(e.getSlot() == 33) {
						if(menu.nextM - System.currentTimeMillis() > 7200000 & BStaffMember.getWSMOfMonth() != null) {
							new StaffManagerPlayerMenu(p, BStaffMember.getWSMOfMonth(), menu).openInventory();
						}
					}
				}
				else {
					if(e.getSlot() == 31) {
						new StaffManagerMenu(p).openInventory();
					}
				}
			}
		}
	}
	
	private void setup() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM NextTopStaff WHERE id=0;");
		try {
			if(rs.next()) {
				nextD = rs.getLong("nextDay");
				nextW = rs.getLong("nextWeek");
				nextM = rs.getLong("nextMonth");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private ItemStack getDaily() {
		if(nextD - System.currentTimeMillis() < 7200000) {
			return new SkullItemBuilder()
					.url("http://textures.minecraft.net/texture/fe5653187521d752a91b163a62d587d2bce65c869ca996825271f6b4539cf2f")
					.name("&cStaff member of the day")
					.lore("\n&fThe new staff member of"
							+ "\n&fthe &dday &fhas not been"
							+ "\n&fannounced yet. Please wait"
							+ "\n&fanother &d" + Time.formatted((nextD - System.currentTimeMillis())/1000)
							+ "\n "
							+ "\n&cWaiting for a new staff member...")
					.build();
		}
		else {
			if(BStaffMember.getSMOfDay() != null) {
				return new SkullItemBuilder()
						.playerUuid(UUID.fromString(BStaffMember.getSMOfDay().getUuid()))
						.name("&dStaff member of the day")
						.lore("\n&fThe new staff member of"
								+ "\n&fthe &dday &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &d&l&kO&r &d&l" + MainDatabase.getName(BStaffMember.getSMOfDay().getUuid()) + " &kO&r &f- &d" + BStaffMember.getSMOfDay().calculateBetween(nextD - TWO_DAYS, nextD - ONE_DAY) + " rating"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextD - System.currentTimeMillis())/1000)
								+ "\n "
								+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
						.build();
			}
			else {
				return new SkullItemBuilder()
						.url("http://textures.minecraft.net/texture/3ed1aba73f639f4bc42bd48196c715197be2712c3b962c97ebf9e9ed8efa025")
						.name("&cStaff member of the day")
						.lore("\n&fThe new staff member of"
								+ "\n&fthe &cday &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &c&l&kO&r &cNo one was picked... &kO&r"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextD - System.currentTimeMillis())/1000)
								+ "\n ")
						.build();
			}
		}
	}
	
	private ItemStack getWeekly() {
		if(nextW - System.currentTimeMillis() < 7200000) {
			return new SkullItemBuilder()
					.url("http://textures.minecraft.net/texture/fe5653187521d752a91b163a62d587d2bce65c869ca996825271f6b4539cf2f")
					.name("&cStaff member of the week")
					.lore("\n&fThe new staff member of"
							+ "\n&fthe &dweek &fhas not been"
							+ "\n&fannounced yet. Please wait"
							+ "\n&fanother &d" + Time.formatted((nextW - System.currentTimeMillis())/1000)
							+ "\n "
							+ "\n&cWaiting for a new staff member...")
					.build();
		}
		else {
			if(BStaffMember.getSMOfWeek() != null) {
				return new SkullItemBuilder()
						.playerUuid(UUID.fromString(BStaffMember.getSMOfWeek().getUuid()))
						.name("&dStaff member of the week")
						.lore("\n&fThe new staff member of"
								+ "\n&fthe &dweek &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &d&l&kO&r &d&l" + MainDatabase.getName(BStaffMember.getSMOfWeek().getUuid()) + " &kO&r &f- &d" + BStaffMember.getSMOfWeek().calculateBetween(nextW - TWO_WEEKS, nextW - ONE_WEEK) + " rating"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextW - System.currentTimeMillis())/1000)
								+ "\n "
								+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
						.build();
			}
			else {
				return new SkullItemBuilder()
						.url("http://textures.minecraft.net/texture/3ed1aba73f639f4bc42bd48196c715197be2712c3b962c97ebf9e9ed8efa025")
						.name("&cStaff member of the week")
						.lore("\n&fThe new staff member of"
								+ "\n&fthe &cweek &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &c&l&kO&r &cNo one was picked... &kO&r"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextW - System.currentTimeMillis())/1000)
								+ "\n ")
						.build();
			}
		}
	}
	
	private ItemStack getMonthly() {
		if(nextM - System.currentTimeMillis() < 7200000) {
			return new SkullItemBuilder()
					.url("http://textures.minecraft.net/texture/fe5653187521d752a91b163a62d587d2bce65c869ca996825271f6b4539cf2f")
					.name("&cStaff member of the month")
					.lore("\n&fThe new staff member of"
							+ "\n&fthe &dmonth &fhas not been"
							+ "\n&fannounced yet. Please wait"
							+ "\n&fanother &d" + Time.formatted((nextM - System.currentTimeMillis())/1000)
							+ "\n "
							+ "\n&cWaiting for a new staff member...")
					.build();
		}
		else {
			if(BStaffMember.getSMOfMonth() != null) {
				Calendar end = Calendar.getInstance();
				end.setTimeInMillis(nextM);
				end.add(Calendar.MONTH, -1);
				Calendar start = Calendar.getInstance();
				start.setTimeInMillis(nextM);
				start.add(Calendar.MONTH, -2);
				return new SkullItemBuilder()
						.playerUuid(UUID.fromString(BStaffMember.getSMOfMonth().getUuid()))
						.name("&dStaff member of the month")
						.lore("\n&fThe new staff member of"
								+ "\n&fthe &dmonth &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &d&l&kO&r &d&l" + MainDatabase.getName(BStaffMember.getSMOfMonth().getUuid()) + " &kO&r &f- &d" + BStaffMember.getSMOfMonth().calculateBetween(start.getTimeInMillis(), end.getTimeInMillis()) + " rating"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextM - System.currentTimeMillis())/1000)
								+ "\n "
								+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
						.build();
			}
			else {
				return new SkullItemBuilder()
						.url("http://textures.minecraft.net/texture/3ed1aba73f639f4bc42bd48196c715197be2712c3b962c97ebf9e9ed8efa025")
						.name("&cStaff member of the month")
						.lore("\n&fThe new staff member of"
								+ "\n&fthe &cmonth &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &c&l&kO&r &cNo one was picked... &kO&r"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextM - System.currentTimeMillis())/1000)
								+ "\n ")
						.build();
			}
		}
	}
	
	
	private ItemStack getWDaily() {
		if(nextD - System.currentTimeMillis() < 7200000) {
			return new SkullItemBuilder()
					.url("http://textures.minecraft.net/texture/fe5653187521d752a91b163a62d587d2bce65c869ca996825271f6b4539cf2f")
					.name("&cWorst staff member of the day")
					.lore("\n&fThe new &cworst &fstaff member of"
							+ "\n&fthe &dday &fhas not been"
							+ "\n&fannounced yet. Please wait"
							+ "\n&fanother &d" + Time.formatted((nextD - System.currentTimeMillis())/1000)
							+ "\n "
							+ "\n&cWaiting for a new staff member...")
					.build();
		}
		else {
			if(BStaffMember.getWSMOfDay() != null) {
				return new SkullItemBuilder()
						.playerUuid(UUID.fromString(BStaffMember.getWSMOfDay().getUuid()))
						.name("&cWorst staff member of the day")
						.lore("\n&fThe new &cworst &fstaff member of"
								+ "\n&fthe &dday &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &c&l&kO&r &c&l" + MainDatabase.getName(BStaffMember.getWSMOfDay().getUuid()) + " &kO&r &f- &d" + BStaffMember.getWSMOfDay().calculateBetween(nextD - TWO_DAYS, nextD - ONE_DAY) + " rating"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextD - System.currentTimeMillis())/1000)
								+ "\n "
								+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
						.build();
			}
			else {
				return new SkullItemBuilder()
						.url("http://textures.minecraft.net/texture/3ed1aba73f639f4bc42bd48196c715197be2712c3b962c97ebf9e9ed8efa025")
						.name("&cWorst member of the day")
						.lore("\n&fThe new &cworst &fstaff member of"
								+ "\n&fthe &cday &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &c&l&kO&r &cNo one was picked... &kO&r"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextD - System.currentTimeMillis())/1000)
								+ "\n ")
						.build();
			}
		}
	}
	
	private ItemStack getWWeekly() {
		if(nextW - System.currentTimeMillis() < 7200000) {
			return new SkullItemBuilder()
					.url("http://textures.minecraft.net/texture/fe5653187521d752a91b163a62d587d2bce65c869ca996825271f6b4539cf2f")
					.name("&cWorst staff member of the week")
					.lore("\n&fThe new &cworst &fstaff member of"
							+ "\n&fthe &dweek &fhas not been"
							+ "\n&fannounced yet. Please wait"
							+ "\n&fanother &d" + Time.formatted((nextW - System.currentTimeMillis())/1000)
							+ "\n "
							+ "\n&cWaiting for a new staff member...")
					.build();
		}
		else {
			if(BStaffMember.getWSMOfWeek() != null) {
				return new SkullItemBuilder()
						.playerUuid(UUID.fromString(BStaffMember.getWSMOfWeek().getUuid()))
						.name("&cWorst staff member of the week")
						.lore("\n&fThe new &cworst &fstaff member of"
								+ "\n&fthe &dweek &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &c&l&kO&r &c&l" + MainDatabase.getName(BStaffMember.getWSMOfWeek().getUuid()) + " &kO&r &f- &d" + BStaffMember.getWSMOfWeek().calculateBetween(nextW - TWO_WEEKS, nextW - ONE_WEEK) + " rating"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextW - System.currentTimeMillis())/1000)
								+ "\n "
								+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
						.build();
			}
			else {
				return new SkullItemBuilder()
						.url("http://textures.minecraft.net/texture/3ed1aba73f639f4bc42bd48196c715197be2712c3b962c97ebf9e9ed8efa025")
						.name("&cWorst staff member of the week")
						.lore("\n&fThe new &cworst &fstaff member of"
								+ "\n&fthe &cweek &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &c&l&kO&r &cNo one was picked... &kO&r"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextW - System.currentTimeMillis())/1000)
								+ "\n ")
						.build();
			}
		}
	}
	
	private ItemStack getWMonthly() {
		if(nextM - System.currentTimeMillis() < 7200000) {
			return new SkullItemBuilder()
					.url("http://textures.minecraft.net/texture/fe5653187521d752a91b163a62d587d2bce65c869ca996825271f6b4539cf2f")
					.name("&cWorst staff member of the month")
					.lore("\n&fThe new &cworst &fstaff member of"
							+ "\n&fthe &dmonth &fhas not been"
							+ "\n&fannounced yet. Please wait"
							+ "\n&fanother &d" + Time.formatted((nextM - System.currentTimeMillis())/1000)
							+ "\n "
							+ "\n&cWaiting for a new staff member...")
					.build();
		}
		else {
			if(BStaffMember.getWSMOfMonth() != null) {
				Calendar end = Calendar.getInstance();
				end.setTimeInMillis(nextM);
				end.add(Calendar.MONTH, -1);
				Calendar start = Calendar.getInstance();
				start.setTimeInMillis(nextM);
				start.add(Calendar.MONTH, -2);
				return new SkullItemBuilder()
						.playerUuid(UUID.fromString(BStaffMember.getWSMOfMonth().getUuid()))
						.name("&cWorst staff member of the month")
						.lore("\n&fThe new &cworst &fstaff member of"
								+ "\n&fthe &dmonth &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &c&l&kO&r &c&l" + MainDatabase.getName(BStaffMember.getWSMOfMonth().getUuid()) + " &kO&r &f- &d" + BStaffMember.getWSMOfMonth().calculateBetween(start.getTimeInMillis(), end.getTimeInMillis()) + " rating"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextM - System.currentTimeMillis())/1000)
								+ "\n "
								+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
						.build();
			}
			else {
				return new SkullItemBuilder()
						.url("http://textures.minecraft.net/texture/3ed1aba73f639f4bc42bd48196c715197be2712c3b962c97ebf9e9ed8efa025")
						.name("&cWorst staff member of the month")
						.lore("\n&fThe new &cworst &fstaff member of"
								+ "\n&fthe &cmonth &fis..."
								+ "\n&7&o(DRUM ROLL...)"
								+ "\n "
								+ "\n &c&l&kO&r &cNo one was picked &kO&r"
								+ "\n "
								+ "\n&fTime remaining for the new one:"
								+ "\n&d" + Time.formatted((nextM - System.currentTimeMillis())/1000)
								+ "\n ")
						.build();
			}
		}
	}
}
