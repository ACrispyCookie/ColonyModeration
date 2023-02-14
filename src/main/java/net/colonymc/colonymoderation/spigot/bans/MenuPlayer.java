package net.colonymc.colonymoderation.spigot.bans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderation.spigot.BungeecordConnector;
import net.colonymc.colonymoderation.spigot.Main;
import net.colonymc.colonymoderation.spigot.Main.SERVER;

public class MenuPlayer implements PluginMessageListener {
	
	String name;
	String uuid;
	String ip;
	int timesBanned;
	int timesMuted;
	boolean online;
	SERVER server;
	static final ArrayList<MenuPlayer> players = new ArrayList<>();
	static BukkitTask update;
	
	public MenuPlayer(String uuid, String ip, int timesBanned, int timesMuted, SERVER server) {
		this.uuid = uuid;
		this.name = MainDatabase.getName(uuid);
		this.ip = ip;
		this.timesBanned = timesBanned;
		this.timesMuted = timesMuted;
		this.online = server != null;
		this.server = server;
		players.add(this);
	}

	public MenuPlayer() {
		update = new BukkitRunnable() {
			@Override
			public void run() {
				loadPlayers();
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 0, 60);
	}

	public String getName() {
		return name;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public String getIp() {
		return ip;
	}
	
	public int getTimesBanned() {
		return timesBanned;
	}
	
	public int getTimesMuted() {
		return timesMuted;
	}
	
	public boolean isOnline() {
		return online;
	}
	
	public SERVER getServer() {
		return server;
	}
	
	public static ArrayList<MenuPlayer> getPlayersOn(SERVER server) {
		ArrayList<MenuPlayer> pls = new ArrayList<>();
		for(MenuPlayer pl : players) {
			if(pl.getServer() == server) {
				pls.add(pl);
			}
		}
		return pls;
	}
	
	public static ArrayList<MenuPlayer> getOnlinePlayers() {
		ArrayList<MenuPlayer> pls = new ArrayList<>();
		for(MenuPlayer pl : players) {
			if(pl.isOnline()) {
				pls.add(pl);
			}
		}
		return pls;
	}
	
	public static ArrayList<MenuPlayer> getPlayers() {
		return players;
	}
	
	public static MenuPlayer getByUuid(String uuid) {
		for(MenuPlayer p : players) {
			if(p.getUuid().equals(uuid)) {
				return p;
			}
		}
		return null;
	}
	
	public static MenuPlayer load(String uuid) {
		if(MenuPlayer.getByUuid(uuid) != null) {
			return MenuPlayer.getByUuid(uuid);
		}
		else {
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM PlayerInfo WHERE uuid='" + uuid + "'");
			try {
				if(rs.next()) {
					String ip = rs.getString("ip");
					int timesB = rs.getInt("timesBanned");
					int timesM = rs.getInt("timesMuted");
					return new MenuPlayer(uuid, ip, timesB, timesM, null);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void forceLoad() {
		loadPlayers();
	}
	
	public static void pauseUpdating() {
		update.cancel();
	}
	
	private static void loadPlayers() {
		if(Bukkit.getOnlinePlayers().size() > 0) {
			BungeecordConnector.requestPlayers(Bukkit.getOnlinePlayers().iterator().next());
		}
	}

	private void removeOld(ArrayList<MenuPlayer> players) {
		for(MenuPlayer pl : MenuPlayer.players) {
			if(pl.isOnline() && !players.contains(pl)) {
				pl.online = false;
				pl.server = null;
			}
		}
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] data) {
		ByteArrayDataInput in = ByteStreams.newDataInput(data);
		String subchannel = in.readUTF();
		if(channel.equals("BanChannel")) {
			if(subchannel.equals("PlayerList")) {
				String json = in.readUTF();
				Gson g = new Gson();
				JsonArray array = g.fromJson(json, JsonArray.class);
				HashMap<String, String> players = new HashMap<>();
				for(JsonElement o : array) {
					if(o instanceof JsonObject) {
						JsonObject obj = (JsonObject) o;
						String uuid = obj.get("uuid").getAsString();
						String server = obj.get("server").getAsString();
						players.put(uuid, server);
					}
				}
				if(players.size() > 0) {
					ArrayList<MenuPlayer> currentlyOnline = new ArrayList<>();
					for(String s : players.keySet()) {
						String name = MainDatabase.getName(s);
						String ip = MainDatabase.getLastIp(name);
						int timesB = MainDatabase.getTimesBanned(name);
						int timesM = MainDatabase.getTimesMuted(name);
						if(MenuPlayer.getByUuid(s) != null) {
							MenuPlayer pl = MenuPlayer.getByUuid(s);
							pl.name = name;
							pl.ip = ip;
							pl.timesBanned = timesB;
							pl.timesMuted = timesM;
							pl.online = true;
							pl.server = SERVER.valueOf(players.get(s).toUpperCase());
							currentlyOnline.add(pl);
						}
						else {
							MenuPlayer pl = new MenuPlayer(s, ip, timesB, timesM, null);
							currentlyOnline.add(pl);
						}
					}
					removeOld(currentlyOnline);
				}
			}
		}
	}
}
