package net.colonymc.colonymoderationsystem.spigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderationsystem.spigot.bans.ChooseDurationMenu;
import net.colonymc.colonymoderationsystem.spigot.bans.ChoosePlayerMenu;
import net.colonymc.colonymoderationsystem.spigot.bans.ChooseReasonMenu;
import net.colonymc.colonymoderationsystem.spigot.bans.ChooseTypeMenu;
import net.colonymc.colonymoderationsystem.spigot.bans.MenuPlayer;
import net.colonymc.colonymoderationsystem.spigot.bans.SignGUI;
import net.colonymc.colonymoderationsystem.spigot.queue.LeaveQueueCommand;
import net.colonymc.colonymoderationsystem.spigot.queue.QueueCommand;
import net.colonymc.colonymoderationsystem.spigot.reports.ArchivedReportsMenu;
import net.colonymc.colonymoderationsystem.spigot.reports.ProcessReport;
import net.colonymc.colonymoderationsystem.spigot.reports.ReportMenu;
import net.colonymc.colonymoderationsystem.spigot.reports.ReportsMenu;
import net.colonymc.colonymoderationsystem.spigot.reports.SelectExistingMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.AddStaffMemberMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.AllStaffManagerMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.SearchStaffMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.SelectRankMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.StaffManagerMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.StaffManagerPlayerMenu;
import net.colonymc.colonymoderationsystem.spigot.staffmanager.TopStaffManagerMenu;
import net.colonymc.colonymoderationsystem.spigot.twofa.Freeze;
import net.luckperms.api.LuckPerms;

public class Main extends JavaPlugin {

	private static Main instance;
	private static SignGUI signGui;
	private LuckPerms luckPerms;
	private BukkitTask reportUpdate;
	boolean started = false;
	
	public enum SERVER {
		ALL("ALL"),
		SKYBLOCK("skyblock"),
		LOBBY("lobby");
		
		final String name;
		SERVER(String name){
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public static boolean contains(String c) {
			for(SERVER s : values()) {
				if(s.name().equals(c)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public void onEnable() {
		setInstance(this);
		if(MainDatabase.isConnected()) {
			signGui = new SignGUI(this);
			luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();
			setupOtherClasses();
			started = true;
			System.out.println("[ColonyModerationSystem] » has been enabled successfully!");
		}
		else {
			System.out.println("[ColonyModerationSystem] Couldn't connect to the databases! The network is not accessible from in-game. Every server will restart when the main database is back up!");
		}
	}
	
	public void onDisable() {
		if(started) {
			if(reportUpdate != null) {
				reportUpdate.cancel();
			}
		}
		System.out.println("[ColonyModerationSystem] » has been disabled successfully!");
	}
	
	private void setupOtherClasses() {
		//setup for plugin channels
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BanChannel");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "BanChannel", new MenuPlayer());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "ReportChannel");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "ReportChannel", new BungeecordConnector());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BanChannel");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "BanChannel", new BungeecordConnector());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "DiscordChannel");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "DiscordChannel", new BungeecordConnector());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "StaffModeChannel");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "StaffModeChannel", new BungeecordConnector());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "ManagerChannel");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "ManagerChannel", new BungeecordConnector());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "FeedbackChannel");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "FeedbackChannel", new BungeecordConnector());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "QueueChannel");
		this.getServer().getPluginManager().registerEvents(new ChoosePlayerMenu(), this);
		this.getServer().getPluginManager().registerEvents(new ChooseReasonMenu(), this);
		this.getServer().getPluginManager().registerEvents(new ChooseDurationMenu(), this);
		this.getServer().getPluginManager().registerEvents(new ChooseTypeMenu(), this);
		this.getServer().getPluginManager().registerEvents(new Freeze(), this);
		this.getServer().getPluginManager().registerEvents(new ArchivedReportsMenu(), this);
		this.getServer().getPluginManager().registerEvents(new ReportsMenu(), this);
		this.getServer().getPluginManager().registerEvents(new ReportMenu(), this);
		this.getServer().getPluginManager().registerEvents(new ProcessReport(), this);
		this.getServer().getPluginManager().registerEvents(new SelectExistingMenu(), this);
		this.getServer().getPluginManager().registerEvents(new AllStaffManagerMenu(), this);
		this.getServer().getPluginManager().registerEvents(new StaffManagerMenu(), this);
		this.getServer().getPluginManager().registerEvents(new StaffManagerPlayerMenu(), this);
		this.getServer().getPluginManager().registerEvents(new TopStaffManagerMenu(), this);
		this.getServer().getPluginManager().registerEvents(new SelectRankMenu(), this);
		this.getServer().getPluginManager().registerEvents(new AddStaffMemberMenu(), this);
		this.getServer().getPluginManager().registerEvents(new SearchStaffMenu(), this);
		this.getCommand("queue").setExecutor(new QueueCommand());
		this.getCommand("leavequeue").setExecutor(new LeaveQueueCommand());
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	public static SignGUI getSignGui() {
		return signGui;
	}
	
	public LuckPerms getLuckPerms() {
		return luckPerms;
	}

	private void setInstance(Main instance) {
		Main.instance = instance;
	}
}
