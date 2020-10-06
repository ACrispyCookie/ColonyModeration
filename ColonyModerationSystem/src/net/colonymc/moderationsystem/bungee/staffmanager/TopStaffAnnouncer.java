package net.colonymc.moderationsystem.bungee.staffmanager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import net.colonymc.colonyapi.MainDatabase;
import net.colonymc.moderationsystem.bungee.Main;
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
		day = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				decideDay();
				startDay();
			}
		}, (da.getTimeInMillis() - System.currentTimeMillis()) / 1000, TimeUnit.SECONDS);
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
		week = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				decideWeek();
				startWeek();
			}
		}, (we.getTimeInMillis() - System.currentTimeMillis()) / 1000, TimeUnit.SECONDS);
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
		month = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				decideMonth();
				startMonth();
			}
		}, (mo.getTimeInMillis() - System.currentTimeMillis()) / 1000, TimeUnit.SECONDS);
	}
	
	private void decideDay() {
		BStaffMember.loadStaff();
		BStaffMember staff = null;
		BStaffMember wStaff = null;
		int tOvr = 0;
		int wOvr = 0;
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.getDailyOvr();
			if(staff == null || ovr > staff.getDailyOvr()) {
				staff = m;
				tOvr = ovr;
			}
		}
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.getDailyOvr();
			if(wStaff == null || ovr < wStaff.getDailyOvr()) {
				wStaff = m;
				wOvr = ovr;
			}
		}
		MainDatabase.sendStatement("UPDATE NextTopStaff SET daily='" + staff.getUuid() + "';");
		MainDatabase.sendStatement("UPDATE NextTopStaff SET wDaily='" + wStaff.getUuid() + "';");
		MainDatabase.sendStatement("UPDATE NextTopStaff SET dP=" + tOvr + ";");
		MainDatabase.sendStatement("UPDATE NextTopStaff SET wdP=" + wOvr + ";");
	}
	
	private void decideWeek() {
		BStaffMember.loadStaff();
		BStaffMember staff = null;
		BStaffMember wStaff = null;
		int tOvr = 0;
		int wOvr = 0;
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.getDailyOvr();
			if(staff == null || ovr > staff.getDailyOvr()) {
				staff = m;
				tOvr = ovr;
			}
		}
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.getDailyOvr();
			if(wStaff == null || ovr < wStaff.getDailyOvr()) {
				wStaff = m;
				wOvr = ovr;
			}
		}
		MainDatabase.sendStatement("UPDATE NextTopStaff SET weekly='" + staff.getUuid() + "';");
		MainDatabase.sendStatement("UPDATE NextTopStaff SET wWeekly='" + wStaff.getUuid() + "';");
		MainDatabase.sendStatement("UPDATE NextTopStaff SET wP=" + tOvr + ";");
		MainDatabase.sendStatement("UPDATE NextTopStaff SET wwP=" + wOvr + ";");
	}
	
	private void decideMonth() {
		BStaffMember.loadStaff();
		BStaffMember staff = null;
		BStaffMember wStaff = null;
		int tOvr = 0;
		int wOvr = 0;
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.getMonthlyOvr();
			if(staff == null || ovr > staff.getMonthlyOvr()) {
				staff = m;
				tOvr = ovr;
			}
		}
		for(BStaffMember m : BStaffMember.getStaff()) {
			if(m.getUuid().equals("37c3bfb6-6fa9-4602-a9bd-a1e95baea85f")) {
				continue;
			}
			int ovr = m.getDailyOvr();
			if(wStaff == null || ovr < wStaff.getDailyOvr()) {
				wStaff = m;
				wOvr = ovr;
			}
		}
		MainDatabase.sendStatement("UPDATE NextTopStaff SET monthly='" + staff.getUuid() + "';");
		MainDatabase.sendStatement("UPDATE NextTopStaff SET wMonthly='" + wStaff.getUuid() + "';");
		MainDatabase.sendStatement("UPDATE NextTopStaff SET mP=" + tOvr + ";");
		MainDatabase.sendStatement("UPDATE NextTopStaff SET wmP=" + wOvr + ";");
	}

}
