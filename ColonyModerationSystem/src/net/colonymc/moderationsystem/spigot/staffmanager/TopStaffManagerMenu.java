package net.colonymc.moderationsystem.spigot.staffmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
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

import net.colonymc.api.itemstacks.ItemStackBuilder;
import net.colonymc.api.itemstacks.SkullItemBuilder;
import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.colonyapi.Time;
import net.colonymc.moderationsystem.bungee.staffmanager.BStaffMember;
import net.colonymc.moderationsystem.spigot.Main;

public class TopStaffManagerMenu implements Listener, InventoryHolder {

	Inventory inv;
	Player p;
	String tDor;
	String tWor;
	String tMor;
	String wDor;
	String wWor;
	String wMor;
	String tD;
	String tW;
	String tM;
	String wD;
	String wW;
	String wM;
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
						if(menu.nextD - System.currentTimeMillis() > 7200000) {
							new StaffManagerPlayerMenu(p, BStaffMember.getByUuid(menu.tD), menu).openInventory();
						}
					}
					else if(e.getSlot() == 13) {
						if(menu.nextW - System.currentTimeMillis() > 7200000) {
							new StaffManagerPlayerMenu(p, BStaffMember.getByUuid(menu.tW), menu).openInventory();
						}
					}
					else if(e.getSlot() == 15) {
						if(menu.nextM - System.currentTimeMillis() > 7200000) {
							new StaffManagerPlayerMenu(p, BStaffMember.getByUuid(menu.tM), menu).openInventory();
						}
					}
					else if(e.getSlot() == 29) {
						if(menu.nextD - System.currentTimeMillis() > 7200000) {
							new StaffManagerPlayerMenu(p, BStaffMember.getByUuid(menu.wD), menu).openInventory();
						}
					}
					else if(e.getSlot() == 31) {
						if(menu.nextW - System.currentTimeMillis() > 7200000) {
							new StaffManagerPlayerMenu(p, BStaffMember.getByUuid(menu.wW), menu).openInventory();
						}
					}
					else if(e.getSlot() == 33) {
						if(menu.nextM - System.currentTimeMillis() > 7200000) {
							new StaffManagerPlayerMenu(p, BStaffMember.getByUuid(menu.wM), menu).openInventory();
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
				tD = rs.getString("daily");
				tW = rs.getString("weekly");
				tM = rs.getString("monthly");
				wD = rs.getString("wDaily");
				wW = rs.getString("wWeekly");
				wM = rs.getString("wMonthly");
				tDor = rs.getString("dP");
				tWor = rs.getString("wP");
				tMor = rs.getString("MP");
				wDor = rs.getString("wdP");
				wWor = rs.getString("wwP");
				wMor = rs.getString("wmP");
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
			return new SkullItemBuilder()
					.playerUuid(UUID.fromString(tD))
					.name("&dStaff member of the day")
					.lore("\n&fThe new staff member of"
							+ "\n&fthe &dday &fis..."
							+ "\n&7&o(DRUM ROLL...)"
							+ "\n "
							+ "\n &d&l&kO&r &d&l" + MainDatabase.getName(tD) + " &kO&r &f- &d" + tDor + " rating"
							+ "\n "
							+ "\n&fTime remaining for the new one:"
							+ "\n&d" + Time.formatted((nextD - System.currentTimeMillis())/1000)
							+ "\n "
							+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
					.build();
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
			return new SkullItemBuilder()
					.playerUuid(UUID.fromString(tW))
					.name("&dStaff member of the week")
					.lore("\n&fThe new staff member of"
							+ "\n&fthe &dweek &fis..."
							+ "\n&7&o(DRUM ROLL...)"
							+ "\n "
							+ "\n &d&l&kO&r &d&l" + MainDatabase.getName(tW) + " &kO&r &f- &d" + tWor + " rating"
							+ "\n "
							+ "\n&fTime remaining for the new one:"
							+ "\n&d" + Time.formatted((nextW - System.currentTimeMillis())/1000)
							+ "\n "
							+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
					.build();
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
			return new SkullItemBuilder()
					.playerUuid(UUID.fromString(tM))
					.name("&dStaff member of the month")
					.lore("\n&fThe new staff member of"
							+ "\n&fthe &dmonth &fis..."
							+ "\n&7&o(DRUM ROLL...)"
							+ "\n "
							+ "\n &d&l&kO&r &d&l" + MainDatabase.getName(tM) + " &kO&r &f- &d" + tMor + " rating"
							+ "\n "
							+ "\n&fTime remaining for the new one:"
							+ "\n&d" + Time.formatted((nextM - System.currentTimeMillis())/1000)
							+ "\n "
							+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
					.build();
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
			return new SkullItemBuilder()
					.playerUuid(UUID.fromString(wD))
					.name("&cWorst staff member of the day")
					.lore("\n&fThe new &cworst &fstaff member of"
							+ "\n&fthe &dday &fis..."
							+ "\n&7&o(DRUM ROLL...)"
							+ "\n "
							+ "\n &c&l&kO&r &c&l" + MainDatabase.getName(wD) + " &kO&r &f- &d" + wDor + " rating"
							+ "\n "
							+ "\n&fTime remaining for the new one:"
							+ "\n&d" + Time.formatted((nextD - System.currentTimeMillis())/1000)
							+ "\n "
							+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
					.build();
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
			return new SkullItemBuilder()
					.playerUuid(UUID.fromString(wW))
					.name("&cWorst staff member of the week")
					.lore("\n&fThe new &cworst &fstaff member of"
							+ "\n&fthe &dweek &fis..."
							+ "\n&7&o(DRUM ROLL...)"
							+ "\n "
							+ "\n &c&l&kO&r &c&l" + MainDatabase.getName(wW) + " &kO&r &f- &d" + wWor + " rating"
							+ "\n "
							+ "\n&fTime remaining for the new one:"
							+ "\n&d" + Time.formatted((nextW - System.currentTimeMillis())/1000)
							+ "\n "
							+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
					.build();
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
			return new SkullItemBuilder()
					.playerUuid(UUID.fromString(wM))
					.name("&cWorst staff member of the month")
					.lore("\n&fThe new &cworst &fstaff member of"
							+ "\n&fthe &dmonth &fis..."
							+ "\n&7&o(DRUM ROLL...)"
							+ "\n "
							+ "\n &c&l&kO&r &c&l" + MainDatabase.getName(wM) + " &kO&r &f- &d" + wMor + " rating"
							+ "\n "
							+ "\n&fTime remaining for the new one:"
							+ "\n&d" + Time.formatted((nextM - System.currentTimeMillis())/1000)
							+ "\n "
							+ (p.hasPermission("colonymc.staffmanager") ? "\n&dClick here to inspect them!" : ""))
					.build();
		}
	}
}
