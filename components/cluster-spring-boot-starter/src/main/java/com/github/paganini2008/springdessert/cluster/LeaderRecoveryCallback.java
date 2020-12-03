package com.github.paganini2008.springdessert.cluster;

/**
 * 
 * LeaderRecoveryCallback
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface LeaderRecoveryCallback {

	void recover(ApplicationInfo leaderInfo);

}
