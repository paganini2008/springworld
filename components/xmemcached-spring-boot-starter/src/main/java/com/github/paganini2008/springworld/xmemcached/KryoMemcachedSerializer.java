package com.github.paganini2008.springworld.xmemcached;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import com.github.paganini2008.devtools.io.SerializationException;

/**
 * 
 * KryoMemcachedSerializer
 *
 * @author Fred Feng
 * @version 1.0
 */
public class KryoMemcachedSerializer implements MemcachedSerializer {

	public static final int DEFAULT_POOL_SIZE = 128;

	private final Pool<Kryo> pool;
	private final Pool<Output> outputPool;
	private final Pool<Input> inputPool;

	public KryoMemcachedSerializer() {
		this(16, DEFAULT_POOL_SIZE, DEFAULT_POOL_SIZE, 4096);
	}

	public KryoMemcachedSerializer(int poolSize, int outputSize, int inputSize, int bufferSize) {
		pool = new Pool<Kryo>(true, false, poolSize) {

			@Override
			protected Kryo create() {
				Kryo kryo = new Kryo();
				kryo.setReferences(false);
				kryo.setRegistrationRequired(false);
				kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
				return kryo;
			}

		};

		outputPool = new Pool<Output>(true, false, outputSize) {
			protected Output create() {
				return new Output(bufferSize, -1);
			}
		};

		inputPool = new Pool<Input>(true, false, inputSize) {
			protected Input create() {
				return new Input(bufferSize);
			}
		};
	}

	@Override
	public byte[] serialize(Object object) {
		Kryo kryo = pool.obtain();
		Output output = outputPool.obtain();
		try {
			output.reset();
			kryo.writeObject(output, object);
			return output.getBuffer();
		} catch (Exception e) {
			throw new SerializationException(e);
		} finally {
			outputPool.free(output);
			pool.free(kryo);
		}
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> requiredType) throws Exception {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		Kryo kryo = pool.obtain();
		Input input = inputPool.obtain();
		try {
			input.setBuffer(bytes);
			return kryo.readObject(input, requiredType);
		} catch (Exception e) {
			throw new SerializationException(e);
		} finally {
			inputPool.free(input);
			pool.free(kryo);
		}
	}

}
