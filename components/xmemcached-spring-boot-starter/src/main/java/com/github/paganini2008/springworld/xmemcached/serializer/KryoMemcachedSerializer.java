package com.github.paganini2008.springworld.xmemcached.serializer;

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

	public static final int DEFAULT_POOL_SIZE = 16;
	private static final int DEFAULT_IO_POOL_SIZE = 128;
	private static final int DEFAULT_POOL_BUFFER_SIZE = 8192;

	private final Pool<Kryo> pool;
	private final Pool<Output> outputPool;
	private final Pool<Input> inputPool;

	public KryoMemcachedSerializer() {
		this(DEFAULT_POOL_SIZE, DEFAULT_IO_POOL_SIZE, DEFAULT_IO_POOL_SIZE, DEFAULT_POOL_BUFFER_SIZE);
	}

	public KryoMemcachedSerializer(int poolSize, int outputSize, int inputSize, int bufferSize) {
		this.pool = KryoHelper.getPool(poolSize);
		this.outputPool = KryoHelper.getOutputPool(outputSize, bufferSize);
		this.inputPool = KryoHelper.getInputPool(inputSize, bufferSize);
	}

	@Override
	public byte[] serialize(Object object) {
		if (object == null) {
			return null;
		}
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
