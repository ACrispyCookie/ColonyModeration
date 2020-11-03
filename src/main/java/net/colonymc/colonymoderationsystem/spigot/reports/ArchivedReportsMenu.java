package net.colonymc.colonymoderationsystem.spigot.reports;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.colonymc.colonyspigotapi.itemstacks.ItemStackBuilder;
import net.colonymc.colonymoderationsystem.spigot.Main;

public class ArchivedReportsMenu implements Listener, InventoryHolder {
	
	Inventory inv;
	Player p;
	BukkitTask update;
	int page;
	int totalPages;
	static final ArrayList<Report> reports = new ArrayList<>();
	
	public ArchivedReportsMenu(Player p) {
		this.p = p;
		this.inv = Bukkit.createInventory(this, 54, "Archived reports");
		for(int i = 45; i < 54; i++) {
			inv.setItem(i, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).build());
		}
		openInventory();
		update = new BukkitRunnable() {
			@Override
			public void run() {
				update();
				changePage(0);
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 0, 10);
	}
	
	public ArchivedReportsMenu() {
	}

	public void changePage(int amount) {
		totalPages = (int) Math.ceil((double) reports.size() / 45);
		page = page + amount;
		for(int i = 0; i < 45; i++) {
			int index = page * 45 + i;
			if(reports.size() > index) {
				Report r = reports.get(index);
				inv.setItem(i, new ItemStackBuilder(Material.BOOK)
						.name("&fReport [&d#" + r.getId() + "&f]")
						.lore("\n&fReported player: &d" + r.getReportedName() + 
						"\n&fReporter: &d" + r.getReporterName() + "\n&fReason: &d" + r.getReason() + 
						"\n&fTime Reported: &d" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(r.getTimeReport())) + 
						"\n&fStaff processor: &d" + r.getProcessorName() + "\n&fTime processed: &d" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(r.getTimeProcessed()))
						+ "\n&fWas punished: " + (r.wasPunished() ? "&atrue" : "&cfalse") + "\n&fPunish ID: " + (!r.getPunishId().equals("NONE") ? "&d" + r.getPunishId() : "&7None"))
						.build());
			}
			else {
				inv.setItem(i, new ItemStack(Material.AIR));
			}
		}
		if(page > 0) {
			inv.setItem(45, new ItemStackBuilder(Material.ARROW).name("&dPrevious Page").build());
		}
		else {
			inv.setItem(45, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).build());
		}
		if(page + 1 < totalPages) {
			inv.setItem(53, new ItemStackBuilder(Material.ARROW).name("&dNext Page").build());
		}
		else {
			inv.setItem(53, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).build());
		}
		reports.clear();
	}
	
	public void openInventory() {
		p.openInventory(inv);
	}

	private void update() {
		for(int i = 0; i < 45; i++) {
			if(i < Report.archived.size()) {
				reports.add(Report.archived.get(i));
			}
		}
	}
	
	@Override
	public Inventory getInventory() {
		return inv;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getInventory().getHolder() instanceof ArchivedReportsMenu) {
			e.setCancelled(true);
		}	
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if(e.getInventory().getHolder() instanceof ArchivedReportsMenu) {
			ArchivedReportsMenu menu = (ArchivedReportsMenu) e.getInventory().getHolder();
			menu.update.cancel();
		}
	}
}
