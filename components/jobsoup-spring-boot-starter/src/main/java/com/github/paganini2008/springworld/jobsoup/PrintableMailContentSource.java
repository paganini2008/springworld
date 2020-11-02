package com.github.paganini2008.springworld.jobsoup;

import java.util.Date;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.date.DateUtils;

/**
 * 
 * PrintableMailContentSource
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class PrintableMailContentSource implements MailContentSource {

	private static final String NEWLINE = System.getProperty("line.separator");

	@Override
	public String getContent(long traceId, JobKey jobKey, Object attachment, Date startDate, RunningState runningState, Throwable reason) {
		final StringBuilder str = new StringBuilder();
		str.append(NEWLINE).append(StringUtils.repeat("-", 60)).append(NEWLINE);
		str.append("| ClusterName: ").append(jobKey.getClusterName()).append(" | GroupName: ").append(jobKey.getGroupName())
				.append(NEWLINE);
		str.append("| JobName: ").append(jobKey.getJobName()).append(" | JobClassName: ").append(jobKey.getJobClassName()).append(NEWLINE);
		str.append("| TraceId: ").append(traceId).append(" | StartDate: ").append(DateUtils.format(startDate, "dd/M/yyyy HH:mm:ss"))
				.append(NEWLINE);
		str.append("| RunningState: ").append(runningState).append(" | Attachment: ").append(attachment).append(NEWLINE);
		if (reason != null) {
			str.append(NEWLINE);
			String[] stackTraces = ExceptionUtils.toArray(reason);
			for (String stackTrace : stackTraces) {
				str.append(stackTrace).append(NEWLINE);
			}
		}
		return str.toString();
	}

	@Override
	public boolean isHtml() {
		return false;
	}

}
