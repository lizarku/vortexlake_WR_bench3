package com.vortexlake.bench;

import com.vortexlake.bench.model.BaseLog;
import com.vortexlake.bench.reader.LogReader;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AvroMain {

    static final Schema SCHEMA = ReflectData.get().getSchema(BaseLog.class);

    public static InputStream encode(List<Path> files) throws Exception {
        AtomicInteger count = new AtomicInteger();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DatumWriter<BaseLog> datumWriter = new ReflectDatumWriter<>(BaseLog.class);

        try (DataFileWriter<BaseLog> writer = new DataFileWriter<>(datumWriter)) {
            writer.create(SCHEMA, baos);
            for (Path file : files) {
                LogReader.read(file).forEach(log -> {
                    try {
                        writer.append(log);
                        count.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        System.out.println("count: " + count.get());
        return new ByteArrayInputStream(baos.toByteArray());
    }

}
