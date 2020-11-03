package net.colonymc.colonymoderationsystem.bungee.feedback;

import net.colonymc.colonymoderationsystem.bungee.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class FeedbackCommand extends Command {

	public FeedbackCommand() {
		super("feedback");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if(args.length == 0) {
				TextComponent ask = new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n &5&l» &fWould you like to answer a random feedback survey?\n &5&l» &f(Reward may be available)"));
				TextComponent proceed = new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n \n &5&l» &d&lClick HERE to start!\n "));
				proceed.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/feedback new"));
				ask.addExtra(proceed);
				p.sendMessage(ask);
			}
			else if(args.length == 1) {
				if(args[0].equalsIgnoreCase("new")) {
					Main.getFeedback().pickQuestion(p);
				}
				else {
					p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/feedback [new]")));
				}
			}
			else {
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &fUsage: &d/feedback [new]")));
			}
		}
		else {
			sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cOnly players can execute this command!")));
		}
	}

}
