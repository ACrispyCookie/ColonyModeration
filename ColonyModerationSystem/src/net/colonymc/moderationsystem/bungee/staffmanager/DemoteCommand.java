package net.colonymc.moderationsystem.bungee.staffmanager;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class DemoteCommand extends Command {

	public DemoteCommand() {
		super("demote");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String invalid = ChatColor.translateAlternateColorCodes('&', "&fInvalid rank! Valid ranks: &dKNIGHT, PRINCE, KING, ARCHON, OVERLORD, COLONY, HELPER, MOD, ADMIN, MANAGER, OWNER");
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(p.hasPermission("*")) {
				if(args.length == 2) {
					if(!MainDatabase.getUuid(args[0]).equals("Not Found")) {
						String uuid = MainDatabase.getUuid(args[0]);
						if(Rank.valueOf(args[1]) != null) {
							if(StaffMember.getByUuid(uuid) != null) {
								if(StaffMember.getByUuid(uuid).getRank().ordinal() > Rank.valueOf(args[1]).ordinal()) {
									StaffMember.getByUuid(uuid).demote(Rank.valueOf(args[1]), p.getUniqueId().toString());
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYou demoted &d" + args[0] + " &fto &d" + Rank.valueOf(args[1]).name + "!")));
								}
								else {
									p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cThis player already has a lower or equal rank!")));
								}
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
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/demote <player> <rank>")));
				}
			}
			else {
				p.sendMessage(new TextComponent(Messages.noPerm));
			}
		}		
	}

}
