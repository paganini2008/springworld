package com.github.paganini2008.springdessert.cluster;

/**
 * 
 * LeaderRecoveryCallback
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface LeaderRecoveryCallback {

	void recover(ApplicationInfo leaderInfo);

}
