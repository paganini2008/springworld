package com.github.paganini2008.springdessert.jellyfish.stat;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.collection.SequentialMetricsCollector;
import com.github.paganini2008.devtools.collection.SimpleSequentialMetricsCollector;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.devtools.date.SpanUnit;

/**
 * 
 * DefaultSequentialMetricsCollectorFactory
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@SuppressWarnings("all")
public class DefaultSequentialMetricsCollectorFactory implements SequentialMetricsCollectorFactory {

	private String datetimePattern = SimpleSequentialMetricsCollector.DEFAULT_DATETIME_PATTERN;
	private int span = 1;
	private SpanUnit spanUnit = SpanUnit.MINUTE;
	private int bufferSize = 60;

	public void setDatetimePattern(String datetimePattern) {
		this.datetimePattern = datetimePattern;
	}

	public void setSpan(int span) {
		this.span = span;
	}

	public void setSpanUnit(SpanUnit spanUnit) {
		this.spanUnit = spanUnit;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	@Override
	public SequentialMetricsCollector createSequentialMetricsCollector(Catalog catalog) {
		return new SimpleSequentialMetricsCollector(bufferSize, span, spanUnit, null);
	}

	@Override
	public Map render(Map map) {
		Map<String, MetricVO> data = (Map<String, MetricVO>) map;
		if (!(data instanceof TreeMap)) {
			data = new TreeMap<String, MetricVO>(data);
		}
		Map.Entry<String, MetricVO> entry = MapUtils.getFirstEntry(data);
		Date startDate = getStartDate(entry.getKey(), entry.getValue());
		Map<String, MetricVO> sequentialMap = sequentialMap(startDate);
		sequentialMap.putAll(data);
		return sequentialMap;
	}

	private Date getStartDate(String time, MetricVO vo) {
		Calendar copy = Calendar.getInstance();
		copy.setTimeInMillis(vo.getTimestamp());
		int year = copy.get(Calendar.YEAR);
		int month = copy.get(Calendar.MONTH);
		int date = copy.get(Calendar.DATE);
		Date startDate = DateUtils.parse(time, datetimePattern);
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DATE, date);
		return c.getTime();
	}

	protected Map<String, MetricVO> sequentialMap(Date startDate) {
		Map<String, MetricVO> map = new TreeMap<String, MetricVO>();
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		for (int i = 0; i < bufferSize; i++) {
			c.add(Calendar.MINUTE, 1);
			map.put(DateUtils.format(c.getTime(), datetimePattern), new MetricVO());
		}
		return map;
	}

}
