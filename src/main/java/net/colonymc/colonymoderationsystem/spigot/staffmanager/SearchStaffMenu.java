package net.colonymc.colonymoderationsystem.spigot.staffmanager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.colonymc.colonyspigotapi.api.itemstack.ItemStackBuilder;
import net.colonymc.colonyspigotapi.api.itemstack.SkullItemBuilder;
import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderationsystem.bungee.staffmanager.BStaffMember;
import net.colonymc.colonymoderationsystem.bungee.staffmanager.BStaffMember.FIXED_TIME;
import net.colonymc.colonymoderationsystem.spigot.Main;

public class SearchStaffMenu implements Listener, InventoryHolder {

	Inventory inv;
	Player p;
	int page = 0;
	int totalPages;
	boolean showingAll = false;
	final ArrayList<BStaffMember> found = new ArrayList<>();
	final HashMap<Integer, BStaffMember> staff = new HashMap<>();
	
	public SearchStaffMenu(Player p, String query) {
		this.p = p;
		for(BStaffMember m : BStaffMember.getAllStaff()) {
			if(MainDatabase.getName(m.getUuid()).toLowerCase().contains(query.toLowerCase())) {
				found.add(m);
			}
		}
		this.inv = Bukkit.createInventory(this, 54, "Staff found: " + found.size());
		fillInventory();
	}
	
	public SearchStaffMenu() {
		
	}
	
	public void fillInventory() {
		inv.clear();
		for(int i = 45; i < 54; i++) {
			inv.setItem(i, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).name(" ").build());
		}
		inv.setItem(49, new ItemStackBuilder(Material.ARROW).name("&dGo back").build());
		inv.setItem(50, new ItemStackBuilder(Material.INK_SACK).durability((short) (showingAll ? 10 : 8)).name((showingAll ? "&dHide" : "&dShow") + " retired staff members").build());
		totalPages = (int) Math.ceil((double) found.size()/45);
		int notShowing = 0;
		for(int i = 0; i < 45; i++) {
			int index = i + (page * 45);
			if(index <= found.size() - 1) {
				BStaffMember b = found.get(index);
				if(!b.isStaff() && !showingAll) {
					notShowing++;
					continue;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				String name = MainDatabase.getName(b.getUuid());
				String joinTimestamp = sdf.format(new Date(b.getJoin()));
				String leaveTimestamp = sdf.format(new Date(b.getLeave()));
				String rank = b.getRank().getName();
				ItemStack item = new SkullItemBuilder().playerUuid(UUID.fromString(b.getUuid())).name((b.isStaff() ? "&d" : "&c") + name)
						.lore(
								"\n&d&nInformation:" + 
								"\n &5» &fRank: &d" + rank + 
								"\n &5» &fJoined at: &d" + joinTimestamp + 
								"\n &5» &fLeft at: &d" + leaveTimestamp + 
								"\n " +
								"\n&d&nRatings:" + 
								"\n &5» &fDaily rating: &d" + b.calculateFixed(FIXED_TIME.DAILY) + 
								"\n &5» &fWeekly rating: &d" + b.calculateFixed(FIXED_TIME.WEEKLY) + 
								"\n &5» &fMonthly rating: &d" + b.calculateFixed(FIXED_TIME.MONTHLY) + 
								"\n " +
								(b.hasTitles() ? p.hasPermission("colonymc.staffmanager") ? "\n&5» &d&nTitles:" + "\n " + b.getFullTitles() + "\n \n" : "\n&5» &d&nTitles:" + "\n " + b.getTitles() + "\n \n" : "\n") +
								(b.isStaff() ? "&dClick to inspect " + name : (MainDatabase.getDiscordId(b.getUuid()) != 0 ? "&cClick to promote " + name : "&cThis player cannot be promoted"
										+ "\n&cbecause they no longer"
										+ "\n&chave their discord linked!"))
								+ (b.isStaff() ? "" : "\n "
										+ "\n&cThis player is no longer a staff member!")).build();
				inv.setItem(i - notShowing, item);
				staff.put(i - notShowing, b);
			}
			else {
				inv.setItem(i, new ItemStack(Material.AIR));
			}
		}
		if(page > 0) {
			inv.setItem(45, new ItemStackBuilder(Material.ARROW).name("&dPrevious Page").build());
		}
		else {
			inv.setItem(45, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).name(" ").build());
		}
		if(totalPages > 1 && page < totalPages - 1) {
			inv.setItem(53, new ItemStackBuilder(Material.ARROW).name("&dNext Page").build());
		}
		else {
			inv.setItem(53, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).name(" ").build());
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
	
	@Override
	public Inventory getInventory() {
		return inv;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getInventory() != null && e.getInventory().getType() != InventoryType.PLAYER) {
			if(e.getInventory().getHolder() instanceof SearchStaffMenu) {
				SearchStaffMenu m = (SearchStaffMenu) e.getInventory().getHolder();
				Player p = m.p;
				e.setCancelled(true);
				if(m.staff.containsKey(e.getSlot())) {
					if(m.staff.get(e.getSlot()).isStaff()) {
						p.closeInventory();
						new StaffManagerPlayerMenu(p, m.staff.get(e.getSlot()), m).openInventory();
					}
					else if(MainDatabase.getDiscordId(m.staff.get(e.getSlot()).getUuid()) != 0){
						p.closeInventory();
						new AddStaffMemberMenu(p, m.staff.get(e.getSlot()).getUuid()).openInventory();
					}
				}
				else if(e.getSlot() == 49) {
					p.closeInventory();
					new StaffManagerMenu(p).openInventory();
				}
				else if(e.getSlot() == 50) {
					m.showingAll = !m.showingAll;
					m.fillInventory();
					p.updateInventory();
				}
				else if(e.getSlot() == 45) {
					if(page > 0) {
						page--;
						m.fillInventory();
						p.updateInventory();
					}
				}
				else if(e.getSlot() == 53) {
					if(totalPages > 1 && page < totalPages - 1) {
						page++;
						m.fillInventory();
						p.updateInventory();
					}
				}
			}
		}
	}

}
