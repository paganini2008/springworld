package com.github.paganini2008.springworld.jobsoup;

import java.util.Date;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.ObjectUtils;
import com.github.paganini2008.devtools.TableArray;
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

	@Override
	public String getContent(long traceId, JobKey jobKey, Object attachment, Date startDate, RunningState runningState, Throwable reason) {
		StringBuilder html = new StringBuilder();
		html.append("<html lang=\"en\"><body>");
		html.append("<h5>Dear All: </h5>");
		TableArray tableArray = new TableArray(9, 2);
		tableArray.setWidth(0, 20).setWidth(1, 80);
		tableArray.setValueOnLeft(0, 0, "[job runtime info]: ", 0);
		tableArray.setValueOnRight(1, 0, "[cluster name]: ", 0);
		tableArray.setValueOnLeft(1, 1, jobKey.getClusterName(), 0);
		tableArray.setValueOnRight(2, 0, "[group name]: ", 0);
		tableArray.setValueOnLeft(2, 1, jobKey.getGroupName(), 0);
		tableArray.setValueOnRight(3, 0, "[job name]: ", 0);
		tableArray.setValueOnLeft(3, 1, jobKey.getJobName(), 0);
		tableArray.setValueOnRight(4, 0, "[job class name]: ", 0);
		tableArray.setValueOnLeft(4, 1, jobKey.getJobClassName(), 0);
		tableArray.setValueOnRight(5, 0, "[trace id]: ", 0);
		tableArray.setValueOnLeft(5, 1, String.valueOf(traceId), 0);
		tableArray.setValueOnRight(6, 0, "[start date]: ", 0);
		tableArray.setValueOnLeft(6, 1, DateUtils.format(startDate, "MM/dd/yyyy HH:mm:ss"), 0);
		tableArray.setValueOnRight(7, 0, "[running state]: ", 0);
		tableArray.setValueOnLeft(7, 1, runningState.getRepr(), 0);
		tableArray.setValueOnRight(8, 0, "[attachment]: ", 0);
		tableArray.setValueOnLeft(8, 1, ObjectUtils.toString(attachment), 0);
		html.append("<pre>");
		html.append(tableArray.toString(true, false));
		html.append("</pre>");

		if (reason != null) {
			html.append("<br/>");
			String[] thrownArray = ExceptionUtils.toArray(reason);
			tableArray = new TableArray(thrownArray.length + 1, 1);
			tableArray.setWidth(0, 100);
			tableArray.setValueOnLeft(0, 0, "[throwable]: ", 0);
			for (int i = 0; i < thrownArray.length; i++) {
				tableArray.setValueOnLeft(i + 1, 0, thrownArray[i], 0);
			}
			html.append("<pre>");
			html.append(tableArray.toString(false, false));
			html.append("</pre>");
		}
		html.append("</body></html>");
		return html.toString();
	}

	@Override
	public boolean isHtml() {
		return true;
	}

}
