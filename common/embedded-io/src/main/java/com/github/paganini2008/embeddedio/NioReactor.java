package com.github.paganini2008.embeddedio;

import java.io.IOError;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;

/**
 * 
 * NioReactor
 *
 * @author Fred Feng
 * @since 1.0
 */
public abstract class NioReactor implements Runnable {

	private static final String REACTOR_THREAD_NAME_PREFIX = "nio-reactor-";
	private static final AtomicInteger threadSerialNo = new AtomicInteger(1);
	protected final Log logger = LogFactory.getLog(getClass());
	private final AtomicBoolean opened = new AtomicBoolean();
	protected Thread executor;
	protected final Selector selector;

	protected NioReactor() {
		try {
			this.selector = Selector.open();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	private long timeout = 0L;

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	protected void register(SelectableChannel channel, int ops, Object attachment) throws IOException {
		selector.wakeup();
		channel.register(selector, ops, attachment);
		if (!isOpened()) {
			opened.set(true);
			executor = ThreadUtils.runAsThread(REACTOR_THREAD_NAME_PREFIX + threadSerialNo.getAndIncrement(), this);
		}
	}

	public boolean isOpened() {
		return opened.get();
	}

	public void destroy() {
		opened.set(false);
		selector.wakeup();
		try {
			executor.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void run() {
		while (isOpened()) {
			int keys = 0;
			try {
				keys = timeout > 0 ? selector.select(timeout) : selector.select();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
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
			}
		}
	}

	protected abstract boolean isSelectable(SelectionKey selectionKey);

	protected abstract void process(SelectionKey selectionKey) throws IOException;

}
