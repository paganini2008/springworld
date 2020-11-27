package com.github.paganini2008.springdessert.transport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadPool;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springdessert.transport.buffer.BufferZone;
import com.github.paganini2008.transport.Tuple;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * TupleLoopProcessor
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class TupleLoopProcessor implements Runnable {

	@Autowired
	private BufferZone bufferZone;

	@Autowired
	private Counter counter;

	@Autowired(required = false)
	private ThreadPool threadPool;

	@Value("${spring.application.transport.bufferzone.collectionName:default}")
	private String collectionName;

	@Value("${spring.application.transport.bufferzone.pullSize:100}")
	private int pullSize;

	private final Map<String, List<Handler>> topicAndHandlers = new ConcurrentHashMap<String, List<Handler>>();
	private final AtomicBoolean running = new AtomicBoolean(false);
	private Thread runner;
	private LoggingThread loggingThread = new LoggingThread();

	public void addHandler(Handler handler) {
		if (handler != null) {
			List<Handler> handlers = MapUtils.get(topicAndHandlers, handler.getTopic(), () -> {
				return new CopyOnWriteArrayList<Handler>();
			});
			if (!handlers.contains(handler)) {
				handlers.add(handler);
			}
		}
	}

	public void removeHandler(Handler handler) {
		if (handler != null) {
			List<Handler> handlers = topicAndHandlers.get(handler.getTopic());
			if (handlers != null) {
				while (handlers.contains(handler)) {
					handlers.remove(handler);
				}
			}
		}
	}

	public int countOfHandlers() {
		return topicAndHandlers.size();
	}

	public void startDaemon() {
		running.set(true);
		runner = ThreadUtils.runAsThread(this);
		loggingThread.start();
		log.info("TupleLoopProcessor is started.");
	}

	public void stop() {
		running.set(false);
		if (runner != null) {
			try {
				runner.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			log.info("TupleLoopProcessor is stoped.");
		}
	}

	@Override
	public void run() {
		while (running.get()) {
			List<Tuple> tuples = null;
			try {
				tuples = bufferZone.get(collectionName, pullSize);
			} catch (Throwable e) {
				if (log.isTraceEnabled()) {
					log.trace(e.getMessage(), e);
				}
			}
			if (CollectionUtils.isNotEmpty(tuples)) {
				for (Tuple tuple : tuples) {
					List<Handler> handlers = topicAndHandlers.get(tuple.getTopic());
					if (CollectionUtils.isNotEmpty(handlers)) {
						for (Handler handler : handlers) {
							Tuple copy = tuple.copy();
							if (threadPool != null) {
								threadPool.apply(() -> {
									handler.onData(copy);
								});
							} else {
								handler.onData(copy);
							}
						}
					}
				}
				tuples.clear();
				tuples = null;
			} else {
				ThreadUtils.randomSleep(1000L);
			}
		}
		log.info("Ending Loop!");
	}

	class LoggingThread implements Executable {

		public void start() {
			ThreadUtils.scheduleAtFixedRate(this, 3, TimeUnit.SECONDS);
		}

		@Override
		public boolean execute() {
			if (log.isTraceEnabled()) {
				try {
					log.trace("[Snapshot] count=" + counter.local() + "/" + counter.global() + ", tps=" + counter.localTps() + "/"
							+ counter.globalTps() + ", buffer=" + bufferZone.size(collectionName));
				} catch (Exception ignored) {
				}
			}
			return running.get();
		}

	}

}
