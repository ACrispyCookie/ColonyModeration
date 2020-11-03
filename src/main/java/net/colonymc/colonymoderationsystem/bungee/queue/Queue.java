package net.colonymc.colonymoderationsystem.bungee.queue;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonymoderationsystem.bungee.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

public class Queue implements Listener {

	String serverName;
	int length;
	ArrayList<ProxiedPlayer> players = new ArrayList<>();
	ScheduledTask task;
	static final ArrayList<Queue> queues = new ArrayList<>();
	
	public Queue(String serverName, ArrayList<ProxiedPlayer> players) {
		this.serverName = serverName;
		this.players = players;
		this.length = players.size();
		start();
		queues.add(this);
	}
	
	public Queue(String serverName) {
		this.serverName = serverName;
		start();
		queues.add(this);
	}
	
	public Queue() {
		
	}
	
	public void start() {
		task = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
			length = players.size();
			if(length > 0) {
				if(ProxyServer.getInstance().getServerInfo(serverName).getPlayers().size() < 100) {
					players.get(0).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fSending you to the server &d" + serverName + "&f!")));
					players.get(0).connect(ProxyServer.getInstance().getServerInfo(serverName));
					players.remove(0);
				}
			}
			for(int i = 0; i < players.size(); i++) {
				ProxiedPlayer p = players.get(i);
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou are currently in position &d#" + (i + 1) + " &fon the queue for the server &d" + serverName + "&f!")));
			}
		}, 0, 5, TimeUnit.SECONDS);
	}
	
	public void addPlayer(ProxiedPlayer p) {
		if(findRank(p) == 0) {
			players.add(p);
			p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have entered the queue for the server &d" + serverName + " &fand you are on position &d#" + players.size() + "&f!")));
		}
		else if(findRank(p) == -1) {
			p.connect(ProxyServer.getInstance().getServerInfo(serverName));
			p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fSending you to the server &d" + serverName + "&f!")));
		}
		else {
			int index = findIndex(findRank(p));
			if(index != -1) {
				players.add(index, p);
			}
			else {
				players.add(p);
			}
			p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have entered the queue for the server &d" + serverName + " &fand you are on position &d#" + players.size() + "&f!")));
		}
	}
	
	public void removePlayer(ProxiedPlayer p) {
		if(players.contains(p)) {
			players.remove(p);
			p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have left the queue for the server &d" + serverName + "&f!")));
		}
	}
	
	private int findRank(ProxiedPlayer p) {
		if(p.hasPermission("staff.store") || p.hasPermission("famous.store")) {
			return -1;
		}
		if(p.hasPermission("colony.store")) {
			return 6;
		}
		else if(p.hasPermission("overlord.store")) {
			return 5;
		}
		else if(p.hasPermission("archon.store")) {
			return 4;
		}
		else if(p.hasPermission("king.store")) {
			return 3;
		}
		else if(p.hasPermission("price.store")) {
			return 2;
		}
		else if(p.hasPermission("anax.store")) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
	private int findIndex(int rank) {
		for(int i = 0; i < players.size(); i++) {
			int r = findRank(players.get(i));
			if(rank > r) {
				return i;
			}
		}
		return -1;
	}
	
	public String getName() {
		return serverName;
	}
	
	public static boolean isInQueue(ProxiedPlayer p) {
		for(Queue q : queues) {
			if(q.players.contains(p)) {
				return true;
			}
		}
		return false;
	}
	
	public static Queue getByServerName(String name) {
		for(Queue q : queues) {
			if(q.serverName.equals(name)) {
				return q;
			}
		}
		return null;
	}
	
	public static Queue getByPlayer(ProxiedPlayer p) {
		for(Queue q : queues) {
			if(q.players.contains(p)) {
				return q;
			}
		}
		return null;
	}
	
	@EventHandler
	public void onLeave(PlayerDisconnectEvent e) {
		if(Queue.isInQueue(e.getPlayer())) {
			Queue.getByPlayer(e.getPlayer()).players.remove(e.getPlayer());
		}
	}
}
