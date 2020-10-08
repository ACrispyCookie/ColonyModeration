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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.colonymc.api.itemstacks.ItemStackBuilder;
import net.colonymc.api.itemstacks.SkullItemBuilder;
import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.bungee.bans.PunishmentType;
import net.colonymc.moderationsystem.spigot.Main;
import net.colonymc.moderationsystem.spigot.Main.SERVER;
import net.colonymc.moderationsystem.spigot.bans.SignGUI.SignGUIListener;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

public class ChoosePlayerMenu implements Listener,InventoryHolder {
	
	final int BACK_BUTTON = 51;
	final int NEXT_BUTTON = 53;
	final int SELECT_BUTTON = 49;
	final int SELECT_OFFLINE = 48;
	Inventory inv;
	Player p;
	BukkitTask update;
	BukkitTask cancel;
	SERVER serverSelected;
	PunishmentType type;
	boolean selectMultiple;
	int page = 0;
	int totalPages;
	HashMap<Integer, MenuPlayer> players = new HashMap<Integer, MenuPlayer>();
	ArrayList<MenuPlayer> selectedPlayers = new ArrayList<MenuPlayer>();
	
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
		this.serverSelected = SERVER.ALL;
		MenuPlayer.forceLoad();
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
		for(int i = 48; i < 54; i++) {
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
		ArrayList<MenuPlayer> listToUse = 
				(serverSelected == SERVER.ALL ? MenuPlayer.getOnlinePlayers() : MenuPlayer.getPlayersOn(serverSelected));
		listToUse.remove(MenuPlayer.getByUuid(p.getUniqueId().toString()));
		if(selectMultiple) {
			for(MenuPlayer m : selectedPlayers) {
				if(!listToUse.contains(m)) {
					listToUse.add(m);
				}
			}
		}
		totalPages = (int) Math.ceil((double) listToUse.size() / 45);
		page = page + amount;
		int index = page * 45;
		for(int i = 0; i < 45; i++) {
			if(listToUse.size() > index) {
				MenuPlayer pl = listToUse.get(index);
				if(selectMultiple) {
					inv.setItem(i, new SkullItemBuilder()
							.playerUuid(UUID.fromString(pl.getUuid()))
							.name("&d" + pl.getName())
							.lore("\n&fPlayer's UUID: &d" + pl.getUuid() 
									+ "\n&fPlayer's IP: &d" + pl.getIp() 
									+ "\n&fTimes Banned: &d" + pl.getTimesBanned()
									+ "\n&fTimes Muted: &d" + pl.getTimesMuted()
									+ "\n&fStatus: " + (pl.isOnline() ? "&aOnline" : "&cOffline")
									+ "\n&fServer: &d" + (pl.getServer() == null ? "None" : pl.getServer().getName())
									+ ((selectedPlayers.contains(pl) ? "\n\n&aSelected!" : "")))
							.build());
				}
				else {
					inv.setItem(i, new SkullItemBuilder()
							.playerUuid(UUID.fromString(pl.getUuid()))
							.name("&d" + pl.getName())
							.lore("\n&fPlayer's UUID: &d" + pl.getUuid() 
									+ "\n&fPlayer's IP: &d" + pl.getIp()
									+ "\n&fTimes Banned: &d" + pl.getTimesBanned()
									+ "\n&fTimes Muted: &d" + pl.getTimesMuted()
									+ "\n&fStatus: " + (pl.isOnline() ? "&aOnline" : "&cOffline")
									+ "\n&fServer: &d" + (pl.getServer() == null ? "None" : pl.getServer().getName()))
							.build());
				}
				players.put(i, listToUse.get(index));
			}
			else {
				inv.setItem(i, new ItemStack(Material.AIR));
			}
			index++;
		}
		if(page > 0) {
			inv.setItem(BACK_BUTTON, new ItemStackBuilder(Material.ARROW).name("&dPrevious Page").build());
		}
		else {
			inv.setItem(BACK_BUTTON, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).build());
		}
		if(page + 1 < totalPages) {
			inv.setItem(NEXT_BUTTON, new ItemStackBuilder(Material.ARROW).name("&dNext Page").build());
		}
		else {
			inv.setItem(NEXT_BUTTON, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).build());
		}
		if(selectMultiple) {
			if(selectedPlayers.size() > 0) {
				if(selectedPlayers.size() == 1) {
					inv.setItem(SELECT_BUTTON, new ItemStackBuilder(Material.EMERALD_BLOCK)
							.name("&dSelect " + selectedPlayers.size() + " player")
							.lore("\n&fClick here to select &d" + selectedPlayers.size() + " &fplayer!")
							.glint(true)
							.build());
				}
				else {
					inv.setItem(SELECT_BUTTON, new ItemStackBuilder(Material.EMERALD_BLOCK)
							.name("&dSelect " + selectedPlayers.size() + " players")
							.lore("\n&fClick here to select &d" + selectedPlayers.size() + " &fplayers!")
							.glint(true)
							.build());
				}
			}
			else {
				inv.setItem(SELECT_BUTTON, new ItemStackBuilder(Material.STAINED_GLASS_PANE).durability((short) 2).build());
			}
		}
		if(selectMultiple) {
			inv.setItem(SELECT_OFFLINE, new ItemStackBuilder(Material.SIGN).name("&dAdd offline player").build());
		}
		else {
			inv.setItem(SELECT_OFFLINE, new ItemStackBuilder(Material.SIGN).name("&dSelect offline player").build());
		}
	}
	
	private BukkitTask startUpdating() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				changePage(0);
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 0, 1);
	}
	
	private void addPlayer(MenuPlayer pl) {
		if(selectMultiple) {
			if(!selectedPlayers.contains(pl)) {
				if(canBan(pl) == 1) {
					if(type != PunishmentType.MUTE || !MainDatabase.isMuted(pl.getName())) {
						selectedPlayers.add(pl);
						p.playSound(p.getLocation(), Sound.CHICKEN_EGG_POP, 2, 1);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou added &d" + pl.getName() + " &fto the list!"));
					}
					else {
						p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is already muted!"));
					}
				}
				else if(canBan(pl) == 0) {
					p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot punish this player!"));
				}
				else {
					p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error has occured please try again!"));
				}
			}
			else {
				selectedPlayers.remove(pl);
				p.playSound(p.getLocation(), Sound.CHICKEN_EGG_POP, 2, 1);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou removed &d" + pl.getName() + " &ffrom the list!"));
			}
		}
	}
	
	private void select(MenuPlayer pl) {
		if(!selectMultiple) {
			if(canBan(pl) == 1) {
				p.closeInventory();
				new ChooseReasonMenu(p, pl, -1);
			}
			else if(canBan(pl) == 0) {
				p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot punish this player!"));
			}
			else {
				p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error has occured please try again!"));
			}
		}
	}
	
	public void openInventory() {
		new BukkitRunnable() {
			@Override
			public void run() {
				p.openInventory(inv);
				startUpdating();
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
						menu.addPlayer(menu.players.get(e.getSlot()));
					}
					else {
						menu.select(menu.players.get(e.getSlot()));
					}
				}
				else if(e.getSlot() == NEXT_BUTTON) {
					if(menu.page + 1 < menu.totalPages) {
						menu.changePage(1);
					}
				}
				else if(e.getSlot() == BACK_BUTTON) {
					if(menu.page > 0) {
						menu.changePage(-1);
					}
				}
				else if(e.getSlot() == 45) {
					menu.serverSelected = SERVER.ALL;
					menu.p.playSound(menu.p.getLocation(), Sound.CHICKEN_EGG_POP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fNow viewing players on &dthe whole network&f!"));
				}
				else if(e.getSlot() == 46) {
					menu.serverSelected = SERVER.LOBBY;
					menu.p.playSound(menu.p.getLocation(), Sound.CHICKEN_EGG_POP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fNow viewing players on &dthe lobby server&f!"));
				}
				else if(e.getSlot() == 47) {
					menu.serverSelected = SERVER.SKYBLOCK;
					menu.p.playSound(menu.p.getLocation(), Sound.CHICKEN_EGG_POP, 2, 1);
					menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &fNow viewing players on &dthe skyblock server&f!"));
				}
				else if(e.getSlot() == SELECT_BUTTON) {
					if(menu.selectMultiple && menu.selectedPlayers.size() > 0) {
						menu.p.closeInventory();
						new ChooseReasonMenu(menu.p, menu.selectedPlayers, menu.type);
					}
				}
				else if(e.getSlot() == SELECT_OFFLINE) {
					menu.p.closeInventory();
					Main.getSignGui().open(menu.p, new String[] {"", "^^^^^^^^^^^^^^^", "Enter the name", "of the player"}, new SignGUIListener() {
						@Override
						public void onSignDone(Player player, String[] lines) {
							String msg = lines[0].replaceAll("\"", "");
							if(!msg.isEmpty()) {
								if(menu.selectMultiple) {
									String uuid = MainDatabase.getUuid(msg);
									if(!uuid.equals("Not Found")) {
										MenuPlayer p = MenuPlayer.load(uuid);
										menu.addPlayer(p);
										menu.openInventory();
									}
									else {
										menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cWe couldn't find this player!"));
										menu.p.playSound(menu.p.getLocation(), Sound.NOTE_BASS, 2, 1);
										menu.openInventory();
									}
								}
								else {
									String uuid = MainDatabase.getUuid(msg);
									if(!uuid.equals("Not Found")) {
										MenuPlayer p = MenuPlayer.load(uuid);
										menu.select(p);
									}
									else {
										menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cWe couldn't find this player!"));
										menu.p.playSound(menu.p.getLocation(), Sound.NOTE_BASS, 2, 1);
										menu.openInventory();
									}
								}
							}
							else {
								menu.p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou didn't enter a name!"));
								menu.p.playSound(menu.p.getLocation(), Sound.NOTE_BASS, 2, 1);
								menu.openInventory();
							}
						}
					});
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
	
	public int canBan(MenuPlayer player) {
	    UserManager userManager = Main.getInstance().getLuckPerms().getUserManager();
	    CompletableFuture<User> userFuture = userManager.loadUser(UUID.fromString(player.getUuid()));
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

}
