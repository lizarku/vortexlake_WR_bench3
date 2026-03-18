package com.vortexlake.bench;

import com.vortexlake.bench.model.BaseLog;
import com.vortexlake.bench.reader.LogReader;
import org.apache.avro.reflect.ReflectData;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ParquetMain {

    public static InputStream encode(List<Path> files) throws Exception {
        AtomicInteger count = new AtomicInteger();
        Path tempFile = Files.createTempFile("parquet_", ".parquet");

        try {
            org.apache.hadoop.fs.Path hadoopPath =
                    new org.apache.hadoop.fs.Path(tempFile.toUri());

            try (ParquetWriter<BaseLog> writer =
                    AvroParquetWriter.<BaseLog>builder(hadoopPath)
                            .withSchema(AvroMain.SCHEMA)
                            .withDataModel(ReflectData.get())
                            .withCompressionCodec(CompressionCodecName.UNCOMPRESSED)
                            .withConf(new Configuration())
                            .build()) {

                for (Path file : files) {
                    LogReader.read(file).forEach(log -> {
                        try {
                            writer.write(log);
                            count.incrementAndGet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            System.out.println("count: " + count.get());
            byte[] bytes = Files.readAllBytes(tempFile);
            return new ByteArrayInputStream(bytes);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
