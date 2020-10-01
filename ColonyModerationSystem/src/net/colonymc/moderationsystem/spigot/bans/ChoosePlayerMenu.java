package net.colonymc.moderationsystem.spigot.bans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import net.colonymc.api.itemstacks.ItemStackBuilder;
import net.colonymc.api.itemstacks.SkullItemBuilder;
import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.bungee.bans.PunishmentType;
import net.colonymc.moderationsystem.spigot.BungeecordConnector;
import net.colonymc.moderationsystem.spigot.Main;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

public class ChoosePlayerMenu implements Listener,PluginMessageListener,InventoryHolder {
	
	Inventory inv;
	Player p;
	BukkitTask update;
	BukkitTask cancel;
	String serverSelected;
	boolean selectMultiple;
	PunishmentType type;
	int page = 0;
	int totalPages;
	String[] serverNames = new String[] {"skyblock", "lobby"};
	HashMap<Integer, String> players = new HashMap<Integer, String>();
	ArrayList<String> selectedPlayers = new ArrayList<String>();
	public static HashMap<String, ArrayList<String>> servers = new HashMap<String, ArrayList<String>>();
	
	public ChoosePlayerMenu(Player p, PunishmentType type, boolean selectMultiple) {
		this.p = p;
		this.selectMultiple = selectMultiple;
		if(selectMultiple) {
			this.type = type;
			this.inv = Bukkit.createInventory(this, 54, "Select multiple players...");
		}
		else {
			this.inv = Bukkit.createInventory(this, 54, "Select a player...");
		}
		this.serverSelected = "ALL";
		fillInventory();
		openInventory();
		update = startUpdating();
		cancel = new BukkitRunnable() {
			@Override
			public void run() {
				p.closeInventory();
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cProcess cancelled due to inactivity in the last 2 minutes!"));
			}
		}.runTaskLaterAsynchronously(Main.getInstance(), 2400);
	}

	public ChoosePlayerMenu() {
	}
	
	private void fillInventory() {
		for(int i = 46 + servers.keySet().size(); i < 54; i++) {
			inv.setItem(i, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
					.durability((short) 2)
					.build());
		}
		inv.setItem(45, new SkullItemBuilder()
				.url("http://textures.minecraft.net/texture/f0e61c7f947f4dfafe327ba5c51b73124a2a72b7fe77ee666fd7ed40b73362e2")
				.name("&dAll players")
				.lore("\n&fClick here to display\n&fall the players online!")
				.build());
		inv.setItem(46, new SkullItemBuilder()
				.url("http://textures.minecraft.net/texture/28d3c19dd0179f0b220dbdb313f92d062a9db9397288c8b3429fa817e43a6f95")
				.name("&dLobby players")
				.lore("\n&fClick here to display\n&fonline players on the &dlobby&f!")
				.build());
		inv.setItem(47, new SkullItemBuilder()
				.url("http://textures.minecraft.net/texture/48ad7ade366bb438d3672883e79e5d0ce90ba22ed1a6e23691f2992f1635f639")
				.name("&dSkyblock players")
				.lore("\n&fClick here to display\n&fonline players on &dskyblock&f!")
				.build());
	}

	private void changePage(int amount) {
		ArrayList<String> allPlayers = new ArrayList<String>();
		if(serverSelected.equals("ALL")) {
			if(servers.get("skyblock") != null && !servers.get("skyblock").isEmpty()) {
				allPlayers.addAll(servers.get("skyblock"));
			}
			if(servers.get("lobby") != null && !servers.get("lobby").isEmpty()) {
				allPlayers.addAll(servers.get("lobby"));
			}
		}
		else if(serverSelected.equals("skyblock")) {
			if(servers.get("skyblock") != null && !servers.get("skyblock").isEmpty()) {
				allPlayers.addAll(servers.get("skyblock"));
			}
		}
		else if(serverSelected.equals("lobby")) {
			if(servers.get("lobby") != null && !servers.get("lobby").isEmpty()) {
				allPlayers.addAll(servers.get("lobby"));
			}
		}
		for(int i = 0; i < allPlayers.size(); i++) {
			if(p.getName().equals(allPlayers.get(i))) {
				allPlayers.remove(i);
			}
		}
		totalPages = (int) Math.ceil((double) allPlayers.size() / 45);
		page = page + amount;
		int index = page * 45;
		for(int i = 0; i < 45; i++) {
			if(allPlayers.size() > index) {
				if(selectMultiple) {
					inv.setItem(i, new SkullItemBuilder()
							.playerName(allPlayers.get(index))
							.name("&d" + allPlayers.get(index))
							.lore("\n&fPlayer's UUID: &d" + MainDatabase.getUuid(allPlayers.get(index)) + 
									"\n&fPlayer's IP: &d" + MainDatabase.getLastIp(allPlayers.get(index)) + "\n&fTimes Banned: &d" + MainDatabase.getTimesBanned(allPlayers.get(index)) + 
									"\n&fTimes Muted: &d" + MainDatabase.getTimesMuted(allPlayers.get(index)) + ((selectedPlayers.contains(allPlayers.get(index)) ? "\n\n&aSelected!" : "")))
							.build());
				}
				else {
					inv.setItem(i, new SkullItemBuilder()
							.playerName(allPlayers.get(index))
							.name("&d" + allPlayers.get(index))
							.lore("\n&fPlayer's UUID: &d" + MainDatabase.getUuid(allPlayers.get(index)) + 
									"\n&fPlayer's IP: &d" + MainDatabase.getLastIp(allPlayers.get(index)) + "\n&fTimes Banned: &d" + MainDatabase.getTimesBanned(allPlayers.get(index)) + 
									"\n&fTimes Muted: &d" + MainDatabase.getTimesMuted(allPlayers.get(index)))
							.build());
				}
				players.put(i, allPlayers.get(index));
			}
			else {
				inv.setItem(i, new ItemStack(Material.AIR));
			}
			index++;
		}
		if(page > 0) {
			inv.setItem(48, new ItemStackBuilder(Material.ARROW).name("&dPrevious Page").build());
		}
		else {
			inv.setItem(48, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).build());
		}
		if(page + 1 < totalPages) {
			inv.setItem(51, new ItemStackBuilder(Material.ARROW).name("&dNext Page").build());
		}
		else {
			inv.setItem(51, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).build());
		}
		if(selectMultiple) {
			if(selectedPlayers.size() > 0) {
				if(selectedPlayers.size() == 1) {
					inv.setItem(53, new ItemStackBuilder(Material.EMERALD_BLOCK)
							.name("&dSelect " + selectedPlayers.size() + " player")
							.lore("\n&fClick here to select &d" + selectedPlayers.size() + " &fplayer!")
							.glint(true)
							.build());
				}
				else {
					inv.setItem(53, new ItemStackBuilder(Material.EMERALD_BLOCK)
							.name("&dSelect " + selectedPlayers.size() + " players")
							.lore("\n&fClick here to select &d" + selectedPlayers.size() + " &fplayers!")
							.glint(true)
							.build());
				}
			}
			else {
				inv.setItem(53, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).build());
			}
		}
	}
	
	private BukkitTask startUpdating() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				updateServers();
				changePage(0);
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 0, 1);
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
		if(e.getInventory().getHolder() instanceof ChoosePlayerMenu) {
			e.setCancelled(true);
			ChoosePlayerMenu menu = (ChoosePlayerMenu) e.getInventory().getHolder();
			if(e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
				menu.cancel.cancel();
				menu.cancel = new BukkitRunnable() {
					@Override
					public void run() {
						menu.p.closeInventory();
						menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cProcess cancelled due to inactivity in the last 2 minutes!"));
					}
				}.runTaskLaterAsynchronously(Main.getInstance(), 2400);
				if(menu.players.containsKey(e.getSlot())) {
					if(menu.selectMultiple) {
						if(!menu.selectedPlayers.contains(menu.players.get(e.getSlot()))) {
							if(menu.canBan(menu.players.get(e.getSlot())) == 1) {
								if(menu.type != PunishmentType.MUTE || !MainDatabase.isMuted(menu.players.get(e.getSlot()))) {
									menu.selectedPlayers.add(menu.players.get(e.getSlot()));
									menu.p.playSound(menu.p.getLocation(), Sound.CHICKEN_EGG_POP, 2, 1);
									menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou added &d" + menu.players.get(e.getSlot()) + " &fto the list!"));
								}
								else {
									menu.p.playSound(menu.p.getLocation(), Sound.NOTE_BASS, 2, 1);
									menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is already muted!"));
								}
							}
							else if(menu.canBan(menu.players.get(e.getSlot())) == 0) {
								menu.p.playSound(menu.p.getLocation(), Sound.NOTE_BASS, 2, 1);
								menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot punish this player!"));
							}
							else {
								menu.p.playSound(menu.p.getLocation(), Sound.NOTE_BASS, 2, 1);
								menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error has occured please try again!"));
							}
						}
						else {
							menu.selectedPlayers.remove(menu.selectedPlayers.indexOf(menu.players.get(e.getSlot())));
							menu.p.playSound(menu.p.getLocation(), Sound.CHICKEN_EGG_POP, 2, 1);
							menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou removed &d" + menu.players.get(e.getSlot()) + " &ffrom the list!"));
						}
					}
					else {
						if(menu.canBan(menu.players.get(e.getSlot())) == 1) {
							String playerName = menu.players.get(e.getSlot());
							menu.p.closeInventory();
							new ChooseReasonMenu(menu.p, playerName, -1);
						}
						else if(menu.canBan(menu.players.get(e.getSlot())) == 0) {
							menu.p.playSound(menu.p.getLocation(), Sound.NOTE_BASS, 2, 1);
							menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot punish this player!"));
						}
						else {
							menu.p.playSound(menu.p.getLocation(), Sound.NOTE_BASS, 2, 1);
							menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error has occured please try again!"));
						}
					}
				}
				else if(e.getSlot() == 52) {
					if(menu.page + 1 < menu.totalPages) {
						menu.changePage(1);
					}
				}
				else if(e.getSlot() == 49) {
					if(menu.page > 0) {
						menu.changePage(-1);
					}
				}
				else if(e.getSlot() == 45) {
					menu.serverSelected = "ALL";
					menu.p.playSound(menu.p.getLocation(), Sound.CHICKEN_EGG_POP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fNow viewing players on &dthe whole network&f!"));
				}
				else if(e.getSlot() == 46) {
					menu.serverSelected = "lobby";
					menu.p.playSound(menu.p.getLocation(), Sound.CHICKEN_EGG_POP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fNow viewing players on &dthe lobby server&f!"));
				}
				else if(e.getSlot() == 47) {
					menu.serverSelected = "skyblock";
					menu.p.playSound(menu.p.getLocation(), Sound.CHICKEN_EGG_POP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fNow viewing players on &dthe skyblock server&f!"));
				}
				else if(e.getSlot() == 53) {
					if(menu.selectMultiple && menu.selectedPlayers.size() > 0) {
						menu.p.closeInventory();
						new ChooseReasonMenu(menu.p, menu.selectedPlayers, menu.type);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if(e.getInventory().getHolder() instanceof ChoosePlayerMenu) {
			ChoosePlayerMenu menu = (ChoosePlayerMenu) e.getInventory().getHolder();
			menu.update.cancel();
			menu.cancel.cancel();
		}
	}
	
	public int canBan(String name) {
	    UserManager userManager = Main.getInstance().getLuckPerms().getUserManager();
	    CompletableFuture<User> userFuture = userManager.loadUser(UUID.fromString(MainDatabase.getUuid(name)));
	    User user;
		try {
			user = userFuture.get(1, TimeUnit.SECONDS);
	    	if(p.hasPermission("*") || (!user.getPrimaryGroup().equals("admin") && !user.getPrimaryGroup().equals("manager") && !user.getPrimaryGroup().equals("owner"))) {
				return 1;
			}
			else {
				return 0;
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public void updateServers() {
		for(String s : serverNames) {
			BungeecordConnector.servers(p, s);
		}
	}

	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] data) {
		ByteArrayDataInput in = ByteStreams.newDataInput(data);
		String subchannel = in.readUTF();
		if(channel.equals("BungeeCord")) {
			if(subchannel.equals("PlayerList")) {
				String server = in.readUTF();
				String[] playerList = in.readUTF().split(", ");
				ArrayList<String> players = new ArrayList<String>();
				for(String s : playerList) {
					players.add(s);
				}
				if(playerList.length == 1 && playerList[0].equals("")) {
					servers.put(server, new ArrayList<String>());
				}
				else {
					servers.put(server, players);
				}
			}
		}
	}

}
