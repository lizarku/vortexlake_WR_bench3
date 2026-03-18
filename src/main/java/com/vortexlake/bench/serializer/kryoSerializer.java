package com.vortexlake.bench.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.vortexlake.bench.model.BaseLog;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class kryoSerializer {

    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(BaseLog.class);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    public static Kryo getInstance() {
        return kryoThreadLocal.get();
    }

    public static byte[] serialize(List<BaseLog> records) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Output output = new Output(baos)) {
            Kryo kryo = getInstance();
            for (BaseLog log : records) {
                kryo.writeObject(output, log);
            }
        }
        return baos.toByteArray();
    }
}
