package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.embeddedio.ChannelEvent.EventType;

/**
 * 
 * NioChannel
 *
 * @author Fred Feng
 * @since 1.0
 */
public class NioChannel implements Channel, Executable {

	private static final int DEFAULT_BUFFER_INCREMENT_SIZE = 1024;
	private final BlockingQueue<Object> writerQueue = new LinkedBlockingQueue<Object>();
	private final ChannelEventPublisher eventPublisher;
	private final SocketChannel channel;
	private final Transformer transformer;
	private final IoBuffer cachedReaderBuffer;
	private final int batchSize;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final WriteLock writerLock = lock.writeLock();
	private final ReadLock readerLock = lock.readLock();
	private SocketAddress localAddress;
	private SocketAddress remoteAddress;

	NioChannel(SocketChannel channel, ChannelEventPublisher eventPublisher, Transformer transformer, int batchSize, int autoFlushInterval) {
		this.channel = channel;
		this.eventPublisher = eventPublisher;
		this.transformer = transformer;
		this.cachedReaderBuffer = new AppendableByteBuffer(DEFAULT_BUFFER_INCREMENT_SIZE);
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
			while (data.hasRemaining()) {
				length += channel.write(data);
			}
		} catch (IOException e) {
			eventPublisher.publishChannelEvent(new ChannelEvent(this, EventType.FATAL, null, e));
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
	public long read() {
		ByteBuffer readerBuffer;
		try {
			readerBuffer = ByteBuffer.allocate(channel.socket().getReceiveBufferSize());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		long length, total = 0;
		readerLock.lock();
		try {
			while ((length = channel.read(readerBuffer)) > 0) {
				readerBuffer.flip();
				cachedReaderBuffer.append(readerBuffer);
				readerBuffer.clear();
				total += length;
			}
		} catch (IOException e) {
			eventPublisher.publishChannelEvent(new ChannelEvent(this, EventType.FATAL, null, e));
			close();
		} finally {
			readerLock.unlock();
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
				eventPublisher.publishChannelEvent(new ChannelEvent(this, EventType.READABLE, MessagePacket.of(output, total), null));
			}
		}
		return total;
	}

	@Override
	public boolean isActive() {
		return channel.isConnected();
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
				while (data.hasRemaining()) {
					length += channel.write(data);
				}
				eventPublisher.publishChannelEvent(new ChannelEvent(this, EventType.WRITEABLE, MessagePacket.of(list, length), null));
			} catch (IOException e) {
				eventPublisher.publishChannelEvent(new ChannelEvent(this, EventType.FATAL, null, e));
				close();
			} finally {
				writerLock.unlock();
			}
		}
		return length;
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
	public SocketAddress getLocalAddr() {
		if (localAddress == null) {
			try {
				localAddress = channel.getLocalAddress();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return localAddress;
	}

	@Override
	public SocketAddress getRemoteAddr() {
		if (remoteAddress == null) {
			try {
				remoteAddress = channel.getRemoteAddress();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return remoteAddress;
	}

	public String toString() {
		return channel.toString();
	}

}
