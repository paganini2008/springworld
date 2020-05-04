package com.github.paganini2008.embeddedio;

import java.io.IOException;
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

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.embeddedio.ChannelEvent.EventType;

/**
 * 
 * AioChannel
 *
 * @author Fred Feng
 * @since 1.0
 */
public class AioChannel implements Channel, Executable {

	private static final int DEFAULT_BUFFER_INCREMENT_SIZE = 1024;
	private final BlockingQueue<Object> writerQueue = new LinkedBlockingQueue<Object>();
	private final ChannelEventPublisher eventPublisher;
	private final AsynchronousSocketChannel channel;
	private final Transformer transformer;

	AioChannel(ChannelEventPublisher eventPublisher, AsynchronousSocketChannel channel, Transformer transformer, int autoFlushInterval) {
		this.eventPublisher = eventPublisher;
		this.channel = channel;
		this.transformer = transformer;
		if (autoFlushInterval > 0) {
			ThreadUtils.scheduleWithFixedDelay(this, autoFlushInterval, TimeUnit.SECONDS);
		}
	}

	@Override
	public long writeAndFlush(Object message) {
		long length = 0;
		AppendableByteBuffer byteBuffer = new AppendableByteBuffer();
		transformer.transferTo(message, byteBuffer);
		ByteBuffer data = byteBuffer.get();
		data.flip();
		try {
			length += channel.write(data).get();
		} catch (Exception e) {
			eventPublisher.publishChannelEvent(new ChannelEvent(AioChannel.this, EventType.FATAL, null, e));
			close();
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
			try {
				length += channel.write(data).get();
			} catch (Exception e) {
				eventPublisher.publishChannelEvent(new ChannelEvent(AioChannel.this, EventType.FATAL, null, e));
				close();
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
		AppendableByteBuffer cachedReaderBuffer = new AppendableByteBuffer(DEFAULT_BUFFER_INCREMENT_SIZE);
		channel.read(readerBuffer, null, new CompletionHandler<Integer, Object>() {

			@Override
			public void completed(Integer readBytes, Object attachment) {
				if (readBytes != null && readBytes > 0) {
					readerBuffer.flip();
					cachedReaderBuffer.append(readerBuffer);
					readerBuffer.clear();
					total.addAndGet(readBytes);
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
				channel.read(readerBuffer, null, this);
			}

			@Override
			public void failed(Throwable e, Object attachment) {
				eventPublisher.publishChannelEvent(new ChannelEvent(AioChannel.this, EventType.FATAL, null, e));
				close();
			}
		});
		return total.get();
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
