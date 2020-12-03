package com.github.paganini2008.springdessert.jobsoup;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.github.paganini2008.springdessert.jobsoup.server.ServerMode;

/**
 * 
 * OnServerModeCondition
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class OnServerModeCondition extends SpringBootCondition {

	private static final ConditionMessage EMPYT_MESSAGE = ConditionMessage.empty();

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ConditionalOnServerMode.class.getName());
		ServerMode serverSide = (ServerMode) annotationAttributes.get("value");
		final String side = context.getEnvironment().getProperty("jobsoup.server.mode.side", "consumer");
		if (serverSide.getValue().equals(side)) {
			return ConditionOutcome.match(EMPYT_MESSAGE);
		}
		return ConditionOutcome.noMatch(EMPYT_MESSAGE);
	}

}
