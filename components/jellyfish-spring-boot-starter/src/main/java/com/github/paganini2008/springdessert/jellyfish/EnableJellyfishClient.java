package com.github.paganini2008.springdessert.jellyfish;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.github.paganini2008.springdessert.cluster.EnableApplicationCluster;
import com.github.paganini2008.springdessert.jellyfish.ui.JellyfishUIAutoConfiguration;
import com.github.paganini2008.springdessert.xtransport.EnableXTransport;

/**
 * 
 * EnableJellyfishClient
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableXTransport
@EnableApplicationCluster(enableLeaderElection = true, enableMonitor = true)
@Import({ JellyfishAutoConfiguration.class, JellyfishUIAutoConfiguration.class })
public @interface EnableJellyfishClient {
}
