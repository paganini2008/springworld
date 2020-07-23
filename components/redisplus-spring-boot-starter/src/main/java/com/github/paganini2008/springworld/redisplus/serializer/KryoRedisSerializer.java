package com.github.paganini2008.springworld.redisplus.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;

/**
 * 
 * KryoRedisSerializer
 *
 * @author Fred Feng
 * @version 1.0
 */
public class KryoRedisSerializer<T> implements RedisSerializer<T> {

	private static final int DEFAULT_POOL_SIZE = 16;
	private static final int DEFAULT_IO_POOL_SIZE = 128;
	private static final int DEFAULT_POOL_BUFFER_SIZE = 8192;

	private final Class<T> objectClass;
	private final Pool<Kryo> pool;
	private final Pool<Output> outputPool;
	private final Pool<Input> inputPool;

	public KryoRedisSerializer(Class<T> objectClass) {
		this(objectClass, DEFAULT_POOL_SIZE, DEFAULT_IO_POOL_SIZE, DEFAULT_IO_POOL_SIZE, DEFAULT_POOL_BUFFER_SIZE);
	}

	public KryoRedisSerializer(Class<T> objectClass, int poolSize, int outputSize, int inputSize, int bufferSize) {
		this.objectClass = objectClass;
		this.pool = KryoHelper.getPool(poolSize);
		this.outputPool = KryoHelper.getOutputPool(outputSize, bufferSize);
		this.inputPool = KryoHelper.getInputPool(inputSize, bufferSize);
	}

	@Override
	public byte[] serialize(T object) throws SerializationException {
		if (object == null) {
			return null;
		}
		Kryo kryo = pool.obtain();
		Output output = outputPool.obtain();
		try {
			output.reset();
			kryo.writeObject(output, object);
			return output.getBuffer();
		} finally {
			outputPool.free(output);
			pool.free(kryo);
		}
	}

	@Override
	public T deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		Kryo kryo = pool.obtain();
		Input input = inputPool.obtain();
		try {
			input.setBuffer(bytes);
			return kryo.readObject(input, objectClass);
		} finally {
			inputPool.free(input);
			pool.free(kryo);
		}
	}

}
