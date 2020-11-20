package net.colonymc.colonymoderationsystem.spigot.reports;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import net.colonymc.colonyspigotapi.api.itemstack.ItemStackBuilder;
import net.colonymc.colonymoderationsystem.spigot.BungeecordConnector;
import net.colonymc.colonymoderationsystem.spigot.Main;

public class ReportMenu implements Listener, InventoryHolder {

	Inventory inv;
	Player p;
	String name;
	
	public ReportMenu(Player p, String name) {
		this.p = p;
		this.name = name;
		this.inv = Bukkit.createInventory(this, 45, "Reporting " + name + "...");
		fillInventory();
		openInventory();
	}
	
	public ReportMenu() {
	}

	private void fillInventory() {
		inv.setItem(19, new ItemStackBuilder(Material.DIAMOND_SWORD)
				.name("&dCheating - Hacking")
				.lore("\n&fReport a player who is using\n&fany kinds of &dcheats&f!")
				.build());
		inv.setItem(20, new ItemStackBuilder(Material.IRON_SWORD)
				.name("&dOffensive language")
				.lore("\n&fReport a player who is using\n&flanguage to &doffend a player&f!")
				.build());
		inv.setItem(21, new ItemStackBuilder(Material.IRON_SWORD)
				.name("&dNegative behaviour")
				.lore("\n&fReport a player who is using\n&fbehaving &dnegatively&f!")
				.build());
		inv.setItem(22, new ItemStackBuilder(Material.IRON_SWORD)
				.name("&dAdvertising")
				.lore("\n&fReport a player who is using\n&dadvertising servers/URLs &fin the chat!")
				.build());
		inv.setItem(23, new ItemStackBuilder(Material.IRON_SWORD)
				.name("&dSpamming")
				.lore("\n&fReport a player who is\n&dspamming &fthe chat with messages!")
				.build());
		inv.setItem(24, new ItemStackBuilder(Material.DIAMOND_SWORD)
				.name("&dBug abusing")
				.lore("\n&fReport a player who is\n&fis abusing &dbugs&f!")
				.build());
	}

	public void openInventory() {
		new BukkitRunnable() {
			@Override
			public void run() {
				p.openInventory(inv);
			}
		}.runTaskLaterAsynchronously(Main.getInstance(), 1);
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getInventory().getHolder() instanceof ReportMenu) {
			ReportMenu menu = (ReportMenu) e.getInventory().getHolder();
			e.setCancelled(true);
			if(e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
				if(e.getSlot() == 19) {
					menu.p.closeInventory();
					menu.p.playSound(menu.p.getLocation(), Sound.LEVEL_UP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have reported &d" + menu.name + " &ffor the reason &d[Cheating - Hacking]&f!"));
					BungeecordConnector.sendReport(menu.name, menu.p, "Cheating - Hacking");
				}
				else if(e.getSlot() == 20) {
					menu.p.closeInventory();
					menu.p.playSound(menu.p.getLocation(), Sound.LEVEL_UP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have reported &d" + menu.name + " &ffor the reason &d[Offensive language]&f!"));
					BungeecordConnector.sendReport(menu.name, menu.p, "Offensive language");
				}
				else if(e.getSlot() == 21) {
					menu.p.closeInventory();
					menu.p.playSound(menu.p.getLocation(), Sound.LEVEL_UP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have reported &d" + menu.name + " &ffor the reason &d[Negative behaviour]&f!"));
					BungeecordConnector.sendReport(menu.name, menu.p, "Negative behaviour");
				}
				else if(e.getSlot() == 22) {
					menu.p.closeInventory();
					menu.p.playSound(menu.p.getLocation(), Sound.LEVEL_UP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have reported &d" + menu.name + " &ffor the reason &d[Advertising]&f!"));
					BungeecordConnector.sendReport(menu.name, menu.p, "Advertising");
				}
				else if(e.getSlot() == 23) {
					menu.p.closeInventory();
					menu.p.playSound(menu.p.getLocation(), Sound.LEVEL_UP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have reported &d" + menu.name + " &ffor the reason &d[Spamming]&f!"));
					BungeecordConnector.sendReport(menu.name, menu.p, "Spamming");
				}
				else if(e.getSlot() == 24) {
					menu.p.closeInventory();
					menu.p.playSound(menu.p.getLocation(), Sound.LEVEL_UP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have reported &d" + menu.name + " &ffor the reason &d[Bug abusing]&f!"));
					BungeecordConnector.sendReport(menu.name, menu.p, "Bug abusing");
				}
			}
		}
	}
	
}
