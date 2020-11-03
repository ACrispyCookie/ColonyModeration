package net.colonymc.colonymoderationsystem.bungee.staffmanager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.colonymoderationsystem.bungee.Main;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class TopStaffAnnouncer {
	
	ScheduledTask day;
	ScheduledTask week;
	ScheduledTask month;
	
	public TopStaffAnnouncer() {
		start();
	}
	
	private void start() {
		startDay();
		startWeek();
		startMonth();
	}
	
	private void startDay() {
		Calendar da = Calendar.getInstance();
		da.set(Calendar.HOUR_OF_DAY, 0);
		da.set(Calendar.MINUTE, 0);
		da.set(Calendar.SECOND, 0);
		da.set(Calendar.MILLISECOND, 0);
		da.add(Calendar.DATE, 1);
		MainDatabase.sendStatement("UPDATE NextTopStaff SET nextDay=" + da.getTimeInMillis() + ";");
		day = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
			decideDay(da.getTimeInMillis());
			startDay();
		}, (da.getTimeInMillis() - System.currentTimeMillis() - 10000) / 1000, TimeUnit.SECONDS);
	}
	
	private void startWeek() {
		Calendar we = Calendar.getInstance();
		we.add(Calendar.DATE, 7);
		we.set(Calendar.HOUR_OF_DAY, 0);
		we.set(Calendar.MINUTE, 0);
		we.set(Calendar.SECOND, 0);
		we.set(Calendar.MILLISECOND, 0);
		we.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		MainDatabase.sendStatement("UPDATE NextTopStaff SET nextWeek=" + we.getTimeInMillis() + ";");
		week = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
			decideWeek(we.getTimeInMillis());
			startWeek();
		}, (we.getTimeInMillis() - System.currentTimeMillis() - 10000) / 1000, TimeUnit.SECONDS);
	}
	
	private void startMonth() {
		Calendar mo = Calendar.getInstance();
		mo.set(Calendar.DAY_OF_MONTH, 0);
		mo.set(Calendar.HOUR_OF_DAY, 0);
		mo.set(Calendar.MINUTE, 0);
		mo.set(Calendar.SECOND, 0);
		mo.set(Calendar.MILLISECOND, 0);
		mo.add(Calendar.MONTH, 1);
		MainDatabase.sendStatement("UPDATE NextTopStaff SET nextMonth=" + mo.getTimeInMillis() + ";");
		month = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
			decideMonth(mo.getTimeInMillis());
			startMonth();
		}, (mo.getTimeInMillis() - System.currentTimeMillis() - 10000) / 1000, TimeUnit.SECONDS);
	}
	
	private void decideDay(long day) {
		BStaffMember.loadStaff();
		BStaffMember staff = null;
		BStaffMember wStaff = null;
		int tOvr = 0;
		int wOvr = 0;
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.calculateBetween(day - 86400000, day);
			if(staff == null || ovr > staff.calculateBetween(day, day)) {
				staff = m;
				tOvr = ovr;
			}
		}
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.calculateBetween(day - 86400000, day);
			if(wStaff == null || ovr < wStaff.calculateBetween(day - 86400000, day)) {
				wStaff = m;
				wOvr = ovr;
			}
		}
		if(tOvr > 0) {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET daily='" + staff.getUuid() + "';");
		}
		else {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET daily=NULL;");
		}
		if(wOvr != tOvr) {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET wDaily='" + wStaff.getUuid() + "';");
		}
		else {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET wDaily=NULL;");
		}
	}
	
	private void decideWeek(long week) {
		BStaffMember.loadStaff();
		BStaffMember staff = null;
		BStaffMember wStaff = null;
		int tOvr = 0;
		int wOvr = 0;
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.calculateBetween(week - 604800000, week);
			if(staff == null || ovr > staff.calculateBetween(week - 604800000, week)) {
				staff = m;
				tOvr = ovr;
			}
		}
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.calculateBetween(week - 604800000, week);
			if(wStaff == null || ovr < wStaff.calculateBetween(week - 604800000, week)) {
				wStaff = m;
				wOvr = ovr;
			}
		}
		if(tOvr > 0) {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET weekly='" + staff.getUuid() + "';");
		}
		else {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET weekly=NULL;");
		}
		if(tOvr != wOvr) {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET wWeekly='" + wStaff.getUuid() + "';");
		}
		else {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET wWeekly=NULL;");
		}
	}
	
	private void decideMonth(long month) {
		BStaffMember.loadStaff();
		BStaffMember staff = null;
		BStaffMember wStaff = null;
		Calendar previousMonth = Calendar.getInstance();
		previousMonth.setTimeInMillis(month);
		previousMonth.add(Calendar.MONTH, -1);
		int tOvr = 0;
		int wOvr = 0;
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.calculateBetween(previousMonth.getTimeInMillis(), month);
			if(staff == null || ovr > staff.calculateBetween(previousMonth.getTimeInMillis(), month)) {
				staff = m;
				tOvr = ovr;
			}
		}
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.calculateBetween(previousMonth.getTimeInMillis(), month);
			if(wStaff == null || ovr < wStaff.calculateBetween(previousMonth.getTimeInMillis(), month)) {
				wStaff = m;
				wOvr = ovr;
			}
		}
		if(tOvr > 0) {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET monthly='" + staff.getUuid() + "';");
		}
		else {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET monthly=NULL;");
		}
		if(tOvr != wOvr) {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET wMonthly='" + wStaff.getUuid() + "';");
		}
		else {
			MainDatabase.sendStatement("UPDATE NextTopStaff SET wMonthly=NULL;");
		}
	}

}
