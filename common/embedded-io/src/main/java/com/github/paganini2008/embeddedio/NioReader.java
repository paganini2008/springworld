package com.github.paganini2008.embeddedio;

import java.nio.channels.SelectionKey;

/**
 * 
 * NioReader
 *
 * @author Fred Feng
 * @since 1.0
 */
public class NioReader extends NioReactor {

	NioReader() {
		super(true);
	}

	@Override
	protected boolean isSelectable(SelectionKey selectionKey) {
		return selectionKey.isReadable();
	}

	@Override
	protected void process(SelectionKey selectionKey) {
		Channel channel = (Channel) selectionKey.attachment();
		if (channel != null) {
			channel.read();
		}
	}

}
