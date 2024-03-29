package net.colonymc.colonymoderation.spigot.staffmanager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.colonymc.colonyspigotapi.api.itemstack.ItemStackBuilder;
import net.colonymc.colonyspigotapi.api.itemstack.SkullItemBuilder;
import net.colonymc.colonyapi.database.MainDatabase;
import net.colonymc.colonyapi.Time;
import net.colonymc.colonymoderation.bungee.staffmanager.BStaffMember;
import net.colonymc.colonymoderation.bungee.staffmanager.Rank;
import net.colonymc.colonymoderation.bungee.staffmanager.StaffAction;
import net.colonymc.colonymoderation.bungee.staffmanager.BStaffMember.FIXED_TIME;
import net.colonymc.colonymoderation.spigot.Main;
import net.colonymc.colonymoderation.spigot.staffmanager.utils.Feedback;
import net.colonymc.colonymoderation.spigot.staffmanager.utils.Feedback.FEEDBACK_TYPE;
import net.colonymc.colonymoderation.spigot.staffmanager.utils.Promotion;

public class StaffManagerPlayerMenu implements Listener, InventoryHolder {

	Player p;
	Inventory inv;
	BStaffMember b;
	InventoryHolder i;
	
	public StaffManagerPlayerMenu(Player p, BStaffMember b, InventoryHolder i) {
		this.p = p;
		this.b = b;
		this.i = i;
		this.inv = Bukkit.createInventory(this, 45, "Managing " + MainDatabase.getName(b.getUuid()) + "...");
		fillInventory();
	}
	
	public StaffManagerPlayerMenu() {
		
	}
	
	public void fillInventory() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String name = MainDatabase.getName(b.getUuid());
		String joinTimestamp = sdf.format(new Date(b.getJoin()));
		String leaveTimestamp = b.getLeave() == 0 ? "&dNever" : sdf.format(new Date(b.getLeave()));
		String rank = b.getRank().getName();
		ItemStack item = new SkullItemBuilder().playerUuid(UUID.fromString(b.getUuid())).name("&d" + name).lore(
				"\n&d&nInformation:" + 
				"\n &5» &fRank: &d" + rank + 
				"\n &5» &fJoined at: &d" + joinTimestamp + 
				"\n &5» &fLeft at: &d" + leaveTimestamp + 
				"\n " +
				"\n&d&nRatings:" + 
				"\n &5» &fDaily rating: &d" + b.calculateFixed(FIXED_TIME.DAILY) + 
				"\n &5» &fWeekly rating: &d" + b.calculateFixed(FIXED_TIME.WEEKLY) + 
				"\n &5» &fMonthly rating: &d" + b.calculateFixed(FIXED_TIME.MONTHLY) + 
				"\n " +
				(b.hasTitles() ? p.hasPermission("colonymc.staffmanager") ? "\n&d&nTitles:\n " + b.getFullTitles() + "\n " : "\n&5» &d&nTitles:\n " + b.getTitles() + "\n " : ""))
				.build();
		inv.setItem(13, item);
		if(b.isStaff()) {
			if(p.hasPermission("colonymc.staffmanager")) {
				if(b.getRank() != Rank.OWNER) {
					inv.setItem(14, new SkullItemBuilder().url("http://textures.minecraft.net/texture/7fce66789b77b261e669b239fcd4a6296965585ad01933f830de7a93de4374ce")
							.name("&dPromote staff member").lore("\n&fClick here to select a rank\n&fand promote this staff member!").build());
				}
				if(b.getRank() != Rank.KNIGHT) {
					inv.setItem(12, new SkullItemBuilder().url("http://textures.minecraft.net/texture/f9710ceae69b72a177e5abbb86c8ada432981ce7fc2d3ed1c651560f7a11e")
							.name("&dDemote staff member").lore("\n&fClick here to select a rank\n&fand demote this staff member!").build());
				}
			}
			b.loadAll();
			inv.setItem(20, banItem());
			inv.setItem(21, feedbackItem());
			inv.setItem(23, playtimeItem());
			inv.setItem(24, promotionsItem());
		}
		else {
			inv.setItem(14, new SkullItemBuilder().url("http://textures.minecraft.net/texture/7fce66789b77b261e669b239fcd4a6296965585ad01933f830de7a93de4374ce")
					.name("&dAdd to the staff team").lore("\n&fClick here to select a rank\n&fand add this player\n&fto the staff team!").build());
		}
		inv.setItem(40, new ItemStackBuilder(Material.ARROW).name("&dGo back").build());	
	}
	
	public void openInventory() {
		new BukkitRunnable() {
			@Override
			public void run() {
				p.openInventory(inv);
			}
		}.runTaskLater(Main.getInstance(), 1);
	}
	
	@Override
	public Inventory getInventory() {
		return inv;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getInventory() != null && e.getInventory().getType() != InventoryType.PLAYER) {
			if(e.getInventory().getHolder() instanceof StaffManagerPlayerMenu) {
				StaffManagerPlayerMenu m = (StaffManagerPlayerMenu) e.getInventory().getHolder();
				Player p = m.p;
				BStaffMember b = m.b;
				e.setCancelled(true);
				if(p.hasPermission("colonymc.staffmanager")) {
					if(e.getSlot() == 40) {
						p.closeInventory();
						p.openInventory(m.i.getInventory());
					}
					else if(e.getSlot() == 12) {
						if(b.getRank() != Rank.KNIGHT) {
							p.closeInventory();
							new SelectRankMenu(p, m.b, StaffAction.DEMOTE, m.i).openInventory();
						}
					}
					else if(e.getSlot() == 14) {
						if(b.getRank() != Rank.OWNER) {
							p.closeInventory();
							new SelectRankMenu(p, m.b, StaffAction.PROMOTE, m.i).openInventory();
						}
					}
				}
				else {
					if(e.getSlot() == 40) {
						p.closeInventory();
						new StaffManagerMenu(p).openInventory();
					}
				}
			}
		}
	}
	
	private ItemStack banItem() {
		Calendar daily = Calendar.getInstance();
		daily.set(Calendar.HOUR_OF_DAY, 0);
		daily.set(Calendar.MINUTE, 0);
		daily.set(Calendar.SECOND, 0);
		daily.set(Calendar.MILLISECOND, 0);
		Calendar weekly = Calendar.getInstance();
		weekly.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		weekly.set(Calendar.HOUR_OF_DAY, 0);
		weekly.set(Calendar.MINUTE, 0);
		weekly.set(Calendar.SECOND, 0);
		weekly.set(Calendar.MILLISECOND, 0);
		Calendar monthly = Calendar.getInstance();
		monthly.set(Calendar.DAY_OF_MONTH, 0);
		monthly.set(Calendar.HOUR_OF_DAY, 0);
		monthly.set(Calendar.MINUTE, 0);
		monthly.set(Calendar.SECOND, 0);
		monthly.set(Calendar.MILLISECOND, 0);
		return new ItemStackBuilder(Material.BOOK).name("&dPunishments executed")
				.lore("\n&d&nIngame punishments:"
						+ "\n &5» &fDaily: &d" + b.getPunishmentsAfter(daily.getTimeInMillis()).size()
						+ "\n &5» &fWeekly: &d" + b.getPunishmentsAfter(monthly.getTimeInMillis()).size() 
						+ "\n &5» &fMonthly: &d" + b.getPunishmentsAfter(weekly.getTimeInMillis()).size() 
						+ "\n &5» &fTotal: &d" + b.getPunishments().size() 
						+ "\n "
						+ "\n&d&nDiscord punishments:"
						+ "\n &5» &fDaily: &d" + b.getDiscordPunishmentsAfter(daily.getTimeInMillis()).size()
						+ "\n &5» &fWeekly: &d" + b.getDiscordPunishmentsAfter(monthly.getTimeInMillis()).size() 
						+ "\n &5» &fMonthly: &d" + b.getDiscordPunishmentsAfter(weekly.getTimeInMillis()).size() 
						+ "\n &5» &fTotal: &d" + b.getDiscordPunishments().size()
						+ "\n "
						+ "\n&d&nIngame reports closed:"
						+ "\n &5» &fDaily: &d" + b.getReportsAfter(daily.getTimeInMillis()).size()
						+ "\n &5» &fWeekly: &d" + b.getReportsAfter(monthly.getTimeInMillis()).size() 
						+ "\n &5» &fMonthly: &d" + b.getReportsAfter(weekly.getTimeInMillis()).size() 
						+ "\n &5» &fTotal: &d" + b.getReports().size())
				.build();
	}

	private ItemStack playtimeItem() {
		Calendar d = Calendar.getInstance();
		d.set(Calendar.HOUR_OF_DAY, 0);
		d.set(Calendar.MINUTE, 0);
		d.set(Calendar.SECOND, 0);
		d.set(Calendar.MILLISECOND, 0);
		Calendar yD = Calendar.getInstance();
		yD.set(Calendar.DAY_OF_MONTH, yD.get(Calendar.DAY_OF_MONTH) - 1);
		yD.set(Calendar.HOUR_OF_DAY, 0);
		yD.set(Calendar.MINUTE, 0);
		yD.set(Calendar.SECOND, 0);
		yD.set(Calendar.MILLISECOND, 0);
		Calendar w = Calendar.getInstance();
		w.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		w.set(Calendar.HOUR_OF_DAY, 0);
		w.set(Calendar.MINUTE, 0);
		w.set(Calendar.SECOND, 0);
		w.set(Calendar.MILLISECOND, 0);
		Calendar lW = Calendar.getInstance();
		lW.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		lW.set(Calendar.WEEK_OF_MONTH, lW.get(Calendar.WEEK_OF_MONTH) - 1);
		lW.set(Calendar.HOUR_OF_DAY, 0);
		lW.set(Calendar.MINUTE, 0);
		lW.set(Calendar.SECOND, 0);
		lW.set(Calendar.MILLISECOND, 0);
		Calendar m = Calendar.getInstance();
		m.set(Calendar.DAY_OF_MONTH, 0);
		m.set(Calendar.HOUR_OF_DAY, 0);
		m.set(Calendar.MINUTE, 0);
		m.set(Calendar.SECOND, 0);
		m.set(Calendar.MILLISECOND, 0);
		Calendar lM = Calendar.getInstance();
		lM.set(Calendar.DAY_OF_MONTH, 0);
		lM.add(Calendar.MONTH, -1);
		lM.set(Calendar.HOUR_OF_DAY, 0);
		lM.set(Calendar.MINUTE, 0);
		lM.set(Calendar.SECOND, 0);
		lM.set(Calendar.MILLISECOND, 0);
		int day = b.getPlaytimeBetween(d.getTimeInMillis(), System.currentTimeMillis());
		int yesterday = b.getPlaytimeBetween(yD.getTimeInMillis(), d.getTimeInMillis());
		int week = b.getPlaytimeBetween(w.getTimeInMillis(), System.currentTimeMillis());
		int lastWeek = b.getPlaytimeBetween(lW.getTimeInMillis(), w.getTimeInMillis());
		int month = b.getPlaytimeBetween(m.getTimeInMillis(), System.currentTimeMillis());
		int lastMonth = b.getPlaytimeBetween(lM.getTimeInMillis(), m.getTimeInMillis());
		int total = b.getPlaytimeBetween(0, System.currentTimeMillis());
		double dy = (((double) day/yesterday) - 1) * 100;
		double wlw = (((double) week/lastWeek) - 1) * 100;
		double mlm = (((double) month/lastMonth) - 1) * 100;
		DecimalFormat dd = new DecimalFormat("#.##");
		return new ItemStackBuilder(Material.WATCH).name("&dPlaytime")
				.lore("\n&fDaily playtime: &d" + Time.formatted(day) + 
						"\n&7(Yesterday: &d" + Time.formatted(yesterday) + "&7) " 
						+ (day != 0 && yesterday != 0 ? (day > yesterday ? "&a(+" + dd.format(dy) + "%)" : (day == yesterday ? "&e(+0%)" : "&c(" + dd.format(dy) + "%)")) : "") +
						"\n \n&fWeekly playtime: &d" + Time.formatted(week) + 
						"\n&7(Last week: &d" + Time.formatted(lastWeek) + "&7) " 
						+ (week != 0 && lastWeek != 0 ? (week > lastWeek ? "&a(+" + dd.format(wlw) + "%)" : (week == lastWeek ? "&e(+0%)" : "&c(" + dd.format(wlw) + "%)")) : "") +
						"\n \n&fMonthly playtime: &d" + Time.formatted(month) + 
						"\n&7(Last month: &d" + Time.formatted(lastMonth) + "&7) " 
						+ (month != 0 && lastMonth != 0 ? (month > lastMonth ? "&a(+" + dd.format(mlm) + "%)" : (month == lastMonth ? "&e(+0%)" : "&c(" + dd.format(mlm) + "%)")) : "") +
						"\n \n&fTotal playtime: &d" + Time.formatted(total))
				.build();
	}
	
	private ItemStack promotionsItem() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		StringBuilder loreProm = new StringBuilder();
		StringBuilder loreDem = new StringBuilder(b.getDemotions().size() > 0 ? "\n&d&nDemotions:" : "");
		for(Promotion m : b.getPromotions()) {
			loreProm.append("\n &5» &fPromotion at &d").append(sdf.format(new Date(m.getTimestamp()))).append("\n    &fRank after: &").append(m.getRankAfter().getChatColor()).append(m.getRankAfter().getName()).append("\n    &fAction by: &d").append(MainDatabase.getName(m.getByUuid())).append("\n");
		}
		for(Promotion m : b.getDemotions()) {
			loreDem.append("\n &5» &fDemotion at &d").append(sdf.format(new Date(m.getTimestamp()))).append("\n    &fRank after: &").append(m.getRankAfter().getChatColor()).append(m.getRankAfter().getName()).append("\n    &fAction by: &d").append(MainDatabase.getName(m.getByUuid())).append("\n");
		}
		return new ItemStackBuilder(Material.DIAMOND).name("&dPromotions/Demotions")
				.lore("\n&d&nPromotions:" + loreProm + loreDem)
				.build();
	}
	
	private ItemStack feedbackItem() {
		DecimalFormat d = new DecimalFormat("#.##");
		return new ItemStackBuilder(Material.PAPER)
				.name("&dPlayer feedback")
				.lore("\n&d&nToday:"
						+ "\n &5» &fFair: " + getStars((int) Feedback.getFromArray(b.getDailyF(), FEEDBACK_TYPE.FAIR)) + " &7(" + d.format(Feedback.getFromArray(b.getDailyF(), FEEDBACK_TYPE.FAIR)) + "/5)"
						+ "\n &5» &fHelpful: " + getStars((int) Feedback.getFromArray(b.getDailyF(), FEEDBACK_TYPE.HELPFUL)) + " &7(" + d.format(Feedback.getFromArray(b.getDailyF(), FEEDBACK_TYPE.HELPFUL)) + "/5)"
						+ "\n &5» &fFriendly: " + getStars((int) Feedback.getFromArray(b.getDailyF(), FEEDBACK_TYPE.FRIENDLY)) + " &7(" + d.format(Feedback.getFromArray(b.getDailyF(), FEEDBACK_TYPE.FRIENDLY)) + "/5)"
						+ "\n &5» &fActive: " + getStars((int) Feedback.getFromArray(b.getDailyF(), FEEDBACK_TYPE.ACTIVE)) + " &7(" + d.format(Feedback.getFromArray(b.getDailyF(), FEEDBACK_TYPE.ACTIVE)) + "/5)"
					 + "\n \n&d&nThis week:"
						+ "\n &5» &fFair: " + getStars((int) Feedback.getFromArray(b.getWeeklyF(), FEEDBACK_TYPE.FAIR)) + " &7(" + d.format(Feedback.getFromArray(b.getWeeklyF(), FEEDBACK_TYPE.FAIR)) + "/5)"
						+ "\n &5» &fHelpful: " + getStars((int) Feedback.getFromArray(b.getWeeklyF(), FEEDBACK_TYPE.HELPFUL)) + " &7(" + d.format(Feedback.getFromArray(b.getWeeklyF(), FEEDBACK_TYPE.HELPFUL)) + "/5)"
						+ "\n &5» &fFriendly: " + getStars((int) Feedback.getFromArray(b.getWeeklyF(), FEEDBACK_TYPE.FRIENDLY)) + " &7(" + d.format(Feedback.getFromArray(b.getWeeklyF(), FEEDBACK_TYPE.FRIENDLY)) + "/5)"
						+ "\n &5» &fActive: " + getStars((int) Feedback.getFromArray(b.getWeeklyF(), FEEDBACK_TYPE.ACTIVE)) + " &7(" + d.format(Feedback.getFromArray(b.getWeeklyF(), FEEDBACK_TYPE.ACTIVE)) + "/5)"
					+ "\n \n&d&nThis month:"
						+ "\n &5» &fFair: " + getStars((int) Feedback.getFromArray(b.getMonthlyF(), FEEDBACK_TYPE.FAIR)) + " &7(" + d.format(Feedback.getFromArray(b.getMonthlyF(), FEEDBACK_TYPE.FAIR)) + "/5)"
						+ "\n &5» &fHelpful: " + getStars((int) Feedback.getFromArray(b.getMonthlyF(), FEEDBACK_TYPE.HELPFUL)) + " &7(" + d.format(Feedback.getFromArray(b.getMonthlyF(), FEEDBACK_TYPE.HELPFUL)) + "/5)"
						+ "\n &5» &fFriendly: " + getStars((int) Feedback.getFromArray(b.getMonthlyF(), FEEDBACK_TYPE.FRIENDLY)) + " &7(" + d.format(Feedback.getFromArray(b.getMonthlyF(), FEEDBACK_TYPE.FRIENDLY)) + "/5)"
						+ "\n &5» &fActive: " + getStars((int) Feedback.getFromArray(b.getMonthlyF(), FEEDBACK_TYPE.ACTIVE)) + " &7(" + d.format(Feedback.getFromArray(b.getMonthlyF(), FEEDBACK_TYPE.ACTIVE)) + "/5)"
					+ "\n \n&d&nTotal:"
						+ "\n &5» &fFair: " + getStars((int) Feedback.getFromArray(b.getFeedback(), FEEDBACK_TYPE.FAIR)) + " &7(" + d.format(Feedback.getFromArray(b.getFeedback(), FEEDBACK_TYPE.FAIR)) + "/5)"
						+ "\n &5» &fHelpful: " + getStars((int) Feedback.getFromArray(b.getFeedback(), FEEDBACK_TYPE.HELPFUL)) + " &7(" + d.format(Feedback.getFromArray(b.getFeedback(), FEEDBACK_TYPE.HELPFUL)) + "/5)"
						+ "\n &5» &fFriendly: " + getStars((int) Feedback.getFromArray(b.getFeedback(), FEEDBACK_TYPE.FRIENDLY)) + " &7(" + d.format(Feedback.getFromArray(b.getFeedback(), FEEDBACK_TYPE.FRIENDLY)) + "/5)"
						+ "\n &5» &fActive: " + getStars((int) Feedback.getFromArray(b.getFeedback(), FEEDBACK_TYPE.ACTIVE)) + " &7(" + d.format(Feedback.getFromArray(b.getFeedback(), FEEDBACK_TYPE.ACTIVE)) + "/5)")
				.build();
	}
	
	private String getStars(int stars) {
		if(stars >= 5) {
			return "&d⭐⭐⭐⭐⭐";
		}
		else if(stars <= 0) {
			return "&7⭐⭐⭐⭐⭐";
		}
		else {
			String s = "&d⭐⭐⭐⭐⭐";
			s = s.substring(0, 2 + stars) + "&7" + s.substring(2 + stars);
			return s;
		}
	}
	
}
