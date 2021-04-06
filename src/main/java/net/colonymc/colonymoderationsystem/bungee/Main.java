package net.colonymc.colonymoderationsystem.bungee;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderationsystem.bungee.bans.Ban;
import net.colonymc.colonymoderationsystem.bungee.bans.BanCommand;
import net.colonymc.colonymoderationsystem.bungee.bans.CheckCommand;
import net.colonymc.colonymoderationsystem.bungee.bans.JoinListener;
import net.colonymc.colonymoderationsystem.bungee.bans.KickCommand;
import net.colonymc.colonymoderationsystem.bungee.bans.MassBanCommand;
import net.colonymc.colonymoderationsystem.bungee.bans.Mute;
import net.colonymc.colonymoderationsystem.bungee.bans.UnbanCommand;
import net.colonymc.colonymoderationsystem.bungee.bans.UnmuteCommand;
import net.colonymc.colonymoderationsystem.bungee.chatmoderation.ChatCommand;
import net.colonymc.colonymoderationsystem.bungee.chatmoderation.ChatListener;
import net.colonymc.colonymoderationsystem.bungee.discord.DiscordBan;
import net.colonymc.colonymoderationsystem.bungee.discord.DiscordListeners;
import net.colonymc.colonymoderationsystem.bungee.discord.DiscordMute;
import net.colonymc.colonymoderationsystem.bungee.discord.DiscordPunishCommand;
import net.colonymc.colonymoderationsystem.bungee.discord.DiscordUser;
import net.colonymc.colonymoderationsystem.bungee.discord.RoleCommand;
import net.colonymc.colonymoderationsystem.bungee.feedback.FeedbackCommand;
import net.colonymc.colonymoderationsystem.bungee.feedback.FeedbackManager;
import net.colonymc.colonymoderationsystem.bungee.queue.LeaveQueueCommand;
import net.colonymc.colonymoderationsystem.bungee.queue.Queue;
import net.colonymc.colonymoderationsystem.bungee.queue.QueueCommand;
import net.colonymc.colonymoderationsystem.bungee.reports.ArchivedReportsCommand;
import net.colonymc.colonymoderationsystem.bungee.reports.Report;
import net.colonymc.colonymoderationsystem.bungee.reports.ReportCommand;
import net.colonymc.colonymoderationsystem.bungee.reports.ReportsCommand;
import net.colonymc.colonymoderationsystem.bungee.staffmanager.*;
import net.colonymc.colonymoderationsystem.bungee.twofa.FreezeSession;
import net.colonymc.colonymoderationsystem.bungee.twofa.LinkCommand;
import net.colonymc.colonymoderationsystem.bungee.twofa.LinkedPlayer;
import net.colonymc.colonymoderationsystem.bungee.twofa.McLinkCommand;
import net.colonymc.colonymoderationsystem.bungee.twofa.SecureLogoutCommand;
import net.colonymc.colonymoderationsystem.bungee.twofa.UnlinkCommand;
import net.colonymc.colonymoderationsystem.bungee.twofa.VerifiedPlayer;
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
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Main extends Plugin {
 	
    private File logfile;
	private static LuckPerms luckPerms;
	private static Main instance;
	private static FeedbackManager feedback;
	private static TopStaffAnnouncer topAnnouncer;
	private static Configuration feedbackConfig;
	private static JDA jda;
	boolean started = false;
	
	public void onEnable() {
		setInstance(this);
		setupDiscordBot();
		loadNonDatabase();
		if(MainDatabase.isConnected()) {
			loadDatabaseRequired();
			loadLinkedPlayers();
			setupRevokers();
			setupTopStaffAnnouncer();
			setupFeedback();
			logfile = new File(ProxyServer.getInstance().getPluginsFolder() + "/ColonyModerationSystem/log.txt");
			luckPerms = LuckPermsProvider.get();
			if(!logfile.exists()) {
				createNewLogFile();
			}
			loadQueues();
			BStaffMember.startLoadingStaff();
			started = true;
			System.out.println("[ColonyModerationSystem] has been enabled successfully!");
		}
		else {
			System.out.println("[ColonyModerationSystem] Couldn't connect to the databases!");
		}
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
			jda = JDABuilder.createDefault("bot-token").build();
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
		getProxy().registerChannel("ManagerChannel");
		getProxy().registerChannel("FeedbackChannel");
		getProxy().registerChannel("QueueChannel");
		//listeners setup
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
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new KickCommand());
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
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new StaffManagerCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new FeedbackCommand());
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
				ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
					if(Main.getGuild().getMembers().contains(m) && m.getRoles().contains(Main.getGuild().getRoleById(673567494010568714L)) && !m.getRoles().contains(Main.getGuild().getRoleById(367283678323408896L))) {
						Main.getGuild().kick(m).queue();
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
					Main.getGuild().unban(Main.getUser(discordId)).queue();
				}
				else {
					if(rs.getString("type").equals("ban")) {
						new DiscordBan(new DiscordUser(discordId, MainDatabase.getDiscordTag(discordId)), Main.getUser(rs.getLong("staffId")),
								rs.getString("reason"), rs.getLong("bannedUntil"), rs.getLong("issuedAt"), rs.getLong("messageId"));
						bans++;
					}
					else if(rs.getString("type").equals("mute")) {
						new DiscordMute(new DiscordUser(discordId, MainDatabase.getDiscordTag(discordId)), Main.getUser(rs.getLong("staffId")),
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
	
	private void setupFeedback() {
		try {
			if (!getDataFolder().exists()) {
	            getDataFolder().mkdir();
			}
	        File file = new File(getDataFolder(), "surveys.yml");
	        if (!file.exists()) {
	            try (InputStream in = getResourceAsStream("surveys.yml")) {
	                Files.copy(in, file.toPath());
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
			feedbackConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "surveys.yml"));
			feedback = new FeedbackManager(feedbackConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setupTopStaffAnnouncer() {
		topAnnouncer = new TopStaffAnnouncer();
	}
	
	public static void writeToLog(String s) {
		try {
			FileWriter fw = new FileWriter(ProxyServer.getInstance().getPluginsFolder() + "/ColonyModerationSystem/log.txt", true);
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			fw.append("[ColonyModerationSystem] ").append(sdf.format(new Date(System.currentTimeMillis()))).append(" : ").append(s).append("\n");
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
	
	public static TopStaffAnnouncer getAnnouncer() {
		return topAnnouncer;
	}
	
	public static FeedbackManager getFeedback() {
		return feedback;
	}
	
	public static User getUser(long userID) {
		return jda.retrieveUserById(userID).complete();
	}
	
	public static Guild getGuild() {
		return jda.getGuildById(349836835670851584L);
	}
	
	public static Member getMember(User u) {
		Guild guild = jda.getGuildById(349836835670851584L);
		return guild.retrieveMember(u).complete();
	}
	
	public static void addRanksToPlayer(long userID, long rankID) {
		Guild guild = jda.getGuildById(349836835670851584L);
		Member m = guild.retrieveMemberById(userID).complete();
		guild.addRoleToMember(m, guild.getRoleById(rankID)).queue();
	}
	
	public static void removeRanksFromPlayer(long userID, long rankID) {
		Guild guild = jda.getGuildById(349836835670851584L);
		Member m = guild.retrieveMemberById(userID).complete();
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
