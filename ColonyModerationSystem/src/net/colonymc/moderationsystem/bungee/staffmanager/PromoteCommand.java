package net.colonymc.moderationsystem.bungee.staffmanager;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PromoteCommand extends Command {

	public PromoteCommand() {
		super("promote");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String invalid = ChatColor.translateAlternateColorCodes('&', "&fInvalid rank! Valid staff ranks: &dHELPER, MOD, ADMIN, MANAGER, OWNER");
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(p.hasPermission("*")) {
				if(args.length == 2) {
					if(!MainDatabase.getUuid(args[0]).equals("Not Found")) {
						String uuid = MainDatabase.getUuid(args[0]);
						if(Rank.valueOf(args[1].toUpperCase()) != null) {
							if(StaffMember.getByUuid(uuid) != null) {
								if(StaffMember.getByUuid(uuid).getRank().ordinal() >= Rank.valueOf(args[1].toUpperCase()).ordinal()) {
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis staff member already has this rank!")));
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou promoted &d" + args[0] + " &fto &d" + Rank.valueOf(args[1]).name + "!")));
								}
								else {
									StaffMember.getByUuid(uuid).promote(Rank.valueOf(args[1]), p.getUniqueId().toString());
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou added &d" + args[0] + " &fto the staff team!")));
								}
							}
							else if(args[1].equalsIgnoreCase("helper")) {
								StaffMember.addPlayer(uuid, p.getUniqueId().toString());
							}
							else {
								p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player is not a staff member!")));
							}
						}
						else {
							p.sendMessage(new TextComponent(invalid));
						}
					}
					else {
						p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player has never joined the server!")));
					}
				}
				else {
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/promote <player> <rank>")));
				}
			}
			else {
				p.sendMessage(new TextComponent(Messages.noPerm));
			}
		}
		else {
			sender.sendMessage(new TextComponent(Messages.onlyPlayers));
		}
	}

}
