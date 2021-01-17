package com.github.paganini2008.springdessert.logbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.logbox.logback.TransportClientAppenderBase;
import com.github.paganini2008.springdessert.logbox.es.LogEntry;
import com.github.paganini2008.springdessert.logbox.es.LogEntryService;
import com.github.paganini2008.springdessert.reditools.common.IdGenerator;
import com.github.paganini2008.springdessert.xtransport.Handler;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * LogbackHandler
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class LogbackHandler implements Handler {

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private LogEntryService logEntryService;

	@Value("${spring.application.cluster.logbox.interferedCharacterRegex:}")
	private String interferedCharacterRegex;

	@Override
	public void onData(Tuple tuple) {
		LogEntry logEntry = new LogEntry();
		logEntry.setId(idGenerator.generateId());
		logEntry.setClusterName(tuple.getField("clusterName", String.class));
		logEntry.setApplicationName(tuple.getField("applicationName", String.class));
		logEntry.setHost(tuple.getField("host", String.class));
		logEntry.setIdentifier(tuple.getField("identifier", String.class));
		logEntry.setLoggerName(tuple.getField("loggerName", String.class));
		logEntry.setMessage(tuple.getField("message", String.class));
		logEntry.setLevel(tuple.getField("level", String.class));
		logEntry.setReason(tuple.getField("reason", String.class));
		logEntry.setMarker(tuple.getField("marker", String.class));
		logEntry.setMdc(tuple.getField("mdc", String.class));
		logEntry.setCreateTime(tuple.getField("timestamp", Long.class));
		if (StringUtils.isNotBlank(interferedCharacterRegex)) {
			logEntry.setMessage(logEntry.getMessage().replaceAll(interferedCharacterRegex, ""));
			logEntry.setReason(logEntry.getReason().replaceAll(interferedCharacterRegex, ""));
		}
		logEntryService.bulkSaveLogEntry(logEntry);
	}

	@Override
	public String getTopic() {
		return TransportClientAppenderBase.GLOBAL_TOPIC_NAME;
	}

}
