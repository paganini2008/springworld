package com.github.paganini2008.embeddedio;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 
 * IoBuffer
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface IoBuffer {

	IoBuffer append(String value);

	IoBuffer append(String value, Charset charset);

	String getString();

	String getString(Charset charset);

	IoBuffer append(ByteBuffer bb);

	IoBuffer append(byte[] bytes);

	byte[] getBytes();

	IoBuffer append(double value);

	double getDouble();

	IoBuffer append(long value);

	long getLong();

	IoBuffer append(float value);

	float getFloat();

	IoBuffer append(int value);

	int getInt();

	IoBuffer append(short value);

	short getShort();

	IoBuffer append(char value);

	char getChar();

	IoBuffer append(byte value);

	byte getByte();

	long length();

	IoBuffer flip();

	IoBuffer limit(int limit);

	int limit();

	int position();

	int remaining();

	boolean hasRemaining();

	boolean hasRemaining(int length);

	void get(byte[] bytes);

	void reset();

	void clear();

	ByteBuffer get();

}