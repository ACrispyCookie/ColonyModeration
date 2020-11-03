package net.colonymc.moderationsystem.bungee.staffmanager;

import java.util.Comparator;

public class BStaffMemberComparator implements Comparator<BStaffMember> {

	final long start;
	final long end;
	
	public BStaffMemberComparator(long start, long end) {
		this.start = start;
		this.end = end;
	}
	
	@Override
	public int compare(BStaffMember a, BStaffMember b) {
		int aRating = a.calculateBetween(start, end);
		int bRating = b.calculateBetween(start, end);
		if(aRating > bRating) {
			return 1;
		}
		else if(aRating < bRating){
			return -1;
		}
		return 0;
	}

}
