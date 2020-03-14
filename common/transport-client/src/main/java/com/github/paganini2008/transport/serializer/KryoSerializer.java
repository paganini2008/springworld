package com.github.paganini2008.transport.serializer;

import java.io.InputStream;
import java.util.HashMap;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.esotericsoftware.kryo.util.Pool;
import com.github.paganini2008.transport.Tuple;
import com.github.paganini2008.transport.TupleImpl;

/**
 * 
 * KryoSerializer
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class KryoSerializer implements Serializer {

	public static final int DEFAULT_POOL_SIZE = 128;

	private final Pool<Kryo> pool;
	private final Pool<Output> outputPool;
	private final Pool<Input> inputPool;

	public KryoSerializer() {
		this(16, DEFAULT_POOL_SIZE, DEFAULT_POOL_SIZE);
	}

	public KryoSerializer(int poolSize, int outputSize, int inputSize) {
		pool = new Pool<Kryo>(true, false, poolSize) {

			@Override
			protected Kryo create() {
				Kryo kryo = new Kryo();
				kryo.setReferences(false);
				kryo.setRegistrationRequired(false);
				kryo.register(Tuple.class, new MapSerializer<HashMap<String, Object>>());
				kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
				return kryo;
			}

		};

		outputPool = new Pool<Output>(true, false, outputSize) {
			protected Output create() {
				return new Output(8192, -1);
			}
		};

		inputPool = new Pool<Input>(true, false, inputSize) {
			protected Input create() {
				return new Input(4096);
			}
		};

	}

	public byte[] serialize(Tuple tuple) {
		Kryo kryo = pool.obtain();
		Output output = outputPool.obtain();
		try {
			output.reset();
			kryo.writeObject(output, tuple);
			return output.getBuffer();
		} finally {
			outputPool.free(output);
			pool.free(kryo);
		}
	}

	public Tuple deserialize(byte[] bytes) {
		Kryo kryo = pool.obtain();
		Input input = inputPool.obtain();
		try {
			input.setBuffer(bytes);
			return kryo.readObject(input, TupleImpl.class);
		} finally {
			inputPool.free(input);
			pool.free(kryo);
		}
	}

	public Tuple deserialize(InputStream in) {
		Kryo kryo = pool.obtain();
		Input input = inputPool.obtain();
		try {
			input.setInputStream(in);
			return kryo.readObject(input, TupleImpl.class);
		} finally {
			inputPool.free(input);
			pool.free(kryo);
		}
	}

}
