package net.colonymc.colonymoderation.bungee.bans;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderation.Messages;
import net.colonymc.colonymoderation.bungee.Main;
import net.colonymc.colonymoderation.bungee.SpigotConnector;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BanCommand extends Command implements TabExecutor  {

	public BanCommand() {
		super("ban", "", "tempban", "mute", "tempmute", "ipban", "banip");
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		Set<String> matches = new HashSet<>();
		String search = args[0].toLowerCase();
		for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            if(p.getName().toLowerCase().startsWith(search.toLowerCase())) {
        		matches.add(p.getName());
            }
		}
		return matches;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender.hasPermission("staff.store")) {
			if(sender instanceof ProxiedPlayer) {
				ProxiedPlayer p = (ProxiedPlayer) sender;
				if(args.length == 0) {
					SpigotConnector.openBanMenu(p.getServer().getInfo(), p.getName(), "");
				}
				else if(args.length == 1) {
					if(args[0].length() <= 16) {
						if(!MainDatabase.getUuid(args[0]).equals("Not Found")) {
							if(ProxyServer.getInstance().getPlayer(args[0]) != null && ProxyServer.getInstance().getPlayer(args[0]).equals(p)) {
								p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot punish yourself!")));
							}
							else {
								if(!MainDatabase.isBanned(args[0])) {
								    UserManager userManager = Main.getLuckPerms().getUserManager();
								    CompletableFuture<User> userFuture = userManager.loadUser(UUID.fromString(MainDatabase.getUuid(args[0])));
								    userFuture.thenAcceptAsync(user -> {
								    	if(p.hasPermission("*") || (!user.getPrimaryGroup().equals("admin") && !user.getPrimaryGroup().equals("manager") && !user.getPrimaryGroup().equals("owner"))) {
											SpigotConnector.openBanMenu(p.getServer().getInfo(), p.getName(), MainDatabase.getUuid(args[0]));
										}
										else {
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot punish this player!")));
										}
								    });
								}
								else {
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is already banned!")));
								}
							}
						}
						else {
							p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player has never joined the server before!")));
						}
					}
					else {
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player does not exist!")));
					}
				}
				else {
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/ban [player]")));
				}
			}
			else if(args.length == 2 && isInt(args[1]) && Integer.parseInt(args[1]) >= 1 && Integer.parseInt(args[1]) <= 6) {
				if(!MainDatabase.getUuid(args[0]).equals("Not Found")) {
					new Punishment(args[0], sender, chooseReason(Integer.parseInt(args[1])), -1).execute();
				}
				else {
					sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " » This player has never joined the network!")));
				}
			}
			else if(args.length == 3 && isLong(args[2])) {
				if(!MainDatabase.getUuid(args[0]).equals("Not Found")) {
					new Punishment(args[0], sender, args[1], Long.parseLong(args[2]), null, -1).execute();
				}
				else {
					sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " » This player has never joined the network!")));
				}
			}
			else {
				sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
						" » Usage: /ban <name> <reason/reason ID> [time in seconds]"
						+ "\nReason IDs:\n » Cheating - 1\n » Offensive Language - 2\n » Negative Behaviour - 3\n » Advertsing - 4\n » Spamming - 5\n » Bug abusing - 6")));
			}
		}
		else {
			sender.sendMessage(new TextComponent(Messages.noPerm));
		}
	}
	
	private String chooseReason(int reason) {
		switch(reason) {
		case 1:
			return "Cheating - Hacking";
		case 2:
			return "Offensive Language";
		case 3:
			return "Negative Behaviour";
		case 4:
			return "Advertising";
		case 5:
			return "Spamming";
		case 6:
			return "Bug Abusing";
		}
		return "";
	}

	private boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	private boolean isLong(String s) {
		try {
			Long.parseLong(s);
			return true;
		} catch(Exception e) {
			return false;
		}
	}

}
