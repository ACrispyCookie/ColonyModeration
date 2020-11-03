package net.colonymc.colonymoderationsystem.bungee.reports;

import net.colonymc.colonymoderationsystem.Messages;
import net.colonymc.colonymoderationsystem.bungee.SpigotConnector;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ArchivedReportsCommand extends Command {

	public ArchivedReportsCommand() {
		super("archivedreports");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(p.hasPermission("*")) {
				SpigotConnector.openArchivedReportsMenu(p.getServer().getInfo(), p.getName());
			}
			else {
				sender.sendMessage(new TextComponent(Messages.noPerm));
			}
		}
		else {
			sender.sendMessage(new TextComponent(Messages.onlyPlayers));
		}
	}

}
