package com.github.paganini2008.springdessert.cluster;

/**
 * 
 * LeaderRecovery
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface LeaderRecovery {

	void recover(ApplicationInfo formerLeader);

}
