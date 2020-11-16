package net.colonymc.colonymoderationsystem.bungee.bans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonyapi.Time;
import net.colonymc.colonymoderationsystem.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CheckCommand extends Command implements TabExecutor {

	public CheckCommand() {
		super("check");
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		Set<String> matches = new HashSet<>();
		String search = args[0].toLowerCase();
		for (ProxiedPlayer m : ProxyServer.getInstance().getPlayers()) {
            if(m.getName().toLowerCase().startsWith(search.toLowerCase())) {
        		matches.add(m.getName());
            }
		}
		return matches;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender.hasPermission("staff.store")) {
			if(args.length == 1) {
				if(args[0].startsWith("#")) {
					try {
						ResultSet rs = MainDatabase.getResultSet("SELECT * FROM ActiveBans WHERE ID='" + args[0].substring(1) + "';");
						if(rs.next()) {
							String playerName = MainDatabase.getName(rs.getString("uuid"));
							String uuid = rs.getString("uuid");
							String staff = rs.getString("staff");
							String bannedAt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(rs.getLong("issuedAt")));
							String reason = rs.getString("reason");
							long bannedUntilFinal = rs.getLong("bannedUntil") - System.currentTimeMillis();
							String ID = rs.getString("ID");
							String bannedForAnother = Time.formatted(bannedUntilFinal/1000);
							TextComponent finalText = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&f&m------------------&r &d#" + ID + " ban &f&m------------------"));
							finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fPlayer's Name: \n    &d" + playerName));
							finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fPlayer's UUID: \n    &d" + uuid));
							finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fBan Reason: \n    &d" + reason));
							finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fBanned by: \n    &d" + staff));
							finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fBanned at: \n    &d" + bannedAt));
							finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fBan expires in: \n    &d" + bannedForAnother));
							finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fBan ID: \n    &d#" + ID));
							finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n&f&m-----------------------------------------------"));
							sender.sendMessage(finalText);
						}
						else {
							ResultSet mute = MainDatabase.getResultSet("SELECT * FROM ActiveMutes WHERE ID='" + args[0].substring(1) + "';");
							if(mute.next()) {
								String playerName = MainDatabase.getName(rs.getString("uuid"));
								String uuid = mute.getString("uuid");
								String staff = mute.getString("staff");
								String reason = mute.getString("reason");
								String bannedAt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(mute.getLong("issuedAt")));
								long bannedUntilFinal = mute.getLong("mutedUntil") - System.currentTimeMillis();
								String ID = mute.getString("ID");
								String bannedForAnother = Time.formatted(bannedUntilFinal/1000);
								TextComponent finalText = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&f&m------------------&r &d#" + ID + " mute &f&m------------------"));
								finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fPlayer's Name: \n    &d" + playerName));
								finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fPlayer's UUID: \n    &d" + uuid));
								finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fMute Reason: \n    &d" + reason));
								finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fMuted by: \n    &d" + staff));
								finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fMuted at: \n    &d" + bannedAt));
								finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fMute expires in: \n    &d" + bannedForAnother));
								finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n\n &5&l» &fMute ID: \n    &d#" + ID));
								finalText.addExtra(ChatColor.translateAlternateColorCodes('&', "\n&f&m-----------------------------------------------"));
								sender.sendMessage(finalText);
							}
							else {
								sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis punishment ID does not exist!")));
							}
						}
					} catch (SQLException e) {
						e.printStackTrace();
	    				sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error has occured please report it to the admins!")));
					}
				}
				else if(args[0].length() >= 3 && args[0].length() <= 16) {
					try {
						ResultSet rs = MainDatabase.getResultSet("SELECT * FROM PlayerInfo WHERE name='" + args[0] + "';");
						if(rs.next()) {
							Date firstJoin = new Date(rs.getLong("firstJoinTime"));
							Date lastJoin = new Date(rs.getLong("lastJoinTime"));
							SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
							String lastJoinTime;
							if(rs.getLong("lastJoinTime") == 0) {
								lastJoinTime = ChatColor.translateAlternateColorCodes('&', "&cNot Found");
							}
							else {
								lastJoinTime = sdf.format(lastJoin);
							}
							String playerUuid = rs.getString("uuid");
							String ip = rs.getString("ip");
							String country = ChatColor.translateAlternateColorCodes('&', "&cNot Found");
							String firstJoinTime = sdf.format(firstJoin);
							String timesBanned = String.valueOf(rs.getInt("timesBanned"));
							String timesMuted = String.valueOf(rs.getInt("timesMuted"));
							String isBanned = Ban.getByUuid(playerUuid) != null ? ChatColor.translateAlternateColorCodes('&', "&aYes") : ChatColor.translateAlternateColorCodes('&', "&cNo");
							String isMuted = Mute.getByUuid(playerUuid) != null ? ChatColor.translateAlternateColorCodes('&', "&aYes") : ChatColor.translateAlternateColorCodes('&', "&cNo");
							String isBanEvading = Ban.getByIp(ip) != null && Ban.getByUuid(playerUuid) == null ? ChatColor.translateAlternateColorCodes('&', "&aYes") : ChatColor.translateAlternateColorCodes('&', "&cNo");
							String isOnline = ProxyServer.getInstance().getPlayer(args[0]) != null ? ChatColor.translateAlternateColorCodes('&', "&aOnline") : ChatColor.translateAlternateColorCodes('&', "&cOffline");
							String currentServer = ProxyServer.getInstance().getPlayer(args[0]) != null ? ProxyServer.getInstance().getPlayer(args[0]).getServer().getInfo().getName() : null;
							String banReason = Ban.getByUuid(playerUuid) != null ? Ban.getByUuid(playerUuid).getReason() : null;
							String banModerator = Ban.getByUuid(playerUuid) != null ? Ban.getByUuid(playerUuid).getStaff() : null;
							String bannedUntil = Ban.getByUuid(playerUuid) != null ? sdf.format(new Date(Ban.getByUuid(playerUuid).getBannedUntil())) : null;
							String banID = Ban.getByUuid(playerUuid) != null ? Ban.getByUuid(playerUuid).getId() : null;
							String muteReason = Mute.getByUuid(playerUuid) != null ? Mute.getByUuid(playerUuid).getReason() : null;
							String muteModerator = Mute.getByUuid(playerUuid) != null ? Mute.getByUuid(playerUuid).getStaff() : null;
							String mutedUntil = Mute.getByUuid(playerUuid) != null ? sdf.format(new Date(Mute.getByUuid(playerUuid).getMutedUntil())) : null;
							String muteID = Mute.getByUuid(playerUuid) != null ? Mute.getByUuid(playerUuid).getId() : null;
							String banEvadingName = Ban.getByIp(ip) != null && Ban.getByUuid(playerUuid) == null ? Ban.getByIp(ip).getPlayerName() : null;
							String banEvadingUuid = Ban.getByIp(ip) != null && Ban.getByUuid(playerUuid) == null ? Ban.getByIp(ip).getUuid() : null;
							ArrayList<String> alts = new ArrayList<>();
							rs = MainDatabase.getResultSet("SELECT * FROM PlayerInfo WHERE ip='" + ip + "';");
							while(rs.next()) {
								if(!rs.getString("uuid").equals(playerUuid)) {
									alts.add(rs.getString("name"));
								}
							}
							TextComponent header;
							TextComponent footer = new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n\n&m-------------------------------------------------"));
							if(isBanned.equals(ChatColor.translateAlternateColorCodes('&', "&aYes"))) {
								header = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&m------------------&r &d" + MainDatabase.getName(playerUuid) + " &c(Banned) &f&m------------------\n"));
							}
							else {
								header = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&m------------------&r &d" + MainDatabase.getName(playerUuid) + " &f&m------------------\n"));
							}
							header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fStatus: " + isOnline)));
							if(isOnline.equals(ChatColor.translateAlternateColorCodes('&', "&aOnline"))) {
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fCurrent Server: &d" + currentServer)));
							}
							header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fPlayer's UUID: &d" + playerUuid)));
							header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fPlayer's IP: &d" + ip)));
							header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fPlayer's Country: &d" + country)));
							header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fFirst Joined: &d" + firstJoinTime)));
							header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fLast Joined: &d" + lastJoinTime)));
							header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fTimes Banned: &d" + timesBanned)));
							header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fTimes Muted: &d" + timesMuted)));
							header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fBan evading: " + isBanEvading)));
							if(isBanEvading.equals(ChatColor.translateAlternateColorCodes('&', "&aYes"))) {
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l»   - &fBanned account name: &d" + banEvadingName)));
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l»   - &fBanned account uuid: &d" + banEvadingUuid)));
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l»   - &fBanned account IP: &d" + ip)));
							}
							header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fBanned: " + isBanned)));
							if(isBanned.equals(ChatColor.translateAlternateColorCodes('&', "&aYes"))) {
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l»   - &fBan reason: &d" + banReason)));
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l»   - &fPunisher: &d" + banModerator)));
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l»   - &fBanned Until: &d" + bannedUntil)));
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l»   - &fBan ID: &d" + banID)));
							}
							header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fMuted: " + isMuted)));
							if(isMuted.equals(ChatColor.translateAlternateColorCodes('&', "&aYes"))) {
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l»   - &fMute reason: &d" + muteReason)));
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l»   - &fPunisher: &d" + muteModerator)));
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l»   - &fMuted Until: &d" + mutedUntil)));
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l»   - &fMute ID: &d" + muteID)));
							}
							if(!alts.isEmpty()) {
								header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fAlts: &d[")));
								for(int i = 0; i < alts.size(); i++) {
									if(i + 1 == alts.size()) {
										header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&d" + alts.get(i) + "]")));
									}
									else {
										header.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&d" + alts.get(i) + ",")));
									}
								}
							}
							if(MainDatabase.isStaff(args[0])) {
								TextComponent text = new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n \n &5&l» &dClick to perform a staff check on this player!"));
								text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/staffmanager " + args[0]));
								header.addExtra(text);
								
							}
							header.addExtra(footer);
							sender.sendMessage(header);
							
						}
						else {
							sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player has never joined the server!")));
						}
					} catch (SQLException e) {
						e.printStackTrace();
						sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cAn error had occured, please report it to an administator!")));
					}
				}
				else {
					sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/check #<Ban ID> OR <playerName>")));
				}
			}
			else {
				sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/check #<Ban ID> OR <playerName>")));
			}
		}
		else {
			sender.sendMessage(new TextComponent(Messages.noPerm));
		}
	}
}
