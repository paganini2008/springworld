package com.github.paganini2008.springdessert.cluster.election;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;

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
