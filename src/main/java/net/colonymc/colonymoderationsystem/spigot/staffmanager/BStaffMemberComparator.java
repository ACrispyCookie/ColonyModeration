package net.colonymc.colonymoderationsystem.spigot.staffmanager;

import java.util.Comparator;

import net.colonymc.colonymoderationsystem.bungee.staffmanager.BStaffMember;
import net.colonymc.colonymoderationsystem.bungee.staffmanager.Rank;

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
            return Integer.compare(r2.ordinal(), r1.ordinal());
		}
	}

}
