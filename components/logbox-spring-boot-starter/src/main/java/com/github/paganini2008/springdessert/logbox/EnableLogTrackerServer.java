package com.github.paganini2008.springdessert.logbox;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.github.paganini2008.springdessert.cluster.EnableApplicationCluster;
import com.github.paganini2008.springdessert.logbox.ui.LogBoxUIAutoConfiguration;
import com.github.paganini2008.springdessert.xtransport.EnableXTransport;

/**
 * 
 * EnableLogTrackerServer
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableXTransport
@EnableApplicationCluster(enableLeaderElection = true, enableMonitor = true)
@Import({ LogBoxAutoConfiguration.class, LogBoxUIAutoConfiguration.class })
public @interface EnableLogTrackerServer {
}
