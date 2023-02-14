package net.colonymc.colonymoderation.bungee.twofa;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderation.bungee.Main;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class UnlinkCommand extends Command {

	public UnlinkCommand() {
		super("unlink");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(LinkedPlayer.getByPlayer(p) != null) {
				MainDatabase.sendStatement("DELETE FROM VerifiedPlayers WHERE userDiscordID =" + LinkedPlayer.getByPlayer(p).getUserId() + ";");
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYour account has been unlinked from the profile &d" + LinkedPlayer.getByPlayer(p).getUser().getAsTag() + "&f!")));
				Member m = Main.getMember(Main.getUser(LinkedPlayer.getByPlayer(p).getUserId()));
				Main.removeRanksFromPlayer(LinkedPlayer.getByPlayer(p).getUserId(), 599345400062672896L);
				if(m.getRoles().contains(m.getGuild().getRoleById(349847789506789379L))) {
					Main.removeRanksFromPlayer(LinkedPlayer.getByPlayer(p).getUserId(), 349847789506789379L);
				}
				LinkedPlayer.linked.remove(LinkedPlayer.getByPlayer(p));
				m.modifyNickname("").queue();
			}
			else {
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fYour account is not linked! To link it type &d!link " + p.getName() + " &fin our &ddiscord server&f!")));
			}
		}
		else {
			sender.sendMessage(new TextComponent("Only players can use this command!"));
		}
	}

}
