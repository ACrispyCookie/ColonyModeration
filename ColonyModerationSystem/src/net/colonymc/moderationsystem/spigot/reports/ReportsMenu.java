package net.colonymc.moderationsystem.spigot.reports;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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

import net.colonymc.api.itemstacks.InventoryUtils;
import net.colonymc.api.itemstacks.ItemStackBuilder;
import net.colonymc.api.itemstacks.SkullItemBuilder;
import net.colonymc.moderationsystem.spigot.Main;

public class ReportsMenu implements Listener, InventoryHolder {
	
	Inventory inv;
	Player p;
	BukkitTask update;
	int page;
	int totalPages;
	HashMap<Integer, Report> slots = new HashMap<Integer, Report>();
	
	public ReportsMenu(Player p) {
		this.p = p;
		this.inv = Bukkit.createInventory(this, 54, "Open Reports");
		InventoryUtils.fillInventory(this, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).build());
		update = new BukkitRunnable() {
			@Override
			public void run() {
				changePage(0);
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 0, 1);
		openInventory();
	}
	
	public ReportsMenu() {
	}

	public void changePage(int amount) {
		page = page + amount;
		totalPages = (int) Math.ceil((double) Report.reports.size() / 45);
		int index = page * 45;
		for(int i = 0; i < 45; i++) {
			if(Report.reports.size() > index) {
				Report r = Report.reports.get(index);
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				inv.setItem(i, new SkullItemBuilder()
						.playerUuid(UUID.fromString(r.getReportedUuid()))
						.name("&f[&d#" + r.getId() + "&f] &fReport for &d" + r.getReportedName())
						.lore("\n&fReported by: &d" + r.getReporterName() + "\n&fReason: &d" + r.getReason() + "\n&fTime Reported: &d" + sdf.format(new Date(r.getTimeReport())) + 
								"\n\n&dClick to process the report!")
						.build());
				slots.put(i, Report.reports.get(index));
			}
			else {
				inv.setItem(i, new ItemStack(Material.AIR));
			}
			index++;
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
	public void onClick(InventoryClickEvent e) {
		if(e.getInventory().getHolder() instanceof ReportsMenu) {
			ReportsMenu menu = (ReportsMenu) e.getInventory().getHolder();
			e.setCancelled(true);
			if(e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
				if(e.getClickedInventory().getType() != InventoryType.PLAYER) {
					if(menu.slots.containsKey(e.getSlot())) {
						menu.slots.get(e.getSlot()).process(menu.p);
					}
					else if(e.getSlot() == 53) {
						if(menu.page + 1 < menu.totalPages) {
							menu.changePage(1);
						}
					}
					else if(e.getSlot() == 45) {
						if(menu.page > 0) {
							menu.changePage(-1);
						}
					}
				}
			}
		}	
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if(e.getInventory().getHolder() instanceof ReportsMenu) {
			ReportsMenu menu = (ReportsMenu) e.getInventory().getHolder();
			menu.update.cancel();
		}
	}

}
