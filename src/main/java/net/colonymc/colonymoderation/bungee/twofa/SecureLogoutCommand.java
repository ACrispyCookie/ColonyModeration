package net.colonymc.colonymoderation.bungee.twofa;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SecureLogoutCommand extends Command{

	public SecureLogoutCommand() {
		super("securelogout");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(p.hasPermission("staff.store")) {
				if(VerifiedPlayer.getByLinkedPlayer(LinkedPlayer.getByPlayer(p)) != null) {
					VerifiedPlayer.getByLinkedPlayer(LinkedPlayer.getByPlayer(p)).remove();
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fSecurely logging you out...")));
					p.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5» &cYou securely logged out!\n&cAny new connection will require a verification!")));
				}
			}
			else {
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cYou cannot execute this command.")));
			}
		}
		else {
			sender.sendMessage(new TextComponent(" Only players can use this command!"));
		}
	}

}
