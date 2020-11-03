package net.colonymc.moderationsystem.spigot.twofa;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.colonymc.moderationsystem.spigot.Main;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class Freeze implements Listener {
	
	Player p;
	boolean shouldFreeze = false;
	boolean linked;
	BukkitTask task;
	BukkitTask particles;
	static final HashMap<Player, ItemStack[]> inventories = new HashMap<>();
	static final HashMap<Player, ItemStack[]> armors = new HashMap<>();
	static final ArrayList<Freeze> frozen = new ArrayList<>();
	
	public Freeze(String name, boolean linked) {
		this.linked = linked;
		new BukkitRunnable() {
			@Override
			public void run() {
				if(Bukkit.getPlayerExact(name) != null) {
					frozen.add(Freeze.this);
					Freeze.this.p = Bukkit.getPlayerExact(name);
					start();
					cancel();
				}
			}
		}.runTaskTimer(Main.getInstance(), 0, 1);
	}
	
	public Freeze() {
	}
	
	public void start() {
		shouldFreeze = true;
		p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 255, false, false));
		inventories.put(p, p.getInventory().getContents() == null ? new ItemStack[] {} : p.getInventory().getContents());
		armors.put(p, p.getInventory().getArmorContents() == null ? new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)} : p.getInventory().getArmorContents());
		p.getInventory().clear();
		task = new BukkitRunnable() {
			@Override
			public void run() {
				p.playSound(p.getLocation(), Sound.NOTE_BASS, 2, 1);
				if(linked) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cPlease verify your account through your discord profile!"));
				}
				else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &cPlease link your account through your discord profile!"));
				}
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 0, 160);
		particles = new BukkitRunnable() {
			int i = 0;
			@Override
			public void run() {
				p.getWorld().playEffect(p.getLocation().add(0, 2, 0), Effect.VILLAGER_THUNDERCLOUD, 2);
				CraftPlayer cp = (CraftPlayer) p;
				double timeLeft = (double) (1200 - i)/20;
				DecimalFormat df = new DecimalFormat("#.00");
		        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"§5§l» §fTime remaining: §d" + df.format(timeLeft) + "s\"}");
		        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
		        cp.getHandle().playerConnection.sendPacket(ppoc);
		        i++;
			}
		}.runTaskTimer(Main.getInstance(), 0, 1);
	}
	
	public void stop(boolean verified) {
		shouldFreeze = false;
		task.cancel();
		particles.cancel();
		frozen.remove(this);
		p.removePotionEffect(PotionEffectType.BLINDNESS);
		p.getInventory().setArmorContents(armors.get(p) == null ? new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)} : armors.get(p));
		p.getInventory().setContents(inventories.get(p) == null ? new ItemStack[] {} : inventories.get(p));
		if(verified) {
			particles = new BukkitRunnable() {
				int i = 0;
				@Override
				public void run() {
					if(i == 30) {
						this.cancel();
					}
					else {
						p.getWorld().playEffect(p.getLocation().add(0, 2, 0), Effect.HEART, 2);
					}
					i++;
				}
			}.runTaskTimer(Main.getInstance(), 0, 1);
			p.playSound(p.getLocation(), Sound.ORB_PICKUP, 2, 1);
			if(linked) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &aYou successfully verified your account! You won't have to verify it again from this IP for 6 hours!"));
			}
			else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &5&l» &aYou successfully linked your account! You won't have to verify it again from this IP for 6 hours!"));
			}
		}
		armors.remove(p);
		inventories.remove(p);
	}
	
	public static Freeze getByUuid(String uuid) {
		for(Freeze f : frozen) {
			if(f.p.getUniqueId().toString().equals(uuid)) {
				return f;
			}
		}
		return null;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(Freeze.getByUuid(e.getPlayer().getUniqueId().toString()) != null) {
			e.setCancelled(true);
		}
	}
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if(Freeze.getByUuid(e.getPlayer().getUniqueId().toString()) != null) {
			e.getPlayer().teleport(e.getPlayer().getLocation());
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if(Freeze.getByUuid(e.getPlayer().getUniqueId().toString()) != null) {
			Freeze.getByUuid(e.getPlayer().getUniqueId().toString()).stop(false);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerKickEvent e) {
		if(Freeze.getByUuid(e.getPlayer().getUniqueId().toString()) != null) {
			Freeze.getByUuid(e.getPlayer().getUniqueId().toString()).stop(false);
		}
	}
}
