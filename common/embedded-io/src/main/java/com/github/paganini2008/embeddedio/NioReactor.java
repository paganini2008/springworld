package com.github.paganini2008.embeddedio;

import java.io.IOError;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;

/**
 * 
 * NioReactor
 *
 * @author Fred Feng
 * @since 1.0
 */
public abstract class NioReactor implements Runnable, Executable {

	private static final int JVMBUG_THRESHOLD = 512;
	private static final String REACTOR_THREAD_NAME_PREFIX = "nio-reactor-";
	private static final AtomicInteger threadSerialNo = new AtomicInteger(1);
	protected final Log logger = LogFactory.getLog(getClass());
	private final AtomicBoolean opened = new AtomicBoolean();
	private final boolean toFixJvmBug;
	private Thread worker;
	protected Selector selector;
	private volatile int jvmBug = 0;

	protected NioReactor(boolean toFixJvmBug) {
		this.toFixJvmBug = toFixJvmBug;
		this.selector = openSelector();
	}

	private long timeout = 0L;

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	protected Selector openSelector() {
		try {
			return Selector.open();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	private Selector rebuildSelector() {
		final Selector newSelector = openSelector();
		for (SelectionKey key : selector.keys()) {
			try {
				if (!key.isValid()) {
					continue;
				}
				if (key.channel().keyFor(newSelector) != null) {
					continue;
				}
				int ops = key.interestOps();
				Object att = key.attachment();
				key.cancel();
				key.channel().register(newSelector, ops, att);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		try {
			selector.close();
		} catch (IOException ignored) {
			selector = null;
		}
		return newSelector;
	}

	@Override
	public boolean execute() {
		synchronized (this) {
			if (toFixJvmBug && jvmBug >= JVMBUG_THRESHOLD) {
				selector = rebuildSelector();
			}
		}
		return isOpened();
	}

	protected void register(SelectableChannel channel, int ops, Object attachment) throws IOException {
		selector.wakeup();
		channel.register(selector, ops, attachment);
		if (!isOpened()) {
			opened.set(true);
			worker = ThreadUtils.runAsThread(REACTOR_THREAD_NAME_PREFIX + threadSerialNo.getAndIncrement(), this);
			if (toFixJvmBug) {
				ThreadUtils.scheduleAtFixedRate(this, 1, TimeUnit.SECONDS);
			}
		}
	}

	public boolean isOpened() {
		return opened.get();
	}

	public void destroy() {
		opened.set(false);
		selector.wakeup();
		try {
			worker.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void run() {
		int keys;
		while (isOpened()) {
			synchronized (this) {
				try {
					keys = timeout > 0 ? selector.select(timeout) : selector.select();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					keys = 0;
				}
				if (keys > 0) {
					Iterator<?> keyIterator = selector.selectedKeys().iterator();
					while (keyIterator.hasNext()) {
						SelectionKey selectionKey = (SelectionKey) keyIterator.next();
						if (selectionKey.isValid() && isSelectable(selectionKey)) {
							try {
								process(selectionKey);
							} catch (IOException e) {
								logger.error(e.getMessage(), e);
							}
						}
						keyIterator.remove();
					}
					jvmBug = 0;
				} else {
					if (toFixJvmBug) {
						jvmBug++;
						ThreadUtils.randomSleep(1000L);
					}
				}
			}
		}
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				selector = null;
			}
		}
	}

	protected abstract boolean isSelectable(SelectionKey selectionKey);

	protected abstract void process(SelectionKey selectionKey) throws IOException;

}
