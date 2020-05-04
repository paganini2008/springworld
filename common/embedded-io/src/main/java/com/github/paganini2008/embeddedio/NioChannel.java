package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
	private final AppendableByteBuffer cachedReaderBuffer;

	NioChannel(ChannelEventPublisher eventPublisher, SocketChannel channel, Transformer transformer, int autoFlushInterval) {
		this.eventPublisher = eventPublisher;
		this.channel = channel;
		this.transformer = transformer;
		this.cachedReaderBuffer = new AppendableByteBuffer(DEFAULT_BUFFER_INCREMENT_SIZE);
		if (autoFlushInterval > 0) {
			ThreadUtils.scheduleWithFixedDelay(this, autoFlushInterval, TimeUnit.SECONDS);
		}
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
	public long writeAndFlush(Object message) {
		long length = 0;
		AppendableByteBuffer byteBuffer = new AppendableByteBuffer();
		transformer.transferTo(message, byteBuffer);
		ByteBuffer data = byteBuffer.get();
		synchronized (channel) {
			data.flip();
			try {
				while (data.hasRemaining()) {
					length += channel.write(data);
				}
			} catch (IOException e) {
				eventPublisher.publishChannelEvent(new ChannelEvent(this, EventType.FATAL, null, e));
				close();
			}
		}
		return length;
	}

	@Override
	public long write(Object message, int batchSize) {
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
		synchronized (channel) {
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
			}
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
		AppendableByteBuffer byteBuffer = new AppendableByteBuffer(DEFAULT_BUFFER_INCREMENT_SIZE);
		List<Object> list = new ArrayList<Object>();
		if (writerQueue.drainTo(list) > 0) {
			for (Object object : list) {
				transformer.transferTo(object, byteBuffer);
			}
			ByteBuffer data = byteBuffer.get();
			data.flip();
			synchronized (channel) {
				try {
					while (data.hasRemaining()) {
						length += channel.write(data);
					}
				} catch (IOException e) {
					eventPublisher.publishChannelEvent(new ChannelEvent(this, EventType.FATAL, null, e));
					close();
				}
			}
		}
		return length;
	}

	public String toString() {
		return channel.toString();
	}

}
