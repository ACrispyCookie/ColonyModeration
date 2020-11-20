package net.colonymc.colonymoderationsystem.spigot.staffmanager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import net.colonymc.colonyspigotapi.api.itemstack.ItemStackBuilder;
import net.colonymc.colonymoderationsystem.bungee.staffmanager.Rank;
import net.colonymc.colonymoderationsystem.bungee.staffmanager.StaffAction;
import net.colonymc.colonymoderationsystem.spigot.BungeecordConnector;
import net.colonymc.colonymoderationsystem.spigot.Main;

public class AddStaffMemberMenu implements Listener, InventoryHolder {

	Player p;
	Inventory inv;
	String uuid;
	
	public AddStaffMemberMenu(Player p, String uuid) {
		this.p = p;
		this.uuid = uuid;
		this.inv = Bukkit.createInventory(this, p.getUniqueId().toString().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f") ? 36 : 27, "Add player to staff team?");
		fillInventory();
	}
	
	public AddStaffMemberMenu() {
		
	}
	
	public void fillInventory() {
		if(p.getUniqueId().toString().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
			inv.setItem(11, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 14).name("&dPromote to &4[Owner]")
					.lore("\n&fPromote this player\n&fto &4[Owner] &fand add them to the\n&dstaff team&f! This will also give them\n&fthe &4[Owner] &frank on discord!\n \n&dClick here to proceed!").build());
			inv.setItem(12, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 14).name("&dPromote to &c[Manager]")
					.lore("\n&fPromote this player\n&fto &c[Manager] &fand add them to the\n&dstaff team&f! This will also give them\n&fthe &c[Manager] &frank on discord!\n \n&dClick here to proceed!").build());
			inv.setItem(13, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 14).name("&dPromote to &c[Admin]")
					.lore("\n&fPromote this player\n&fto &c[Admin] &fand add them to the\n&dstaff team&f! This will also give them\n&fthe &c[Admin] &frank on discord!\n \n&dClick here to proceed!").build());
			inv.setItem(14, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 10).name("&dPromote to &5[Mod]")
					.lore("\n&fPromote this player\n&fto &5[Mod] &fand add them to the\n&dstaff team&f! This will also give them\n&fthe &5[Mod] &frank on discord!\n \n&dClick here to proceed!").build());
			inv.setItem(15, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 3).name("&dPromote to &b[Helper]")
					.lore("\n&fPromote this player\n&fto &b[Helper] &fand add them to the\n&dstaff team&f! This will also give them\n&fthe &b[Helper] &frank on discord!\n \n&dClick here to proceed!").build());
			inv.setItem(20, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 4).name("&dPromote to &e[Builder]")
					.lore("\n&fPromote this player\n&fto &e[Builder] &fand add them to the\n&dstaff team&f! This will also give them\n&fthe &e[Builder] &frank on discord!\n \n&dClick here to proceed!").build());
		}
		else {
			inv.setItem(11, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 14).name("&dPromote to &c[Manager]")
					.lore("\n&fPromote this player\n&fto &c[Manager] &fand add them to the\n&dstaff team&f! This will also give them\n&fthe &c[Manager] &frank on discord!\n \n&dClick here to proceed!").build());
			inv.setItem(12, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 14).name("&dPromote to &c[Admin]")
					.lore("\n&fPromote this player\n&fto &c[Admin] &fand add them to the\n&dstaff team&f! This will also give them\n&fthe &c[Admin] &frank on discord!\n \n&dClick here to proceed!").build());
			inv.setItem(13, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 10).name("&dPromote to &5[Mod]")
					.lore("\n&fPromote this player\n&fto &5[Mod] &fand add them to the\n&dstaff team&f! This will also give them\n&fthe &5[Mod] &frank on discord!\n \n&dClick here to proceed!").build());
			inv.setItem(14, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 3).name("&dPromote to &b[Helper]")
					.lore("\n&fPromote this player\n&fto &b[Helper] &fand add them to the\n&dstaff team&f! This will also give them\n&fthe &b[Helper] &frank on discord!\n \n&dClick here to proceed!").build());
			inv.setItem(15, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 4).name("&dPromote to &e[Builder]")
					.lore("\n&fPromote this player\n&fto &e[Builder] &fand add them to the\n&dstaff team&f! This will also give them\n&fthe &e[Builder] &frank on discord!\n \n&dClick here to proceed!").build());
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
			if(e.getInventory().getHolder() instanceof AddStaffMemberMenu) {
				AddStaffMemberMenu m = (AddStaffMemberMenu) e.getInventory().getHolder();
				Player p = m.p;
				String uuid = m.uuid;
				e.setCancelled(true);
				if(e.getSlot() == 11) {
					if(p.getUniqueId().toString().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
						p.closeInventory();
						BungeecordConnector.actionOnStaff(p, uuid, StaffAction.PROMOTE, Rank.OWNER);
					}
					else {
						p.closeInventory();
						BungeecordConnector.actionOnStaff(p, uuid, StaffAction.PROMOTE, Rank.MANAGER);
					}
				}
				if(e.getSlot() == 12) {
					if(p.getUniqueId().toString().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
						p.closeInventory();
						BungeecordConnector.actionOnStaff(p, uuid, StaffAction.PROMOTE, Rank.MANAGER);
					}
					else {
						p.closeInventory();
						BungeecordConnector.actionOnStaff(p, uuid, StaffAction.PROMOTE, Rank.ADMIN);
					}
				}
				if(e.getSlot() == 13) {
					if(p.getUniqueId().toString().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
						p.closeInventory();
						BungeecordConnector.actionOnStaff(p, uuid, StaffAction.PROMOTE, Rank.ADMIN);
					}
					else {
						p.closeInventory();
						BungeecordConnector.actionOnStaff(p, uuid, StaffAction.PROMOTE, Rank.MODERATOR);
					}
				}
				if(e.getSlot() == 14) {
					if(p.getUniqueId().toString().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
						p.closeInventory();
						BungeecordConnector.actionOnStaff(p, uuid, StaffAction.PROMOTE, Rank.MODERATOR);
					}
					else {
						p.closeInventory();
						BungeecordConnector.actionOnStaff(p, uuid, StaffAction.PROMOTE, Rank.HELPER);
					}
				}
				if(e.getSlot() == 15) {
					if(p.getUniqueId().toString().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
						p.closeInventory();
						BungeecordConnector.actionOnStaff(p, uuid, StaffAction.PROMOTE, Rank.HELPER);
					}
					else {
						p.closeInventory();
						BungeecordConnector.actionOnStaff(p, uuid, StaffAction.PROMOTE, Rank.BUILDER);
					}
				}
				if(e.getSlot() == 20) {
					if(p.getUniqueId().toString().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
						p.closeInventory();
						BungeecordConnector.actionOnStaff(p, uuid, StaffAction.PROMOTE, Rank.BUILDER);
					}
				}
			}
		}
	}
}
