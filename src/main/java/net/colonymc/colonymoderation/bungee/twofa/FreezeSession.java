package net.colonymc.colonymoderation.bungee.twofa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonymoderation.bungee.Main;
import net.colonymc.colonymoderation.bungee.SpigotConnector;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

public class FreezeSession extends ListenerAdapter implements Listener {
	
	LinkedPlayer l;
	ProxiedPlayer p;
	ServerInfo s;
	int timeLeft;
	ScheduledTask task;
	static final ArrayList<FreezeSession> sessions = new ArrayList<>();
	
	public FreezeSession(LinkedPlayer p, ServerInfo s) {
		this.l = p;
		this.timeLeft = 60;
		this.s = s;
		sendLinkedFreeze();
		task = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), this::cancel, timeLeft, TimeUnit.SECONDS);
		sessions.add(this);
	}
	
	public FreezeSession(ProxiedPlayer p, ServerInfo s) {
		this.p = p;
		this.timeLeft = 60;
		this.s = s;
		sendUnlinkedFreeze();
		task = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), this::cancel, timeLeft, TimeUnit.SECONDS);
		sessions.add(this);
	}
	
	public FreezeSession() {
	}
	
	public void sendLinkedFreeze() {
		SpigotConnector.freezeStaff(s, l.getPlayer().getName());
	}
	
	public void sendUnlinkedFreeze() {
		SpigotConnector.freezeUnlinkedStaff(s, p.getName());
	}
	
	public void cancel() {
		task.cancel();
		sessions.remove(this);
		if(l != null) {
			if(l.getPlayer().isConnected()) {
				l.getPlayer().disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&5» &cFailed to verify through discord! Please try again!")));
			}
		}
		else {
			if(p.isConnected()) {
				p.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&5» &cFailed to verify through discord! Please try again!")));
			}
		}
	}
	
	public void onVerify() {
		task.cancel();
		sessions.remove(this);
		if(l != null) {
			VerifiedPlayer.verified.add(new VerifiedPlayer(l, l.getPlayer().getAddress().getHostString()));
			SpigotConnector.unfreezeStaff(s, l.getPlayerUuid());
		}
		else {
			VerifiedPlayer.verified.add(new VerifiedPlayer(LinkedPlayer.getByPlayer(p), p.getAddress().getHostString()));
			SpigotConnector.unfreezeStaff(s, p.getUniqueId().toString());
		}
	}
	
	public static FreezeSession getByPlayer(LinkedPlayer p) {
		for(FreezeSession f : sessions) {
			if(f.l != null && f.l.equals(p)) {
				return f;
			}
		}
		return null;
	}
	
	public static FreezeSession getByUnlinkedPlayer(ProxiedPlayer p) {
		for(FreezeSession f : sessions) {
			if(f.l == null && f.p.equals(p)) {
				return f;
			}
		}
		return null;
	}
	
	@EventHandler
	public void onChat(ChatEvent e) {
		if(e.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) e.getSender();
			if(getByPlayer(LinkedPlayer.getByPlayer(p)) != null) {
				e.setCancelled(true);
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cPlease verify yourself through discord in order to use the chat!")));
			}
			else if(getByUnlinkedPlayer(p) != null) {
				e.setCancelled(true);
				p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &5&l» &cPlease link yourself through discord in order to use the chat!")));
			}
		}
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent e) {
		try {
			ResultSet rs = MainDatabase.getResultSet("SELECT * FROM ActiveReactions WHERE messageID=" + e.getMessageIdLong() + ";");
			if(rs.next() && rs.getString("messageType").equals("verification")) {
				if(e.getReaction().getEmoji().asUnicode().getName().equals("✅")) {
					if(VerifiedPlayer.getByLinkedPlayer(LinkedPlayer.getByUser(e.getUser())) == null) {
						e.getReaction().removeReaction(e.getUser()).queue();
						if(FreezeSession.getByPlayer(LinkedPlayer.getByUser(e.getUser())) != null) {
							FreezeSession.getByPlayer(LinkedPlayer.getByUser(e.getUser())).onVerify();
						}
					}
					else if(e.getUser() != Main.getJDA().getSelfUser()){
						e.getReaction().removeReaction(e.getUser()).queue();
					}
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}
