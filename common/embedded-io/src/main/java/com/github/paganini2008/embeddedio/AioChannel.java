package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.embeddedio.ChannelEvent.EventType;

/**
 * 
 * AioChannel
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class AioChannel implements Channel, Executable {

	private static final int DEFAULT_BUFFER_INCREMENT_SIZE = 1024;
	private final BlockingQueue<Object> writerQueue = new LinkedBlockingQueue<Object>();
	private final ChannelEventPublisher eventPublisher;
	private final AsynchronousSocketChannel channel;
	private final Transformer transformer;
	private final int batchSize;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final WriteLock writerLock = lock.writeLock();
	private final ReadLock readerLock = lock.readLock();

	AioChannel(AsynchronousSocketChannel channel, ChannelEventPublisher eventPublisher, Transformer transformer, int batchSize,
			int autoFlushInterval) {
		this.channel = channel;
		this.eventPublisher = eventPublisher;
		this.transformer = transformer;
		this.batchSize = batchSize;
		if (autoFlushInterval > 0) {
			ThreadUtils.scheduleWithFixedDelay(this, autoFlushInterval, TimeUnit.SECONDS);
		}
	}

	@Override
	public long writeAndFlush(Object message) {
		long length = 0;
		IoBuffer byteBuffer = new AppendableByteBuffer();
		transformer.transferTo(message, byteBuffer);
		ByteBuffer data = byteBuffer.get();
		data.flip();
		writerLock.lock();
		try {
			length += channel.write(data).get();
		} catch (Exception e) {
			eventPublisher.publishChannelEvent(new ChannelEvent(AioChannel.this, EventType.FATAL, null, e));
			close();
		} finally {
			writerLock.unlock();
		}
		return length;
	}

	@Override
	public long write(Object message) {
		if (batchSize == 1) {
			return writeAndFlush(message);
		} else if (batchSize > 1) {
			writerQueue.add(message);
			if (writerQueue.size() % batchSize == 0) {
				return flush();
			}
			return 0;
		} else {
			throw new IllegalArgumentException("Invalid batchSize: " + batchSize);
		}
	}

	@Override
	public long flush() {
		long length = 0;
		IoBuffer byteBuffer = new AppendableByteBuffer(DEFAULT_BUFFER_INCREMENT_SIZE);
		List<Object> list = new ArrayList<Object>();
		if (writerQueue.drainTo(list) > 0) {
			for (Object object : list) {
				transformer.transferTo(object, byteBuffer);
			}
			ByteBuffer data = byteBuffer.get();
			data.flip();
			writerLock.lock();
			try {
				length += channel.write(data).get();
				eventPublisher
						.publishChannelEvent(new ChannelEvent(AioChannel.this, EventType.WRITEABLE, MessagePacket.of(list, length), null));
			} catch (Exception e) {
				eventPublisher.publishChannelEvent(new ChannelEvent(AioChannel.this, EventType.FATAL, null, e));
				close();
			} finally {
				writerLock.unlock();
			}
		}
		return length;
	}

	@Override
	public long read() {
		ByteBuffer readerBuffer;
		try {
			readerBuffer = ByteBuffer.allocate(channel.getOption(StandardSocketOptions.SO_RCVBUF));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		AtomicLong total = new AtomicLong(0);
		IoBuffer cachedReaderBuffer = new AppendableByteBuffer(DEFAULT_BUFFER_INCREMENT_SIZE);
		readerLock.lock();
		doRead(readerBuffer, cachedReaderBuffer, total);
		readerLock.unlock();
		return total.get();
	}

	private void doRead(ByteBuffer readerBuffer, IoBuffer cachedReaderBuffer, AtomicLong total) {
		channel.read(readerBuffer, null, new CompletionHandler<Integer, Object>() {

			@Override
			public void completed(Integer readBytes, Object attachment) {
				if (readBytes != null && readBytes > 0) {
					readerBuffer.flip();
					cachedReaderBuffer.append(readerBuffer);
					readerBuffer.clear();
					total.getAndSet(readBytes);
				}
				if (cachedReaderBuffer.hasRemaining()) {
					List<Object> output = new ArrayList<Object>();
					cachedReaderBuffer.flip();
					transformer.transferFrom(cachedReaderBuffer, output);
					if (cachedReaderBuffer.hasRemaining()) {
						cachedReaderBuffer.reset();
					} else {
						cachedReaderBuffer.clear();
					}
					if (output.size() > 0) {
						eventPublisher.publishChannelEvent(
								new ChannelEvent(AioChannel.this, EventType.READABLE, MessagePacket.of(output, total.get()), null));
					}
				}
				doRead(readerBuffer, cachedReaderBuffer, total);
			}

			@Override
			public void failed(Throwable e, Object attachment) {
				eventPublisher.publishChannelEvent(new ChannelEvent(AioChannel.this, EventType.FATAL, null, e));
				close();
			}
		});
	}

	@Override
	public void close() {
		if (isActive()) {
			try {
				channel.close();
				eventPublisher.publishChannelEvent(new ChannelEvent(this, EventType.INACTIVE));
			} catch (IOException e) {
				eventPublisher.publishChannelEvent(new ChannelEvent(this, EventType.FATAL, null, e));
			}
		}
	}

	@Override
	public boolean isActive() {
		return channel.isOpen();
	}

	@Override
	public SocketAddress getLocalAddr() {
		try {
			return channel.getLocalAddress();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public SocketAddress getRemoteAddr() {
		try {
			return channel.getRemoteAddress();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean execute() {
		if (writerQueue.isEmpty()) {
			return true;
		}
		try {
			flush();
		} catch (Exception e) {
			eventPublisher.publishChannelEvent(new ChannelEvent(this, EventType.FATAL, null, e));
		}
		return isActive();
	}

	public String toString() {
		return channel.toString();
	}

}
