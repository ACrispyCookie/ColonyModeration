package net.colonymc.moderationsystem.bungee.twofa;

import net.colonymc.moderationsystem.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class McLinkCommand extends Command {

	public McLinkCommand() {
		super("link");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(LinkedPlayer.getByPlayer(p) != null) {
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYour account is already linked to &d" + LinkedPlayer.getByPlayer(p).getUser().getAsTag() + "&f! You can do &d/unlink &fto unlink it!")));
			}
			else {
				if(args.length == 2) {
					if(isLong(args[0])) {
						if(!LinkRequest.getRequestByUserID(Long.parseLong(args[0])).isEmpty()) {
							LinkRequest request = LinkRequest.getByUserID(Long.parseLong(args[0]), p);
							if(Long.parseLong(args[0]) == request.discordUser.getIdLong() && args[1].equals(request.token)) {
								request.accept();
								if(FreezeSession.getByUnlinkedPlayer(p) != null) {
									FreezeSession.getByUnlinkedPlayer(p).onVerify();
								}
							}
							else {
								request.reject();
							}
						}
						else {
							p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fThis request has either expired or never existed!")));
						}
					}
					else {
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fPlease type a valid user Discord ID!")));
					}
				}
				else {
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fType &d!link " + p.getName() + " &fin our discord to link your account!")));
				}
			}
		}
		else {
			sender.sendMessage(new TextComponent(Messages.onlyPlayers));
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
