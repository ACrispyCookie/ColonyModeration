package net.colonymc.moderationsystem.bungee.chatmoderation;

import java.util.ArrayList;
import java.util.HashMap;

import net.colonymc.moderationsystem.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ChatCommand extends Command{

	public ChatCommand() {
		super("chat");
	}

	final TextComponent clearChat = new TextComponent("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" +
			"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + 
			"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
	public static final ArrayList<String> toggled = new ArrayList<>();
	public static final HashMap<String, Integer> slow = new HashMap<>();
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(p.hasPermission("staff.store")) {
				switch(args.length) {
				case 1:
					switch (args[0]) {
						case "clear":
							clearChat(p.getServer().getInfo(), p.getName());
							p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou &dcleared &fthe chat for the server: &d" + p.getServer().getInfo().getName() + "&f!")));
							break;
						case "toggle":
							if (!toggled.contains("all")) {
								if (!toggled.contains(p.getServer().getInfo().getName())) {
									sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been &cdisabled &fby &d" + p.getName() + "&f!");
									toggled.add(p.getServer().getInfo().getName());
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &cdisabled &fthe chat for the server: &d" + p.getServer().getInfo().getName() + "&f!")));
								} else {
									sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been &aenabled &fby &d" + p.getName() + "&f!");
									toggled.remove(p.getServer().getInfo().getName());
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &aenabled &fthe chat for the server: &d" + p.getServer().getInfo().getName() + "&f!")));
								}
							} else {
								p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe chat is currently disabled in the whole network! Do /chat toggle all to enable it!")));
							}
							break;
						case "slow":
							p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/chat <toggle, clear, slow> [duration for slow mode] [server]")));
							break;
					}
					break;
				case 2:
					switch (args[0]) {
						case "clear":
							if (args[1].isEmpty()) {
								clearChat(p.getServer().getInfo(), p.getName());
								p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou &dcleared &fthe chat for the server: &d" + p.getServer().getInfo().getName() + "&f!")));
							} else if (ProxyServer.getInstance().getServerInfo(args[1]) != null) {
								if (ProxyServer.getInstance().getServerInfo(args[1]) == p.getServer().getInfo()) {
									clearChat(p.getServer().getInfo(), p.getName());
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou &dcleared &fthe chat for the server: &d" + p.getServer().getInfo().getName() + "&f!")));
								} else if (p.hasPermission("*")) {
									clearChat(ProxyServer.getInstance().getServerInfo(args[1]), p.getName());
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou &dcleared &fthe chat for the server: &d" + args[1] + "&f!")));
								} else {
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fOnly admins can remotely moderate the chat of other servers!")));
								}
							} else if (args[1].equals("all")) {
								if (p.hasPermission("*")) {
									for (ProxiedPlayer target : ProxyServer.getInstance().getPlayers()) {
										if (!target.hasPermission("staff.store")) {
											target.sendMessage(clearChat);
										}
									}
									ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe chat has been cleared &fby &d" + p.getName() + " &ffor the whole network!")));
								} else {
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fOnly admins can moderate the global chat of the network!")));
								}
							} else {
								ArrayList<String> servers = new ArrayList<>();
								for (ServerInfo s : ProxyServer.getInstance().getServers().values()) {
									servers.add(s.getName());
								}
								servers.add("all");
								sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlease enter a valid server: &d" + servers.toString())));
							}
							break;
						case "toggle":
							if (args[1].isEmpty()) {
								if (!toggled.contains("all")) {
									if (!toggled.contains(p.getServer().getInfo().getName())) {
										sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been &cdisabled &fby &d" + p.getName() + "&f!");
										toggled.add(p.getServer().getInfo().getName());
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &cdisabled &fthe chat for the server: &d" + p.getServer().getInfo().getName() + "&f!")));
									} else {
										sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been &aenabled &fby &d" + p.getName() + "&f!");
										toggled.remove(p.getServer().getInfo().getName());
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &aenabled &fthe chat for the server: &d" + p.getServer().getInfo().getName() + "&f!")));
									}
								} else {
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe chat is currently disabled in the whole network! Do /chat toggle all to enable it!")));
								}
							} else if (ProxyServer.getInstance().getServerInfo(args[1]) != null) {
								if (!toggled.contains("all")) {
									if (ProxyServer.getInstance().getServerInfo(args[1]) == p.getServer().getInfo()) {
										if (!toggled.contains(p.getServer().getInfo().getName())) {
											sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been &cdisabled &fby &d" + p.getName() + "&f!");
											toggled.add(p.getServer().getInfo().getName());
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &cdisabled &fthe chat for the server: &d" + p.getServer().getInfo().getName() + "&f!")));
										} else {
											sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been &aenabled &fby &d" + p.getName() + "&f!");
											toggled.remove(p.getServer().getInfo().getName());
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &aenabled &fthe chat for the server: &d" + p.getServer().getInfo().getName() + "&f!")));
										}
									} else if (p.hasPermission("*")) {
										if (!toggled.contains(args[1])) {
											sendMessage(ProxyServer.getInstance().getServerInfo(args[1]), " &5&l» &fThe chat has been &cdisabled &fby &d" + p.getName() + "&f!");
											toggled.add(args[1]);
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &cdisabled &fthe chat for the server: &d" + args[1] + "&f!")));
										} else {
											sendMessage(ProxyServer.getInstance().getServerInfo(args[1]), " &5&l» &fThe chat has been &aenabled &fby &d" + p.getName() + "&f!");
											toggled.remove(args[1]);
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &aenabled &fthe chat for the server: &d" + args[1] + "&f!")));
										}
									} else {
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fOnly admins can remotely moderate the chat of other servers!")));
									}
								} else {
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe chat is currently disabled in the whole network! Do /chat toggle all to enable it!")));
								}
							} else if (args[1].equals("all")) {
								if (p.hasPermission("*")) {
									if (!toggled.contains("all")) {
										ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe chat has been &cdisabled &fby &d" + p.getName() + " &ffor the whole network!")));
										toggled.clear();
										toggled.add("all");
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &cdisabled &fthe chat for the whole network!")));
									} else {
										ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe chat has been &aenabled &fby &d" + p.getName() + " &ffor the whole network!")));
										toggled.remove("all");
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &aenabled &fthe chat for the whole network!")));
									}
								} else {
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fOnly admins can moderate the global chat of the network!")));
								}
							} else {
								ArrayList<String> servers = new ArrayList<>();
								for (ServerInfo s : ProxyServer.getInstance().getServers().values()) {
									servers.add(s.getName());
								}
								servers.add("all");
								sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlease enter a valid server: &d" + servers.toString())));
							}
							break;
						case "slow":
							if (!slow.containsKey("all")) {
								if (isInteger(args[1])) {
									if (slow.containsKey(p.getServer().getInfo().getName())) {
										if (Integer.parseInt(args[1]) != slow.get(p.getServer().getInfo().getName())) {
											sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been slowed &fby &d" + p.getName() + "&f! Time between messages: &d" + args[1] + " seconds&f.");
											slow.put(p.getServer().getInfo().getName(), Integer.parseInt(args[1]));
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have slowed the chat for the server &d" + p.getServer().getInfo().getName() + " &fby &d" + args[1] + " seconds&f!")));
										} else {
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe chat is already slowed by &d" + args[1] + " seconds&f!")));
										}
									} else {
										sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been slowed &fby &d" + p.getName() + "&f! Time between messages: &d" + args[1] + " seconds&f.");
										slow.put(p.getServer().getInfo().getName(), Integer.parseInt(args[1]));
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have slowed the chat for the server &d" + p.getServer().getInfo().getName() + " &fby &d" + args[1] + " seconds&f!")));
									}
								} else if (args[1].equals("off")) {
									if (slow.containsKey(p.getServer().getInfo().getName())) {
										sendMessage(p.getServer().getInfo(), " &5&l» &fThe slow chat mode &fhas been &cdisabled &fby &d" + p.getName() + "&f!");
										slow.put(p.getServer().getInfo().getName(), -1);
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &cdisabled &fthe slow chat mode for the server &d" + p.getServer().getInfo().getName() + "&f!")));
									} else {
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe slow chat mode is already &cdisabled&f!")));
									}
								} else {
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlease enter a valid number!")));
								}
							} else {
								p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe chat is currently slowed in the whole network! Do /chat slow off all to enable it!")));
							}
							break;
					}
					break;
				case 3:
					if(args[0].equals("slow")) {
						if(args[2].isEmpty()) {
							if(!slow.containsKey("all")) {
								if(isInteger(args[1])) {
									if(slow.containsKey(p.getServer().getInfo().getName())) {
										if(Integer.parseInt(args[1]) != slow.get(p.getServer().getInfo().getName())) {
											sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been slowed by &d" + p.getName() + "&f! Time between messages: &d" + args[1] + " seconds&f.");
											slow.put(p.getServer().getInfo().getName(), Integer.parseInt(args[1]));
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have slowed the chat for the server &d" + p.getServer().getInfo().getName() + " &fby &d" + args[1] + " seconds&f!")));
										}
										else {
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe chat is already slowed by &d" + args[1] + " seconds&f!")));
										}
									}
									else {
										sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been slowed by &d" + p.getName() + "&f! Time between messages: &d" + args[1] + " seconds&f.");
										slow.put(p.getServer().getInfo().getName(), Integer.parseInt(args[1]));
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have slowed the chat for the server &d" + p.getServer().getInfo().getName() + " &fby &d" + args[1] + " seconds&f!")));
									}
								}
								else if(args[1].equals("off")) {
									if(slow.containsKey(args[2])) {
										sendMessage(ProxyServer.getInstance().getServerInfo(args[2]), " &5&l» &fThe slow chat mode has been &cdisabled &fby &d" + p.getName() + "&f!");
										slow.put(p.getServer().getInfo().getName(), -1);
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &cdisabled &fthe slow chat mode for the server: &d" + p.getServer().getInfo().getName() + "&f!")));
									}
									else {
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe slow chat mode is already &cdisabled&f!")));
									}
								}
								else {
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlease enter a valid number!")));
								}
							}
							else {
								p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe chat is currently slowed in the whole network! Do /chat slow off all to enable it!")));
							}
						}
						else if(ProxyServer.getInstance().getServerInfo(args[2]) != null) {
							if(!slow.containsKey("all")) {
								if(ProxyServer.getInstance().getServerInfo(args[2]) == p.getServer().getInfo()) {
									if(isInteger(args[1])) {
										if(slow.containsKey(p.getServer().getInfo().getName())) {
											if(Integer.parseInt(args[1]) != slow.get(p.getServer().getInfo().getName())) {
												sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been slowed by &d" + p.getName() + "&f! Time between messages: &d" + args[1] + " seconds&f.");
												slow.put(p.getServer().getInfo().getName(), Integer.parseInt(args[1]));
												p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have slowed the chat for the server &d" + p.getServer().getInfo().getName() + " &fby &d" + args[1] + " seconds&f!")));
											}
											else {
												p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe chat is already slowed by &d" + args[1] + " seconds&f!")));
											}
										}
										else {
											sendMessage(p.getServer().getInfo(), " &5&l» &fThe chat has been slowed by &d" + p.getName() + "&f! Time between messages: &d" + args[1] + " seconds&f.");
											slow.put(p.getServer().getInfo().getName(), Integer.parseInt(args[1]));
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have slowed the chat for the server &d" + p.getServer().getInfo().getName() + " &fby &d" + args[1] + " seconds&f!")));
										}
									}
									else if(args[1].equals("off")) {
										if(p.hasPermission("*")) {
											if(slow.containsKey(args[2])) {
												sendMessage(ProxyServer.getInstance().getServerInfo(args[2]), " &5&l» &fThe slow chat mode has been &cdisabled &fby &d" + p.getName() + "&f!");
												slow.put(p.getServer().getInfo().getName(), -1);
												p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &cdisabled &fthe slow chat mode for the server &d" + p.getServer().getInfo().getName() + "&f!")));
											}
											else {
												p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe slow chat mode is already &cdisabled&f!")));
											}
										}
										else {
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fOnly admins can remotely moderate the chat of other servers!")));
										}
									}
									else {
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlease enter a valid number!")));
									}
								}
								else if(p.hasPermission("*")) {
									if(isInteger(args[1])) {
										if(slow.containsKey(args[2])) {
											if(Integer.parseInt(args[1]) != slow.get(args[2])) {
												sendMessage(ProxyServer.getInstance().getServerInfo(args[2]), " &5&l» &fThe chat has been slowed by &d" + p.getName() + "&f! Time between messages: &d" + args[1] + " seconds&f.");
												slow.put(args[2], Integer.parseInt(args[1]));
												p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have slowed the chat for the server &d" + args[2] + " &fby &d" + args[1] + " seconds&f!")));
											}
											else {
												p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe chat is already slowed by &d" + args[1] + " seconds&f!")));
											}
										}
										else {
											sendMessage(ProxyServer.getInstance().getServerInfo(args[2]), " &5&l» &fThe chat has been slowed by &d" + p.getName() + "&f! Time between messages: &d" + args[1] + " seconds&f.");
											slow.put(args[2], Integer.parseInt(args[1]));
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have slowed the chat for the server &d" + args[2] + " &fby &d" + args[1] + " seconds&f!")));
										}
									}
									else if(args[1].equals("off")) {
										if(p.hasPermission("*")) {
											if(slow.containsKey(args[2])) {
												sendMessage(ProxyServer.getInstance().getServerInfo(args[2]), " &5&l» &fThe slow chat mode has been &cdisabled &fby &d" + p.getName() + "&f!");
												slow.put(args[2], -1);
												p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou have &cdisabled &fthe slow chat mode for the server &d" + args[2] + "&f!")));
											}
											else {
												p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe slow chat mode is already &cdisabled&f!")));
											}
										}
										else {
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fOnly admins can remotely moderate the chat of other servers!")));
										}
									}
									else {
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlease enter a valid number!")));
									}
								}
								else {
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fOnly admins can remotely moderate the chat of other servers!")));
								}
							}
							else {
								p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThe chat is currently slowed in the whole network! Do /chat slow off all to enable it!")));
							}
						}
						else if(args[2].equals("all")) {
							if(p.hasPermission("*")) {
								if(isInteger(args[1])) {
									if(slow.containsKey(args[2])) {
										if(Integer.parseInt(args[1]) != slow.get(args[2])) {
											slow.clear();
											slow.put("all", Integer.parseInt(args[1]));
											ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&',
													" &5&l» &fThe chat has been slowed by &d" + p.getName() + " &ffor the whole network! Time between messages: &d" + args[1] + " seconds&f!")));
										}
										else {
											p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe chat is already slowed by &d" + args[1] + " seconds&f!")));
										}
									}
									else {
										slow.clear();
										slow.put("all", Integer.parseInt(args[1]));
										ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&',
												" &5&l» &fThe chat has been slowed by &d" + p.getName() + " &ffor the whole network! Time between messages: &d" + args[1] + " seconds&f!")));
									}
								}
								else if(args[1].equals("off")) {
									if(slow.containsKey("all")) {
										slow.remove("all");
										ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&',
												" &5&l» &fThe slow chat mode has been &cdisabled &fby &d" + p.getName() + " &ffor the whole network!")));
									}
									else {
										p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &&fThe slow chat mode is already &cdisabled&f!")));
									}
								}
							}
							else {
								p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fOnly admins can moderate the global chat of the network!")));
							}
						}
						else {
							ArrayList<String> servers = new ArrayList<>();
							for(ServerInfo s : ProxyServer.getInstance().getServers().values()) {
								servers.add(s.getName());
							}
							servers.add("all");
							sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlease enter a valid server: &d" + servers.toString())));
						}
					}
					else {
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/chat <toggle, clear, slow> [duration for slow mode] [server]")));
					}
					break;
				default:
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/chat <toggle, clear, slow> [duration for slow mode] [server]")));
				}
			}
			else {
				p.sendMessage(new TextComponent(Messages.noPerm));
			}
		}
		else {
			
		}
	}

	private void sendMessage(ServerInfo s, String msg) {
		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			if(p.getServer().getInfo().equals(s)) {
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', msg)));
			}
		}
	}
	
	private void clearChat(ServerInfo serverInfo, String name) {
		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			if(p.getServer().getInfo().equals(serverInfo)) {
				p.sendMessage(clearChat);
			}
		}
		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			if(p.getServer().getInfo().equals(serverInfo)) {
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThe chat has been cleared by &d" + name + "&f!")));
			}
		}
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException | NullPointerException e) {
	        return false; 
	    }
		return true;
	}

}
