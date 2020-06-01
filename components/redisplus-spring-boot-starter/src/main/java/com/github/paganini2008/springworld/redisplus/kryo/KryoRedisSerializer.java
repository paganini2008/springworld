package com.github.paganini2008.springworld.redisplus.kryo;

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
public class KryoRedisSerializer implements RedisSerializer<Object> {

	public static final int DEFAULT_POOL_SIZE = 16;
	public static final int DEFAULT_IO_POOL_SIZE = 128;

	private final Class<?> objectClass;
	private final Pool<Kryo> pool;
	private final Pool<Output> outputPool;
	private final Pool<Input> inputPool;

	public KryoRedisSerializer(Class<?> objectClass) {
		this(objectClass, DEFAULT_POOL_SIZE, DEFAULT_IO_POOL_SIZE, DEFAULT_IO_POOL_SIZE, 4096);
	}

	public KryoRedisSerializer(Class<?> objectClass, int poolSize, int outputSize, int inputSize, int bufferSize) {
		this.objectClass = objectClass;
		this.pool = KryoUtils.getPool(poolSize);
		this.outputPool = KryoUtils.getOutputPool(outputSize, bufferSize);
		this.inputPool = KryoUtils.getInputPool(inputSize, bufferSize);
	}

	@Override
	public byte[] serialize(Object object) throws SerializationException {
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
	public Object deserialize(byte[] bytes) throws SerializationException {
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
