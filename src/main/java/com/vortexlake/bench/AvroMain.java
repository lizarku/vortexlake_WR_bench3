package com.vortexlake.bench;

import com.vortexlake.bench.model.RegularLog;
import com.vortexlake.bench.reader.LogReader;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class AvroMain {

    static final Schema SCHEMA = ReflectData.get().getSchema(RegularLog.class);

    public static EncodeResult encode(List<Path> files) throws Exception {
        Path tempFile = Files.createTempFile("avro_", ".avro");
        DatumWriter<RegularLog> datumWriter = new ReflectDatumWriter<>(RegularLog.class);

        long totalParseNs = 0;
        long totalSerializeNs = 0;
        int totalCount = 0;

        try (DataFileWriter<RegularLog> writer = new DataFileWriter<>(datumWriter)) {
            writer.create(SCHEMA, new BufferedOutputStream(new FileOutputStream(tempFile.toFile())));

            for (Path file : files) {
                long p1 = System.nanoTime();
                List<RegularLog> logs = LogReader.read(file).collect(Collectors.toList());
                long p2 = System.nanoTime();
                totalParseNs += (p2 - p1);

                long s1 = System.nanoTime();
                for (RegularLog log : logs) {
                    writer.append(log);
                }
                long s2 = System.nanoTime();
                totalSerializeNs += (s2 - s1);

                totalCount += logs.size();
            }
        }

        System.out.println("count: " + totalCount);

        InputStream stream = new BufferedInputStream(new FileInputStream(tempFile.toFile()) {
            @Override
            public void close() throws IOException {
                super.close();
                Files.deleteIfExists(tempFile);
            }
        });
        return new EncodeResult(stream, totalParseNs, totalSerializeNs, totalCount);
    }
}
