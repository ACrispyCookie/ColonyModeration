package net.colonymc.moderationsystem.spigot.staffmanager;

import java.util.Comparator;

import net.colonymc.moderationsystem.bungee.staffmanager.BStaffMember;
import net.colonymc.moderationsystem.bungee.staffmanager.Rank;

public class BStaffMemberComparator implements Comparator<BStaffMember> {

	@Override
	public int compare(BStaffMember p1, BStaffMember p2) {
		if(p1.isStaff() && !p2.isStaff()) {
			return -1;
		}
		else if(!p1.isStaff() && p2.isStaff()) {
			return 1;
		}
		else {
			Rank r1 = p1.getRank();
			Rank r2 = p2.getRank();
			if(r1.ordinal() > r2.ordinal()) {
				return -1;
			}
			else if(r1.ordinal() < r2.ordinal()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

}
