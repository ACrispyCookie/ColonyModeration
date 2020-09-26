package net.colonymc.moderationsystem.bungee;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.bungee.bans.Ban;
import net.colonymc.moderationsystem.bungee.bans.BanCommand;
import net.colonymc.moderationsystem.bungee.bans.CheckCommand;
import net.colonymc.moderationsystem.bungee.bans.JoinListener;
import net.colonymc.moderationsystem.bungee.bans.MassBanCommand;
import net.colonymc.moderationsystem.bungee.bans.Mute;
import net.colonymc.moderationsystem.bungee.bans.UnbanCommand;
import net.colonymc.moderationsystem.bungee.bans.UnmuteCommand;
import net.colonymc.moderationsystem.bungee.chatmoderation.ChatCommand;
import net.colonymc.moderationsystem.bungee.chatmoderation.ChatListener;
import net.colonymc.moderationsystem.bungee.discord.DiscordBan;
import net.colonymc.moderationsystem.bungee.discord.DiscordListeners;
import net.colonymc.moderationsystem.bungee.discord.DiscordMute;
import net.colonymc.moderationsystem.bungee.discord.DiscordPunishCommand;
import net.colonymc.moderationsystem.bungee.discord.DiscordUser;
import net.colonymc.moderationsystem.bungee.discord.RoleCommand;
import net.colonymc.moderationsystem.bungee.queue.LeaveQueueCommand;
import net.colonymc.moderationsystem.bungee.queue.Queue;
import net.colonymc.moderationsystem.bungee.queue.QueueCommand;
import net.colonymc.moderationsystem.bungee.reports.ArchivedReportsCommand;
import net.colonymc.moderationsystem.bungee.reports.Report;
import net.colonymc.moderationsystem.bungee.reports.ReportCommand;
import net.colonymc.moderationsystem.bungee.reports.ReportsCommand;
import net.colonymc.moderationsystem.bungee.staffmanager.DemoteCommand;
import net.colonymc.moderationsystem.bungee.staffmanager.PromoteCommand;
import net.colonymc.moderationsystem.bungee.staffmanager.StaffJoinListener;
import net.colonymc.moderationsystem.bungee.twofa.FreezeSession;
import net.colonymc.moderationsystem.bungee.twofa.LinkCommand;
import net.colonymc.moderationsystem.bungee.twofa.LinkedPlayer;
import net.colonymc.moderationsystem.bungee.twofa.McLinkCommand;
import net.colonymc.moderationsystem.bungee.twofa.SecureLogoutCommand;
import net.colonymc.moderationsystem.bungee.twofa.UnlinkCommand;
import net.colonymc.moderationsystem.bungee.twofa.VerifiedPlayer;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class Main extends Plugin {
 	
    private File logfile;
	private ScheduledTask databaseLoader;
	private static LuckPerms luckPerms;
	private static Main instance;
	private static JDA jda;
	boolean started = false;
	
	public void onEnable() {
		setInstance(this);
		System.out.println("[ColonyModerationSystem] Waiting for ColonyAPI...");
		Runnable run = new Runnable() {
			@Override
			public void run() {
				try {
					MainDatabase.isConnected();
					if(!MainDatabase.isConnecting()) {
						databaseLoader.cancel();
						if(MainDatabase.isConnected()) {
							setupDiscordBot();
							loadNonDatabase();
							loadDatabaseRequired();
							loadLinkedPlayers();
							setupRevokers();
							logfile = new File(ProxyServer.getInstance().getPluginsFolder() + "/ColonyModerationSystem/log.txt");
							luckPerms = LuckPermsProvider.get();
							if(!logfile.exists()) {
								createNewLogFile();
							}
							loadQueues();
							started = true;
							System.out.println("[ColonyModerationSystem] has been enabled successfully!");
						}
						else {
							ProxyServer.getInstance().getPluginManager().registerListener(Main.this, new DatabaseListener());
							System.out.println("[ColonyModerationSystem] Couldn't connect to the databases! The network is not accessible from in-game. Every server will restart when the main database is back up!");
						}
					}
				}
				catch(NoSuchMethodError e) {
					
				}	
			}
		};
		databaseLoader = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), run, 0, 2, TimeUnit.SECONDS);
	}

	private void createNewLogFile() {
		try {
			logfile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onDisable() {
		if(started) {
			jda.shutdown();
		}
		System.out.println("[ColonyModerationSystem] has been disabled successfully!");
	}
	
	@SuppressWarnings("deprecation")
	public void loadLinkedPlayers() {
		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM VerifiedPlayers WHERE playerUuid='" + p.getUniqueId().toString() + "';");
			try {
				if(rs.next()) {
					LinkedPlayer.linked.add(new LinkedPlayer(p, Main.getUser(rs.getLong("userDiscordID"))));
					if(p.hasPermission("staff.store")) {
						VerifiedPlayer verp = VerifiedPlayer.getByLinkedPlayer(LinkedPlayer.getByPlayer(p));
						if(verp != null && !verp.getCurrentIP().equals(p.getAddress().getHostString())) {
							verp.remove();
							new FreezeSession(LinkedPlayer.getByPlayer(p), p.getServer().getInfo());
						}
						else if(verp == null) {
							new FreezeSession(LinkedPlayer.getByPlayer(p), p.getServer().getInfo());
						}
					}
				}
				else {
					if(p.hasPermission("staff.store")) {
						new FreezeSession(p, p.getServer().getInfo());
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void loadQueues() {
		for(String s : ProxyServer.getInstance().getServers().keySet()) {
			new Queue(s);
		}
	}

	public void loadReports() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM ActiveReports;");
		int counter = 0;
		try {
			while(rs.next()) {
				new Report(MainDatabase.getName(rs.getString("playerUuid")), MainDatabase.getName(rs.getString("reporterUuid")), rs.getString("playerUuid"), rs.getString("reporterUuid"), rs.getString("reason"), 
						rs.getLong("timeReported"), rs.getInt("id"));
				counter++;
			}
			System.out.println("[ColonyModerationSystem] Successfully loaded " + counter + " active reports!");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void setupDiscordBot() {
		try {
			jda = new JDABuilder(AccountType.BOT).setToken("NjQ0NjU1MTIzNTYxMTg1MzQ5.Xo4RLg.nSRlrG0N-tRKZXL0wTQ6kvUkioY").build();
			jda.awaitReady();
			Main.getGuild().getVoiceChannelById(705743260953477188L).getManager().setName("✅ Total Users: " + Main.getGuild().getMemberCount()).queue();
			ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
				boolean players = true;
				@Override
				public void run() {
					if(players) {
						if(ProxyServer.getInstance().getOnlineCount() == 1) {
							jda.getPresence().setActivity(Activity.watching("1 player online"));
						}
						else {
							jda.getPresence().setActivity(Activity.watching(ProxyServer.getInstance().getOnlineCount() + " players online"));
						}
					}
					else {
						jda.getPresence().setActivity(Activity.playing("on play.colonymc.net"));
					}
					players = !players;
				}
			}, 0, 5, TimeUnit.SECONDS);
		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void loadNonDatabase() {
		//setup for channels
		getProxy().registerChannel("ReportChannel");
		getProxy().registerChannel("BanChannel");
		getProxy().registerChannel("DiscordChannel");
		getProxy().registerChannel("StaffModeChannel");
		getProxy().registerChannel("QueueChannel");
		//listeners setup
		ProxyServer.getInstance().getPluginManager().registerListener(this, new DatabaseListener());
		ProxyServer.getInstance().getPluginManager().registerListener(this, new ChatListener());
		ProxyServer.getInstance().getPluginManager().registerListener(this, new Queue());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new QueueCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new LeaveQueueCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new ChatCommand());
	}
	
	public void loadDatabaseRequired() {
		ProxyServer.getInstance().getPluginManager().registerListener(this, new SpigotConnector());
		ProxyServer.getInstance().getPluginManager().registerListener(this, new Mute());
		ProxyServer.getInstance().getPluginManager().registerListener(this, new JoinListener());
		ProxyServer.getInstance().getPluginManager().registerListener(this, new DisconnectListener());
		ProxyServer.getInstance().getPluginManager().registerListener(this, new FreezeSession());
		ProxyServer.getInstance().getPluginManager().registerListener(this, new StaffJoinListener());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new McLinkCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new UnlinkCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new BanCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new MassBanCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new UnbanCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new UnmuteCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new CheckCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new ReportCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new ReportsCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new ArchivedReportsCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new SecureLogoutCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new RoleCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new DemoteCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new PromoteCommand());
		jda.addEventListener(new LinkCommand());
		jda.addEventListener(new FreezeSession());
		jda.addEventListener(new DiscordListeners());
		jda.addEventListener(new DiscordPunishCommand());
	}

	private void setupRevokers() {
		loadReports();
		setupBanRevokers();
		setupMuteRevokers();
		setupDiscordBanRevokers();
		setupDiscordUnacceptedRevokers();
	}

	private void setupBanRevokers() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM ActiveBans;");
		int count = 0;
		try {
			while(rs.next()) {
				long bannedUntil = rs.getLong("bannedUntil");
				if(bannedUntil < System.currentTimeMillis()) {
					MainDatabase.sendStatement("DELETE FROM ActiveBans WHERE uuid='" + rs.getString("uuid") + "';");
				}
				else {
					new Ban(MainDatabase.getName(rs.getString("uuid")), rs.getString("uuid"), rs.getString("bannedIp"), 
							(rs.getString("staffUuid") != null ? MainDatabase.getName(rs.getString("staffUuid")) : "CONSOLE"), rs.getString("staffUuid") != null ? rs.getString("staffUuid") : "CONSOLE"
								, rs.getString("reason"), rs.getString("ID"), rs.getLong("bannedUntil"), rs.getLong("issuedAt"));
					count++;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("[ColonyModerationSystem] Successfully started " + count + " in-game ban revokers!");
	}
	
	private void setupMuteRevokers() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM ActiveMutes;");
		int count = 0;
		try {
			while(rs.next()) {
				long bannedUntil = rs.getLong("mutedUntil");
				if(bannedUntil < System.currentTimeMillis()) {
					MainDatabase.sendStatement("DELETE FROM ActiveMutes WHERE uuid='" + rs.getString("uuid") + "';");
				}
				else {
					new Mute(MainDatabase.getName(rs.getString("uuid")), rs.getString("uuid"), 
							(rs.getString("staffUuid") != null ? MainDatabase.getName(rs.getString("staffUuid")) : "CONSOLE"), rs.getString("staffUuid") != null ? rs.getString("staffUuid") : "CONSOLE", 
							rs.getString("reason"), rs.getString("ID"), rs.getLong("mutedUntil"), rs.getLong("issuedAt"));
					count++;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("[ColonyModerationSystem] Successfully started " + count + " in-game mute revokers!");
	}
	
	private void setupDiscordUnacceptedRevokers() {
		int count = 0;
		for(Member m : Main.getGuild().getMembers()) {
			if(m.getRoles().contains(Main.getGuild().getRoleById(673567494010568714L)) && !m.getRoles().contains(Main.getGuild().getRoleById(367283678323408896L))) {
				ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
					@Override
					public void run() {
						if(Main.getGuild().getMembers().contains(m) && m.getRoles().contains(Main.getGuild().getRoleById(673567494010568714L)) && !m.getRoles().contains(Main.getGuild().getRoleById(367283678323408896L))) {
							Main.getGuild().kick(m).queue();
						}
					}
				}, 172800, TimeUnit.SECONDS);
				count++;
			}
		}
		System.out.println("[ColonyModerationSystem] Successfully started " + count + " discord kickers!");
	}
	
	private void setupDiscordBanRevokers() {
		ResultSet rs = MainDatabase.getResultSet("SELECT * FROM DiscordBans;");
		int bans = 0;
		int mutes = 0;
		try {
			while(rs.next()) {
				long bannedUntil = rs.getLong("bannedUntil");
				long discordId = rs.getLong("discordId");
				if(bannedUntil < System.currentTimeMillis()) {
					MainDatabase.sendStatement("DELETE FROM DiscordBans WHERE discordId='" + rs.getLong("discordId") + "' AND bannedUntil=" + bannedUntil + ";");
					Main.getGuild().unban(Main.getJDA().getUserById(discordId)).queue();
				}
				else {
					if(rs.getString("type").equals("ban")) {
						new DiscordBan(new DiscordUser(discordId, MainDatabase.getDiscordTag(discordId)), Main.getJDA().getUserById(rs.getLong("staffId")), 
								rs.getString("reason"), rs.getLong("bannedUntil"), rs.getLong("issuedAt"), rs.getLong("messageId"));
						bans++;
					}
					else if(rs.getString("type").equals("mute")) {
						new DiscordMute(new DiscordUser(discordId, MainDatabase.getDiscordTag(discordId)), Main.getJDA().getUserById(rs.getLong("staffId")), 
								rs.getString("reason"), rs.getLong("bannedUntil"), rs.getLong("issuedAt"), rs.getLong("messageId"));
						mutes++;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("[ColonyModerationSystem] Successfully started " + bans + " discord ban revokers!");
		System.out.println("[ColonyModerationSystem] Successfully started " + mutes + " discord mute revokers!");
	}
	
	public static void writeToLog(String s) {
		try {
			FileWriter fw = new FileWriter(ProxyServer.getInstance().getPluginsFolder() + "/ColonyModerationSystem/log.txt", true);
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			fw.append("[ColonyModerationSystem] " + sdf.format(new Date(System.currentTimeMillis())) + " : " + s + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	private static void setInstance(Main instance) {
		Main.instance = instance;
	}
	
	public static User getUser(long userID) {
		return jda.getUserById(userID);
	}
	
	public static Guild getGuild() {
		Guild guild = jda.getGuildById(349836835670851584L);
		return guild;
	}
	
	public static Member getMember(User u) {
		Guild guild = jda.getGuildById(349836835670851584L);
		return guild.getMember(u);
	}
	
	public static void addRanksToPlayer(long userID, long rankID) {
		Guild guild = jda.getGuildById(349836835670851584L);
		Member m = guild.getMemberById(userID);
		guild.addRoleToMember(m, guild.getRoleById(rankID)).queue();
	}
	
	public static void removeRanksFromPlayer(long userID, long rankID) {
		Guild guild = jda.getGuildById(349836835670851584L);
		Member m = guild.getMemberById(userID);
		guild.removeRoleFromMember(m, guild.getRoleById(rankID)).queue();
	}

	public static TextChannel getChannel(long channelID) {
		return jda.getTextChannelById(channelID);
	}

	public static TextChannel getChannel(String channelID) {
		return jda.getTextChannelById(channelID);
	}
	
	public static JDA getJDA() {
		return jda;
	}
	
	public static LuckPerms getLuckPerms() {
		return luckPerms;
	}

}
